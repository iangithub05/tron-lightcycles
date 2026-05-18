package com.example.controllers;

import com.example.Main;
import com.example.models.Direction;
import com.example.models.LobbySettings;
import com.example.models.Player;
import com.example.network.GameClient;
import com.example.network.GameServer;
import com.example.network.NetworkMessage;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;

import java.util.List;

public class MultiplayerGameScreen {

    private static final Color[] PLAYER_COLORS = {
        Color.CYAN, Color.LIMEGREEN, Color.ORANGERED, Color.MEDIUMPURPLE
    };

    private final Main         app;
    private final LobbySettings settings;
    private final String       myName;

    private final GameServer   server;
    private final GameClient   client;
    private final int          mySlot;

    private Canvas       canvas;
    private GraphicsContext gc;
    private AnimationTimer renderLoop;

    private volatile List<NetworkMessage.PlayerSnapshot> latestSnapshots;
    private int[] scores;

    public MultiplayerGameScreen(Main app, GameServer server, LobbySettings settings, String myName) {
        this.app = app; this.server = server; this.client = null;
        this.settings = settings; this.myName = myName;
        this.mySlot = 0;
    }

    public MultiplayerGameScreen(Main app, GameClient client,
                                 LobbySettings settings, String myName) {
        this.app = app; this.server = null; this.client = client;
        this.settings = settings; this.myName = myName;
        this.mySlot = client.getMySlot();
    }

    public StackPane getView() {
        canvas = new Canvas(settings.gridWidth, settings.gridHeight);
        gc     = canvas.getGraphicsContext2D();

        Pane canvasPane = new Pane(canvas);
        StackPane root  = new StackPane(canvasPane);

        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> handleKey(e.getCode()));

        if (client != null) {
            client.onStateUpdate = snaps -> {
                latestSnapshots = snaps;
            };
            client.onRoundOver = (winSlot, sc) -> Platform.runLater(() -> {
                scores = sc;
                showRoundSplash(winSlot, sc);
            });
            client.onMatchOver = winSlot -> Platform.runLater(() ->
                showMatchOver(winSlot));
            client.onPlayerDisconnected = name -> Platform.runLater(() ->
                showDisconnectNotice(name));
        } else {
            server.onRoundOver = (winSlot, sc) -> Platform.runLater(() -> {
                scores = sc;
                showRoundSplash(winSlot, sc);
            });
            server.onMatchOver = winSlot -> Platform.runLater(() ->
                showMatchOver(winSlot));
            server.onPlayerDisconnected = name -> Platform.runLater(() ->
                showDisconnectNotice(name));
        }

        startRenderLoop();
        Platform.runLater(root::requestFocus);
        return root;
    }


    private void startRenderLoop() {
        renderLoop = new AnimationTimer() {
            @Override public void handle(long now) {
                if (client != null) {
                    renderSnapshots();
                } else {
                    renderServerGame();
                }
            }
        };
        renderLoop.start();
    }

    private void renderServerGame() {
        if (server.getGame() == null) return;
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, settings.gridWidth, settings.gridHeight);

        List<Player> players = server.getGame().players;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Color  c = PLAYER_COLORS[i % PLAYER_COLORS.length];
            drawTrail(p.trail.points.stream()
                .map(pt -> new double[]{pt.x, pt.y})
                .toList(), c);
            if (p.alive) drawHead(p.x, p.y, c);
        }
        drawHud();
    }

    private void renderSnapshots() {
        List<NetworkMessage.PlayerSnapshot> snaps = latestSnapshots;
        if (snaps == null) return;
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, settings.gridWidth, settings.gridHeight);
        for (NetworkMessage.PlayerSnapshot s : snaps) {
            Color c = PLAYER_COLORS[s.slot % PLAYER_COLORS.length];
            drawTrail(s.trailPoints, c);
            if (s.alive) drawHead(s.x, s.y, c);
        }
        drawHud();
    }

    private void drawTrail(List<double[]> pts, Color c) {
        if (pts == null) return;
        gc.setFill(c.deriveColor(0, 1, 0.6, 0.8));
        for (double[] pt : pts) gc.fillRect(pt[0] - 1, pt[1] - 1, 4, 4);
    }

    private void drawHead(double x, double y, Color c) {
        gc.setFill(c);
        gc.fillOval(x - 3, y - 3, 8, 8);
        gc.setStroke(c.deriveColor(0, 1, 1.4, 0.3));
        gc.setLineWidth(2);
        gc.strokeOval(x - 5, y - 5, 12, 12);
    }

    private void drawHud() {
        if (scores == null) return;
        gc.setFill(Color.web("#0e0e1a", 0.7));
        gc.fillRoundRect(10, 10, 200, 30 * scores.length + 10, 4, 4);
        for (int i = 0; i < scores.length; i++) {
            Color c = PLAYER_COLORS[i % PLAYER_COLORS.length];
            gc.setFill(c);
            gc.setFont(javafx.scene.text.Font.font("Courier New", 13));
            gc.fillText("P" + (i + 1) + ": " + scores[i] + " wins"
                    + (i == mySlot ? "  ←" : ""), 20, 30 + i * 30);
        }
    }

    private void handleKey(KeyCode code) {
        Direction dir = switch (code) {
            case UP, W    -> Direction.UP;
            case DOWN, S  -> Direction.DOWN;
            case LEFT, A  -> Direction.LEFT;
            case RIGHT, D -> Direction.RIGHT;
            default -> null;
        };
        if (dir == null) return;
        if (server != null) server.queueHostDirection(dir);
        else                client.sendDirection(dir);
    }

    private void showRoundSplash(int winSlot, int[] sc) {
        renderLoop.stop();
        String who = winSlot == mySlot ? "YOU WIN!" : (winSlot < 0 ? "DRAW!" : "P" + (winSlot+1) + " WINS!");
        gc.setFill(Color.web("#0e0e1a", 0.85));
        gc.fillRect(0, 0, settings.gridWidth, settings.gridHeight);
        gc.setFill(Color.web("#63b3ed"));
        gc.setFont(javafx.scene.text.Font.font("Courier New", javafx.scene.text.FontWeight.BOLD, 56));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(who, settings.gridWidth / 2.0, settings.gridHeight / 2.0 - 30);
        gc.setFill(Color.web("#a0aec0"));
        gc.setFont(javafx.scene.text.Font.font("Courier New", 20));
        StringBuilder scoreLine = new StringBuilder("Score: ");
        for (int i = 0; i < sc.length; i++) {
            if (i > 0) scoreLine.append("  |  ");
            scoreLine.append("P").append(i+1).append(": ").append(sc[i]);
        }
        gc.fillText(scoreLine.toString(), settings.gridWidth / 2.0,
                settings.gridHeight / 2.0 + 30);
        gc.setFont(javafx.scene.text.Font.font("Courier New", 14));
        gc.fillText("next round starting…", settings.gridWidth / 2.0, settings.gridHeight / 2.0 + 70);
        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
        pt.setOnFinished(e -> { scores = sc; renderLoop.start(); });
        pt.play();
    }

    private void showMatchOver(int winSlot) {
        renderLoop.stop();
        String who = winSlot == mySlot ? "YOU WON THE MATCH!" : "P" + (winSlot+1) + " WINS THE MATCH!";
        if (server != null) server.close();
        app.showMultiplayerMatchOver(who, scores);
    }

    private void showDisconnectNotice(String name) {
        gc.setFill(Color.web("#e53e3e", 0.9));
        gc.setFont(javafx.scene.text.Font.font("Courier New", 14));
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        gc.fillText("⚠  " + name + " disconnected", 20, settings.gridHeight - 20);
    }
}
