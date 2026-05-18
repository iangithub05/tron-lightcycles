package com.example.controllers;

import com.example.Main;
import com.example.models.Direction;
import com.example.models.LobbySettings;
import com.example.models.Player;
import com.example.network.GameClient;
import com.example.network.GameServer;
import com.example.network.NetworkMessage;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.List;

public class MultiplayerGameScreen {

    private static final Color[] PLAYER_COLORS = {
        Color.CYAN, Color.LIMEGREEN, Color.ORANGERED, Color.MEDIUMPURPLE
    };

    private final Main app;
    private final GameServer server;
    private final GameClient client;
    private final LobbySettings settings;
    private final String myName;
    private final int mySlot;

    private Canvas          canvas;
    private GraphicsContext gc;
    private AnimationTimer  renderLoop;
    private volatile List<NetworkMessage.PlayerSnapshot> latestSnapshots = null;

    private volatile int[]   scores         = null;
    private volatile boolean localPlayerDead = false;
    public MultiplayerGameScreen(Main app, GameServer server,
                                 LobbySettings settings, String myName) {
        this.app      = app;
        this.server   = server;
        this.client   = null;
        this.settings = settings;
        this.myName   = myName;
        this.mySlot   = 0;
    }

    public MultiplayerGameScreen(Main app, GameClient client, LobbySettings settings, String myName) {
        this.app      = app;
        this.server   = null;
        this.client   = client;
        this.settings = settings;
        this.myName   = myName;
        this.mySlot   = client.getMySlot();
    }


    public StackPane getView() {
        canvas = new Canvas(settings.gridWidth, settings.gridHeight);
        gc     = canvas.getGraphicsContext2D();

        Pane       canvasPane = new Pane(canvas);
        StackPane  root       = new StackPane(canvasPane);

        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> handleKey(e.getCode()));

        wireCallbacks();
        startRenderLoop();

        Platform.runLater(root::requestFocus);
        return root;
    }


    private void wireCallbacks() {
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
    }


    private void startRenderLoop() {
        renderLoop = new AnimationTimer() {
            @Override public void handle(long now) {
                if (server != null) renderFromServer();
                else                renderFromSnapshots();
            }
        };
        renderLoop.start();
    }

    private void renderFromServer() {
        if (server.getGame() == null) return;

        clearCanvas();

        List<Player> players = server.getGame().players;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Color  c = colorFor(i);
            drawTrail(p.trail.points.stream()
                          .map(pt -> new double[]{pt.x, pt.y})
                          .toList(), c);
            if (p.alive) drawHead(p.x, p.y, c);
        }

        drawHud();

        if (mySlot < players.size() && !players.get(mySlot).alive) {
            localPlayerDead = true;
        }
        if (localPlayerDead) drawDeadOverlay();
    }

    private void renderFromSnapshots() {
        List<NetworkMessage.PlayerSnapshot> snaps = latestSnapshots;
        if (snaps == null) return;

        clearCanvas();

        for (NetworkMessage.PlayerSnapshot s : snaps) {
            Color c = colorFor(s.slot);
            drawTrail(s.trailPoints, c);
            if (s.alive) drawHead(s.x, s.y, c);
        }

        drawHud();

        for (NetworkMessage.PlayerSnapshot s : snaps) {
            if (s.slot == mySlot && !s.alive) {
                localPlayerDead = true;
                break;
            }
        }
        if (localPlayerDead) drawDeadOverlay();
    }

    private void clearCanvas() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, settings.gridWidth, settings.gridHeight);
    }

    private void drawTrail(List<double[]> pts, Color c) {
        if (pts == null || pts.isEmpty()) return;
        gc.setFill(c.deriveColor(0, 1, 0.6, 0.8));
        for (double[] pt : pts) {
            gc.fillRect(pt[0] - 1, pt[1] - 1, 4, 4);
        }
    }

    private void drawHead(double x, double y, Color c) {
        gc.setFill(c);
        gc.fillOval(x - 3, y - 3, 8, 8);
        gc.setStroke(c.deriveColor(0, 1, 1.4, 0.3));
        gc.setLineWidth(2);
        gc.strokeOval(x - 5, y - 5, 12, 12);
    }

    private void drawHud() {
        int[] sc = scores;
        if (sc == null) return;

        gc.setFill(Color.web("#0e0e1a", 0.7));
        gc.fillRoundRect(10, 10, 200, 30 * sc.length + 10, 4, 4);

        for (int i = 0; i < sc.length; i++) {
            gc.setFill(colorFor(i));
            gc.setFont(Font.font("Courier New", 13));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText("P" + (i + 1) + ": " + sc[i] + " wins"
                    + (i == mySlot ? "  ←" : ""), 20, 30 + i * 30);
        }
    }

    private void drawDeadOverlay() {
        double w = settings.gridWidth;
        double h = settings.gridHeight;

        gc.setFill(Color.web("#0a0a14", 0.72));
        gc.fillRect(0, 0, w, h);

        gc.setFill(Color.web("#fc8181"));
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 72));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("YOU DIED", w / 2.0, h / 2.0 - 20);

        gc.setFill(Color.web("#a0aec0"));
        gc.setFont(Font.font("Courier New", 20));
        gc.fillText("spectating…", w / 2.0, h / 2.0 + 30);
    }


    private void handleKey(KeyCode code) {
        if (localPlayerDead) return;

        Direction dir = switch (code) {
            case UP,    W -> Direction.UP;
            case DOWN,  S -> Direction.DOWN;
            case LEFT,  A -> Direction.LEFT;
            case RIGHT, D -> Direction.RIGHT;
            default -> null;
        };
        if (dir == null) return;

        if (server != null) server.queueHostDirection(dir);
        else                client.sendDirection(dir);
    }


    private void showRoundSplash(int winSlot, int[] sc) {
        localPlayerDead = false;
        renderLoop.stop();

        String who = winSlot < 0        ? "DRAW!"
                   : winSlot == mySlot  ? "YOU WIN!"
                                        : "P" + (winSlot + 1) + " WINS!";

        double w = settings.gridWidth;
        double h = settings.gridHeight;

        gc.setFill(Color.web("#0e0e1a", 0.85));
        gc.fillRect(0, 0, w, h);

        gc.setFill(Color.web("#63b3ed"));
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 56));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(who, w / 2.0, h / 2.0 - 30);

        gc.setFill(Color.web("#a0aec0"));
        gc.setFont(Font.font("Courier New", 20));

        StringBuilder scoreLine = new StringBuilder("Score: ");
        for (int i = 0; i < sc.length; i++) {
            if (i > 0) scoreLine.append("  |  ");
            scoreLine.append("P").append(i + 1).append(": ").append(sc[i]);
        }
        gc.fillText(scoreLine.toString(), w / 2.0, h / 2.0 + 30);

        gc.setFont(Font.font("Courier New", 14));
        gc.fillText("next round starting…", w / 2.0, h / 2.0 + 70);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            latestSnapshots = null;
            renderLoop.start();
        });
        pause.play();
    }

    private void showMatchOver(int winSlot) {
        renderLoop.stop();
        String who = winSlot == mySlot
                ? "YOU WON THE MATCH!"
                : "P" + (winSlot + 1) + " WINS THE MATCH!";
        if (server != null) server.close();
        app.showMultiplayerMatchOver(who, scores);
    }

    private void showDisconnectNotice(String name) {
        gc.setFill(Color.web("#e53e3e", 0.9));
        gc.setFont(Font.font("Courier New", 14));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("⚠  " + name + " disconnected",
                20, settings.gridHeight - 20);
    }


    private static Color colorFor(int slot) {
        return PLAYER_COLORS[slot % PLAYER_COLORS.length];
    }
}