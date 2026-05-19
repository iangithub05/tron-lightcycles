package com.example.network;

import com.example.models.Direction;
import com.example.models.GameSnapshot;
import com.example.models.LobbySettings;
import com.example.models.MultiplayerGame;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class GameServer {

    private static final int GAME_TICK_MS = 16;
    private static final int NETWORK_SEND_MS = 33;
    private static final int NEXT_ROUND_DELAY_MS = 6250;

    public Consumer<Integer>          onPlayerCountChanged;
    public BiConsumer<String, String> onChatMessage;
    public Consumer<String>           onPlayerDisconnected;
    public Consumer<String>           onError;
    public BiConsumer<Integer, int[]> onRoundOver;
    public Consumer<Integer>          onMatchOver;

    private LobbySettings settings;
    private MultiplayerGame multiplayerGame;
    private volatile GameSnapshot latestSnapshot;

    private ServerSocket serverSocket;
    private final List<ClientConn> clients = new CopyOnWriteArrayList<>();
    private final List<String> playerNames = new CopyOnWriteArrayList<>();

    private LanDiscovery discovery;
    private String roomCode;
    private String hostName;

    private ScheduledExecutorService gameExecutor;
    private volatile boolean gameRunning;
    private volatile Direction hostDir = null;
    private long lastNetworkSendNanos = 0L;

    private class ClientConn {
        final int slot;
        final Socket socket;
        final BufferedReader in;
        final PrintWriter out;
        volatile Direction pendingDir;

        ClientConn(int slot, Socket socket) throws IOException {
            this.slot = slot;
            this.socket = socket;
            this.socket.setTcpNoDelay(true);
            this.socket.setKeepAlive(true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        }

        void send(String msg) {
            out.println(msg);
        }
    }

    public void listen(int port, LobbySettings settings, String hostName) {
        this.settings = settings;
        this.hostName = hostName;

        playerNames.clear();
        playerNames.add(hostName);

        roomCode = RoomCode.encode(RoomCode.getLocalIp(), port);
        discovery = new LanDiscovery();
        discovery.startResponder(roomCode, hostName, 1, settings.maxPlayers);

        Thread acceptThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
                serverSocket.setReuseAddress(true);
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    int slot = clients.size() + 1;
                    if (slot >= settings.maxPlayers) {
                        socket.close();
                        continue;
                    }
                    ClientConn conn = new ClientConn(slot, socket);
                    clients.add(conn);
                    startClientReader(conn);
                }
            } catch (IOException e) {
                if (serverSocket != null && !serverSocket.isClosed() && onError != null) {
                    onError.accept(e.getMessage());
                }
            }
        }, "server-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getLocalIp() {
        return RoomCode.getLocalIp();
    }

    public LobbySettings getSettings() {
        return settings;
    }

    public List<String> getPlayerNames() {
        return new ArrayList<>(playerNames);
    }

    public GameSnapshot getLatestSnapshot() {
        GameSnapshot snapshot = latestSnapshot;
        return snapshot == null ? null : snapshot.copyWithoutTrail();
    }


    public GameSnapshot getRenderSnapshot(int[] trailDrawCount) {
        MultiplayerGame game = multiplayerGame;
        return game == null ? null : game.snapshotUsingTrailCounters(trailDrawCount);
    }

    public int getGamePlayerCount() {
        MultiplayerGame game = multiplayerGame;
        return game == null ? 0 : game.getPlayerCount();
    }
    public void updateSettings(LobbySettings s) {
        this.settings = s;
        broadcast(NetworkMessage.make(NetworkMessage.SETTINGS, s.encode()));
        if (discovery != null) {
            discovery.updatePlayerCount(roomCode, hostName, clients.size() + 1, s.maxPlayers);
        }
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

    public void queueHostDirection(Direction dir) {
        hostDir = dir;
    }

    public void close() {
        gameRunning = false;
        if (gameExecutor != null) gameExecutor.shutdownNow();
        if (discovery != null) discovery.stopResponder();
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
                if (onPlayerCountChanged != null) onPlayerCountChanged.accept(clients.size() + 1);
                if (discovery != null) {
                    discovery.updatePlayerCount(roomCode, hostName,
                            clients.size() + 1, settings.maxPlayers);
                }
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
        String name = conn.slot < playerNames.size() ? playerNames.get(conn.slot) : "PLAYER";
        clients.remove(conn);
        closeConn(conn);
        broadcast(NetworkMessage.make(NetworkMessage.DISCONNECT, name));
        if (onPlayerDisconnected != null) onPlayerDisconnected.accept(name);
        broadcastLobbyState();
        if (onPlayerCountChanged != null) onPlayerCountChanged.accept(clients.size() + 1);
    }

    private void buildGame() {
        multiplayerGame = new MultiplayerGame(settings, getFilledPlayerNames());
        latestSnapshot = multiplayerGame.snapshot(false).copyWithoutTrail();
        lastNetworkSendNanos = 0L;
    }

    private List<String> getFilledPlayerNames() {
        List<String> filled = new ArrayList<>();
        for (String name : playerNames) {
            if (name != null && !name.isEmpty()) filled.add(name);
        }
        return filled;
    }

    private void beginGameLoop() {
        gameRunning = true;
        gameExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "game-loop");
            t.setDaemon(true);
            return t;
        });

        gameExecutor.scheduleAtFixedRate(() -> {
            if (!gameRunning) {
                gameExecutor.shutdown();
                return;
            }

            try {
                readInputs();
                multiplayerGame.update();
                broadcastStateIfDue();

                if (multiplayerGame.isRoundOver()) {
                    finishRound();
                }
            } catch (Exception e) {
                if (onError != null) onError.accept(e.getMessage());
            }
        }, 0, GAME_TICK_MS, TimeUnit.MILLISECONDS);
    }

    private void readInputs() {
        Direction d = hostDir;
        if (d != null) {
            multiplayerGame.applyDirection(0, d);
            hostDir = null;
        }

        for (ClientConn c : clients) {
            Direction cd = c.pendingDir;
            if (cd != null) {
                multiplayerGame.applyDirection(c.slot, cd);
                c.pendingDir = null;
            }
        }
    }

    private void broadcastStateIfDue() {
        long now = System.nanoTime();
        long interval = TimeUnit.MILLISECONDS.toNanos(NETWORK_SEND_MS);
        if (now - lastNetworkSendNanos < interval) return;

        GameSnapshot snapshot = multiplayerGame.snapshot(true);
        latestSnapshot = snapshot.copyWithoutTrail();
        broadcast(NetworkMessage.make(NetworkMessage.STATE, NetworkMessage.encodeSnapshot(snapshot)));
        lastNetworkSendNanos = now;
    }

    private void finishRound() {
        gameRunning = false;

        int winSlot = multiplayerGame.getRoundWinnerSlot();
        int[] scores = multiplayerGame.getScores();

        broadcast(NetworkMessage.make(NetworkMessage.ROUND_OVER,
                String.valueOf(winSlot), scoresString(scores)));
        if (onRoundOver != null) onRoundOver.accept(winSlot, scores.clone());

        if (multiplayerGame.isMatchOver()) {
            int matchWinner = multiplayerGame.getMatchWinnerSlot();
            broadcast(NetworkMessage.make(NetworkMessage.MATCH_OVER, String.valueOf(matchWinner)));
            if (onMatchOver != null) onMatchOver.accept(matchWinner);
        } else {
            gameExecutor.schedule(() -> {
                multiplayerGame.resetRound(multiplayerGame.getPlayerCount());
                latestSnapshot = multiplayerGame.snapshot(false).copyWithoutTrail();
                lastNetworkSendNanos = 0L;
                beginGameLoop();
            }, NEXT_ROUND_DELAY_MS, TimeUnit.MILLISECONDS);
        }

        gameExecutor.shutdown();
    }

    private void broadcastLobbyState() {
        broadcast(NetworkMessage.make(NetworkMessage.LOBBY_STATE, String.join(",", getFilledPlayerNames())));
    }

    private void broadcast(String msg) {
        for (ClientConn c : clients) c.send(msg);
    }

    private String scoresString(int[] scores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scores.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(scores[i]);
        }
        return sb.toString();
    }

    private void closeConn(ClientConn c) {
        try { c.socket.close(); } catch (IOException ignored) {}
    }
}
