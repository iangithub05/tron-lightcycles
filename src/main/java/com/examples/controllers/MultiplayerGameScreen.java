package com.example.controllers;

import com.example.Main;
import com.example.models.Direction;
import com.example.network.GameClient;
import com.example.network.GameServer;
import com.example.network.NetworkMessage.PlayerSnapshot;
import com.example.utils.ConfigManager;
import com.example.utils.InputManager;
import com.example.utils.KeyBindings;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Renders a multiplayer game.
 *
 * HOST mode  — drives GameServer; reads local input every frame; renders from server's Game.
 * GUEST mode — drives GameClient; sends input over the socket; renders from received snapshots.
 *
 * Exactly one of {@code server} or {@code client} is non-null.
 */
public class MultiplayerGameScreen {

    private final Main app;

    // exactly one is non-null
    private final GameServer server;
    private final GameClient client;

    private Canvas          canvas;
    private GraphicsContext gc;
    private AnimationTimer  loop;

    // guest-side: latest snapshot received from server
    private volatile List<PlayerSnapshot> latestSnapshot = null;

    // last direction sent this frame — prevents spamming the same direction
    private Direction lastSentDir = null;

    // ── constructors ──────────────────────────────────────────────────────

    /** Host constructor — server is already started and game is running. */
    public MultiplayerGameScreen(Main app, GameServer server) {
        this.app    = app;
        this.server = server;
        this.client = null;
        wireServerCallbacks();
    }

    /** Guest constructor — client is already connected. */
    public MultiplayerGameScreen(Main app, GameClient client) {
        this.app    = app;
        this.server = null;
        this.client = client;
        wireClientCallbacks();
    }

    // ── view ──────────────────────────────────────────────────────────────

    public Pane getView() {
        canvas = new Canvas(ConfigManager.gridWidth, ConfigManager.gridHeight);
        gc     = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);

        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                handleInput();
                render();
            }
        };
        loop.start();

        return root;
    }

    // ── input ─────────────────────────────────────────────────────────────

    private void handleInput() {
        Direction dir = null;
        if (InputManager.isDown(KeyBindings.UP))    dir = Direction.UP;
        if (InputManager.isDown(KeyBindings.DOWN))  dir = Direction.DOWN;
        if (InputManager.isDown(KeyBindings.LEFT))  dir = Direction.LEFT;
        if (InputManager.isDown(KeyBindings.RIGHT)) dir = Direction.RIGHT;

        if (dir != null && dir != lastSentDir) {
            lastSentDir = dir;
            if (server != null) server.queueHostDirection(dir);
            if (client != null) client.sendDirection(dir);
        }
    }

    // ── rendering ─────────────────────────────────────────────────────────

    private void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, ConfigManager.gridWidth, ConfigManager.gridHeight);

        gc.setStroke(Color.web("#1a1a2e"));
        gc.strokeRect(0, 0, ConfigManager.gridWidth, ConfigManager.gridHeight);

        if (server != null) {
            renderFromServer();
        } else {
            renderFromSnapshot();
        }
    }

    /** Host renders directly from the live Game model. */
    private void renderFromServer() {
        if (server.getGame() == null) return;
        List<com.example.models.Player> players = server.getGame().players;
        for (int i = 0; i < players.size(); i++) {
            com.example.models.Player p = players.get(i);
            drawPlayer(i, p.x, p.y, p.alive,
                    p.trail.points.stream()
                            .map(pt -> new double[]{pt.x, pt.y})
                            .toList());
        }
    }

    /** Guest renders from the latest snapshot pushed by the receive thread. */
    private void renderFromSnapshot() {
        List<PlayerSnapshot> snap = latestSnapshot;
        if (snap == null) return;
        for (int i = 0; i < snap.size(); i++) {
            PlayerSnapshot ps = snap.get(i);
            drawPlayer(i, ps.x, ps.y, ps.alive, ps.trailPoints);
        }
    }

    private void drawPlayer(int slot, double x, double y, boolean alive, List<double[]> trail) {
        Color color = ConfigManager.PLAYER_COLORS[slot];

        // Trail
        gc.setFill(color.deriveColor(0, 1, 0.65, 0.85));
        for (double[] pt : trail) {
            gc.fillRect(pt[0] - 1, pt[1] - 1, 4, 4);
        }

        // Head
        if (alive) {
            gc.setFill(color);
            gc.fillOval(x - 3, y - 3, 8, 8);

            gc.setStroke(color.deriveColor(0, 1, 1.5, 0.3));
            gc.setLineWidth(2);
            gc.strokeOval(x - 5, y - 5, 12, 12);
        }
    }

    // ── wiring ────────────────────────────────────────────────────────────

    private void wireServerCallbacks() {
        server.onGameOver = (hostResult, guestResult) ->
                Platform.runLater(() -> {
                    if (loop != null) loop.stop();
                    app.showMultiplayerGameOver(hostResult);
                });

        server.onError = err ->
                Platform.runLater(() -> {
                    if (loop != null) loop.stop();
                    app.showMultiplayerGameOver("Connection lost: " + err);
                });
    }

    private void wireClientCallbacks() {
        client.onStateUpdate = snapshot ->
                latestSnapshot = snapshot; // runs on receiver thread; volatile write is fine

        client.onGameOver = result ->
                Platform.runLater(() -> {
                    if (loop != null) loop.stop();
                    app.showMultiplayerGameOver(result);
                });

        client.onError = err ->
                Platform.runLater(() -> {
                    if (loop != null) loop.stop();
                    app.showMultiplayerGameOver("Connection lost: " + err);
                });
    }
}
