package com.example.network;

import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.GameRules;
import com.example.models.Player;
import com.example.models.Point;
import com.example.network.NetworkMessage.PlayerSnapshot;
import com.example.utils.ConfigManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Runs on the host machine.
 *
 * Lifecycle:
 *  1. {@link #listen(int)} — opens a ServerSocket and waits for one guest.
 *  2. Once connected, fires {@code onGuestConnected}.
 *  3. {@link #startGame()} — builds the Game, starts the tick loop.
 *  4. Sends STATE every tick; receives DIRECTION from both sides.
 *  5. When the game ends, sends GAME_OVER to both and fires {@code onGameOver}.
 */
public class GameServer {

    // ── callbacks (set before calling listen) ─────────────────────────────
    /** Called on the networking thread once a guest socket is accepted. */
    public Consumer<Void>         onGuestConnected;
    /** Called on the networking thread when the match ends. First arg = host result text. */
    public BiConsumer<String, String> onGameOver; // (hostResult, guestResult)
    /** Called when a fatal networking error occurs. */
    public Consumer<String>       onError;

    // ── state ──────────────────────────────────────────────────────────────
    private ServerSocket serverSocket;
    private Socket       guestSocket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;

    private Game   game;
    private Player hostPlayer;   // slot 0
    private Player guestPlayer;  // slot 1

    private final AtomicBoolean running = new AtomicBoolean(false);

    // direction queued by each player's receive-thread; applied on next tick
    private volatile Direction hostQueued  = null;
    private volatile Direction guestQueued = null;

    // ── public API ─────────────────────────────────────────────────────────

    /**
     * Opens a ServerSocket on {@code port} and blocks until a guest connects.
     * Runs on a background thread — call from a Thread or executor.
     */
    public void listen(int port) {
        try {
            serverSocket = new ServerSocket(port);
            guestSocket  = serverSocket.accept(); // blocks until guest arrives

            // ObjectOutputStream MUST be created before ObjectInputStream on both ends
            out = new ObjectOutputStream(guestSocket.getOutputStream());
            out.flush();
            in  = new ObjectInputStream(guestSocket.getInputStream());

            // Tell guest which slot it occupies
            send(NetworkMessage.assign(1));

            // Wait for READY from guest
            NetworkMessage ready = (NetworkMessage) in.readObject();
            if (ready.type != NetworkMessage.Type.READY) {
                error("Unexpected message during handshake: " + ready.type);
                return;
            }

            if (onGuestConnected != null) onGuestConnected.accept(null);

        } catch (Exception e) {
            error("Server listen error: " + e.getMessage());
        }
    }

    /**
     * Builds the game and starts the tick loop.
     * Call this after {@code onGuestConnected} fires (from any thread).
     */
    public void startGame() {
        buildGame();
        running.set(true);

        // Receive loop — reads direction messages from the guest
        Thread receiver = new Thread(this::receiveLoop, "server-receiver");
        receiver.setDaemon(true);
        receiver.start();

        // Tick loop — game logic + broadcast
        Thread ticker = new Thread(this::tickLoop, "server-ticker");
        ticker.setDaemon(true);
        ticker.start();
    }

    /** Queue a direction change for the host player (called from the JavaFX thread). */
    public void queueHostDirection(Direction dir) {
        hostQueued = dir;
    }

    /** The host player, for local rendering. Non-null only after {@link #startGame()}. */
    public Game getGame() {
        return game;
    }

    public Player getHostPlayer()  { return hostPlayer;  }
    public Player getGuestPlayer() { return guestPlayer; }

    /** Cleanly shut down the server. */
    public void close() {
        running.set(false);
        try { if (guestSocket  != null) guestSocket.close();  } catch (IOException ignored) {}
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
    }

    // ── private ────────────────────────────────────────────────────────────

    private void buildGame() {
        GameRules rules = new GameRules();
        rules.gridWidth          = ConfigManager.gridWidth;
        rules.gridHeight         = ConfigManager.gridHeight;
        rules.playerSpeed        = ConfigManager.playerSpeed;
        rules.collisionTolerance = ConfigManager.collisionTolerance;

        game = new Game(rules);

        // Slot 0 — host (CYAN, top-left)
        double[] p0 = ConfigManager.START_POSITIONS[0];
        hostPlayer = new Player(p0[0], p0[1]);
        hostPlayer.direction = ConfigManager.START_DIRECTIONS[0];
        game.addPlayer(hostPlayer);

        // Slot 1 — guest (LIMEGREEN, top-right)
        double[] p1 = ConfigManager.START_POSITIONS[1];
        guestPlayer = new Player(p1[0], p1[1]);
        guestPlayer.direction = ConfigManager.START_DIRECTIONS[1];
        game.addPlayer(guestPlayer);
    }

    private void tickLoop() {
        final long TICK_NS = 16_000_000L; // ~60 fps
        long last = System.nanoTime();

        while (running.get()) {
            long now = System.nanoTime();
            if (now - last < TICK_NS) {
                try { Thread.sleep(1); } catch (InterruptedException ignored) {}
                continue;
            }
            last = now;

            // Apply queued directions
            if (hostQueued  != null) { hostPlayer.setDirection(hostQueued);   hostQueued  = null; }
            if (guestQueued != null) { guestPlayer.setDirection(guestQueued); guestQueued = null; }

            game.update();

            // Build and broadcast state
            List<PlayerSnapshot> snapshots = buildSnapshots();
            send(NetworkMessage.state(snapshots));

            // Check end conditions
            boolean hostAlive  = hostPlayer.alive;
            boolean guestAlive = guestPlayer.alive;

            if (!hostAlive || !guestAlive) {
                running.set(false);
                String hostResult, guestResult;

                if (!hostAlive && !guestAlive) {
                    hostResult  = "Draw! You both crashed.";
                    guestResult = "Draw! You both crashed.";
                } else if (hostAlive) {
                    hostResult  = "You Win!";
                    guestResult = "You Crashed!";
                } else {
                    hostResult  = "You Crashed!";
                    guestResult = "You Win!";
                }

                send(NetworkMessage.gameOver(guestResult)); // send guest their result
                if (onGameOver != null) onGameOver.accept(hostResult, guestResult);
                break;
            }
        }
    }

    private void receiveLoop() {
        while (running.get()) {
            try {
                NetworkMessage msg = (NetworkMessage) in.readObject();
                if (msg.type == NetworkMessage.Type.DIRECTION && msg.playerSlot == 1) {
                    guestQueued = msg.direction;
                }
            } catch (Exception e) {
                if (running.get()) error("Server receive error: " + e.getMessage());
                break;
            }
        }
    }

    private synchronized void send(NetworkMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset(); // prevent stale cached objects for mutable state
        } catch (IOException e) {
            if (running.get()) error("Server send error: " + e.getMessage());
        }
    }

    private List<PlayerSnapshot> buildSnapshots() {
        List<PlayerSnapshot> list = new ArrayList<>();
        for (Player p : game.players) {
            List<double[]> pts = new ArrayList<>();
            for (Point pt : p.trail.points) {
                pts.add(new double[]{pt.x, pt.y});
            }
            list.add(new PlayerSnapshot(p.x, p.y, p.alive, pts));
        }
        return list;
    }

    private void error(String msg) {
        if (onError != null) onError.accept(msg);
    }
}
