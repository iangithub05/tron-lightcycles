package com.example.network;

import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.LobbySettings;
import com.example.models.Player;
import com.example.models.Point;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class GameServer {

    public Consumer<Integer>          onPlayerCountChanged;
    public BiConsumer<String, String> onChatMessage;
    public Consumer<String>           onPlayerDisconnected;
    public Consumer<String>           onError;
    public BiConsumer<Integer, int[]> onRoundOver;
    public Consumer<Integer>          onMatchOver;

    private LobbySettings settings;
    private Game           game;
    private int[]          matchScores;
    private int[]          trailSentCount;

    private static final long NETWORK_BROADCAST_INTERVAL_NANOS = 50_000_000L; // 20 snapshots/sec
    private final Object gameLock = new Object();
    private long lastStateBroadcastNanos = 0L;

    private ServerSocket           serverSocket;
    private final List<ClientConn> clients = new CopyOnWriteArrayList<>();
    private final List<String>     playerNames = new CopyOnWriteArrayList<>();

    private LanDiscovery discovery;
    private String        roomCode;
    private String        hostName;

    private ScheduledExecutorService gameExecutor;
    private volatile boolean         gameRunning;
    private volatile Direction       hostDir = null;


    private class ClientConn {
        final int            slot;
        final Socket         socket;
        final BufferedReader in;
        final PrintWriter    out;
        volatile Direction   pendingDir;

        ClientConn(int slot, Socket socket) throws IOException {
            this.slot   = slot;
            this.socket = socket;
            this.in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out    = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        }

        void send(String msg) { out.println(msg); }
    }


    public void listen(int port, LobbySettings settings, String hostName) {
        this.settings     = settings;
        this.hostName     = hostName;
        this.matchScores  = new int[settings.maxPlayers];

        playerNames.add(hostName);

        roomCode  = RoomCode.encode(RoomCode.getLocalIp(), port);
        discovery = new LanDiscovery();
        discovery.startResponder(roomCode, hostName, 1, settings.maxPlayers);

        Thread acceptThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                while (!serverSocket.isClosed()) {
                    Socket s    = serverSocket.accept();
                    int    slot = clients.size() + 1;
                    if (slot >= settings.maxPlayers) { s.close(); continue; }
                    ClientConn conn = new ClientConn(slot, s);
                    clients.add(conn);
                    startClientReader(conn);
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed() && onError != null)
                    onError.accept(e.getMessage());
            }
        }, "server-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public String        getRoomCode()    { return roomCode; }
    public String        getLocalIp()     { return RoomCode.getLocalIp(); }
    public Game          getGame()        { return game; }
    public LobbySettings getSettings()   { return settings; }
    public List<String>  getPlayerNames() { return new ArrayList<>(playerNames); }

    public int getGamePlayerCount() {
        synchronized (gameLock) {
            return game == null ? 0 : game.players.size();
        }
    }

    public List<NetworkMessage.PlayerSnapshot> getRenderSnapshots(int[] trailDrawCount) {
        synchronized (gameLock) {
            List<NetworkMessage.PlayerSnapshot> snaps = new ArrayList<>();
            if (game == null || trailDrawCount == null) return snaps;

            List<Player> players = game.players;
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                List<Point> all = p.trail.points;
                int from = i < trailDrawCount.length ? Math.min(trailDrawCount[i], all.size()) : all.size();
                List<double[]> newTrailPoints = new ArrayList<>();
                for (int j = from; j < all.size(); j++) {
                    Point pt = all.get(j);
                    newTrailPoints.add(new double[]{pt.x, pt.y});
                }
                if (i < trailDrawCount.length) trailDrawCount[i] = all.size();
                snaps.add(new NetworkMessage.PlayerSnapshot(i, p.x, p.y, p.alive, newTrailPoints));
            }
            return snaps;
        }
    }

    public void updateSettings(LobbySettings s) {
        this.settings = s;
        broadcast(NetworkMessage.make(NetworkMessage.SETTINGS, s.encode()));
        if (discovery != null)
            discovery.updatePlayerCount(roomCode, hostName, clients.size() + 1, s.maxPlayers);
    }

    public void sendChat(String name, String message) {
        broadcast(NetworkMessage.make(NetworkMessage.CHAT, name, message));
        if (onChatMessage != null) onChatMessage.accept(name, message);
    }

    public void startGame() {
        buildGame();
        broadcast(NetworkMessage.make(NetworkMessage.START));
        if (discovery != null) discovery.stopResponder();
        beginGameLoop();
    }

    public void queueHostDirection(Direction dir) { hostDir = dir; }

    public void close() {
        gameRunning = false;
        if (gameExecutor != null) gameExecutor.shutdownNow();
        if (discovery    != null) discovery.stopResponder();
        for (ClientConn c : clients) closeConn(c);
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
    }


    private void startClientReader(ClientConn conn) {
        Thread t = new Thread(() -> {
            try {
                String line;
                while ((line = conn.in.readLine()) != null) {
                    handleClientMessage(conn, line.trim());
                }
            } catch (IOException ignored) {
            } finally {
                handleClientDisconnect(conn);
            }
        }, "server-client-" + conn.slot);
        t.setDaemon(true);
        t.start();
    }

    private void handleClientMessage(ClientConn conn, String raw) {
        String[] parts = NetworkMessage.parse(raw);
        switch (parts[0]) {
            case NetworkMessage.HELLO -> {
                String name = parts.length > 1 ? parts[1] : "PLAYER";
                while (playerNames.size() <= conn.slot) playerNames.add("");
                playerNames.set(conn.slot, name);
                conn.send(NetworkMessage.make(NetworkMessage.WELCOME,
                        String.valueOf(conn.slot), settings.encode()));
                broadcastLobbyState();
                if (onPlayerCountChanged != null)
                    onPlayerCountChanged.accept(clients.size() + 1);
                if (discovery != null)
                    discovery.updatePlayerCount(roomCode, hostName,
                            clients.size() + 1, settings.maxPlayers);
            }
            case NetworkMessage.INPUT -> {
                if (parts.length > 1) {
                    try { conn.pendingDir = Direction.valueOf(parts[1]); }
                    catch (Exception ignored) {}
                }
            }
            case NetworkMessage.CHAT -> {
                if (parts.length > 2) {
                    broadcast(raw);
                    if (onChatMessage != null) onChatMessage.accept(parts[1], parts[2]);
                }
            }
            case NetworkMessage.PING -> conn.send(NetworkMessage.PONG);
        }
    }

    private void handleClientDisconnect(ClientConn conn) {
        String name = conn.slot < playerNames.size()
                ? playerNames.get(conn.slot) : "PLAYER";
        clients.remove(conn);
        closeConn(conn);
        broadcast(NetworkMessage.make(NetworkMessage.DISCONNECT, name));
        if (onPlayerDisconnected != null) onPlayerDisconnected.accept(name);
        broadcastLobbyState();
        if (onPlayerCountChanged != null)
            onPlayerCountChanged.accept(clients.size() + 1);
    }


    private void buildGame() {
        synchronized (gameLock) {
            game = new Game(settings.toGameRules());

            double[][] startPos = {
                {80,                       80},
                {settings.gridWidth - 80,  80},
                {settings.gridWidth - 80,  settings.gridHeight - 80},
                {80,                       settings.gridHeight - 80}
            };
            Direction[] startDir = {
                Direction.RIGHT, Direction.DOWN,
                Direction.LEFT,  Direction.UP
            };

            int total = clients.size() + 1;
            for (int i = 0; i < total; i++) {
                game.addPlayer(new Player(startPos[i][0], startPos[i][1],
                        startDir[i], settings.toGameRules().playerSpeed));
            }

            trailSentCount = new int[total];
            lastStateBroadcastNanos = 0L;
        }
    }

    private void beginGameLoop() {
        gameRunning  = true;
        gameExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "game-loop");
            t.setDaemon(true);
            return t;
        });
        gameExecutor.scheduleAtFixedRate(() -> {
            if (!gameRunning) { gameExecutor.shutdown(); return; }
            try {
                int winSlot = -2;
                int[] scoresSnapshot = null;
                boolean matchOver = false;

                synchronized (gameLock) {
                    readInputs();
                    game.update();

                    long now = System.nanoTime();
                    if (lastStateBroadcastNanos == 0L || now - lastStateBroadcastNanos >= NETWORK_BROADCAST_INTERVAL_NANOS) {
                        broadcastState();
                        lastStateBroadcastNanos = now;
                    }

                    if (game.isOver()) {
                        gameRunning = false;
                        broadcastState(); // flush final positions/trails before the round result

                        Player winner = game.getWinner();
                        winSlot = (winner != null) ? game.players.indexOf(winner) : -1;
                        if (winSlot >= 0) matchScores[winSlot]++;
                        scoresSnapshot = matchScores.clone();
                        matchOver = winSlot >= 0 && matchScores[winSlot] >= settings.gamesToWin;
                    }
                }

                if (winSlot != -2) {
                    broadcast(NetworkMessage.make(NetworkMessage.ROUND_OVER,
                            String.valueOf(winSlot), scoresString()));
                    if (onRoundOver != null) onRoundOver.accept(winSlot, scoresSnapshot);

                    if (matchOver) {
                        broadcast(NetworkMessage.make(
                                NetworkMessage.MATCH_OVER, String.valueOf(winSlot)));
                        if (onMatchOver != null) onMatchOver.accept(winSlot);
                    } else {
                        gameExecutor.schedule(() -> {
                            buildGame();
                            beginGameLoop();
                        }, 6250, TimeUnit.MILLISECONDS);
                    }

                    gameExecutor.shutdown();
                }
            } catch (Exception e) {
                if (onError != null) onError.accept(e.getMessage());
            }
        }, 0, 16, TimeUnit.MILLISECONDS);
    }

    private void readInputs() {
        Direction d = hostDir;
        if (d != null && !game.players.isEmpty()) {
            game.players.get(0).setDirection(d);
            hostDir = null;
        }
        for (ClientConn c : clients) {
            Direction cd = c.pendingDir;
            if (cd != null && c.slot < game.players.size()) {
                game.players.get(c.slot).setDirection(cd);
                c.pendingDir = null;
            }
        }
    }

    private void broadcastState() {
        StringBuilder sb = new StringBuilder();
        List<Player> players = game.players;

        for (int i = 0; i < players.size(); i++) {
            if (i > 0) sb.append(';');

            Player p = players.get(i);
            List<Point> all = p.trail.points;
            int from = Math.min(trailSentCount[i], all.size());

            sb.append(i).append(':')
              .append(p.x).append(':')
              .append(p.y).append(':')
              .append(p.alive ? '1' : '0').append(':');

            for (int j = from; j < all.size(); j++) {
                if (j > from) sb.append('~');
                Point pt = all.get(j);
                sb.append(pt.x).append(',').append(pt.y);
            }

            trailSentCount[i] = all.size();
        }

        broadcast(NetworkMessage.make(NetworkMessage.STATE, sb.toString()));
    }

    private void broadcastLobbyState() {
        List<String> filled = new ArrayList<>();
        for (String n : playerNames) if (n != null && !n.isEmpty()) filled.add(n);
        broadcast(NetworkMessage.make(NetworkMessage.LOBBY_STATE, String.join(",", filled)));
    }

    private void broadcast(String msg) {
        for (ClientConn c : clients) c.send(msg);
    }

    private String scoresString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matchScores.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(matchScores[i]);
        }
        return sb.toString();
    }

    private void closeConn(ClientConn c) {
        try { c.socket.close(); } catch (IOException ignored) {}
    }
}