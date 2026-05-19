package com.example.controllers;

import com.example.Main;
import com.example.models.Direction;
import com.example.models.LobbySettings;
import com.example.models.Player;
import com.example.network.GameClient;
import com.example.network.GameServer;
import com.example.network.NetworkMessage;
import com.example.models.Point;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.*;

public class MultiplayerGameScreen {

    private static final Color[] PLAYER_COLORS = {
        Color.CYAN, Color.LIMEGREEN, Color.ORANGERED, Color.MEDIUMPURPLE
    };

    private final Main          app;
    private final LobbySettings settings;
    private final String        myName;

    private final GameServer server;
    private final GameClient client;
    private final int        mySlot;

    private Canvas          trailCanvas;
    private Canvas          canvas;
    private GraphicsContext trailGc;
    private GraphicsContext gc;
    private AnimationTimer  renderLoop;

    private final Map<Integer, List<double[]>> pendingClientTrailPoints = new HashMap<>();
    private final Map<Integer, NetworkMessage.PlayerSnapshot> latestHeads = new HashMap<>();
    private int[] serverTrailDrawCount;

    private int[]   scores;
    private boolean localPlayerDead = false;
    private boolean roundTransitioning = false;
    private boolean lastLocalAlive = true;
    private boolean localDeathSplashShown = false;

    private boolean splashVisible = false;
    private String  splashTitle = "";
    private String  splashSubtitle = "";
    private Color   splashColor = Color.WHITE;
    private PauseTransition splashTimer;
    private PauseTransition nextRoundTimer;

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
        trailCanvas = new Canvas(settings.gridWidth, settings.gridHeight);
        canvas      = new Canvas(settings.gridWidth, settings.gridHeight);
        trailGc     = trailCanvas.getGraphicsContext2D();
        gc          = canvas.getGraphicsContext2D();
        clearTrailCanvas();

        Pane      canvasPane = new Pane(trailCanvas, canvas);
        StackPane root       = new StackPane(canvasPane);

        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> handleKey(e.getCode()));

        if (client != null) {
            client.onStateUpdate = snaps -> Platform.runLater(() -> applyClientSnapshots(snaps));
            client.onRoundOver = (winSlot, sc) -> Platform.runLater(() -> {
                scores = sc;
                roundTransitioning = true;
                startRoundResolvedSplash(winSlot, sc);
            });
            client.onMatchOver = winSlot -> Platform.runLater(() -> showMatchOver(winSlot));
            client.onPlayerDisconnected = name -> Platform.runLater(() -> showDisconnectNotice(name));
        } else {
            server.onRoundOver = (winSlot, sc) -> Platform.runLater(() -> {
                scores = sc;
                roundTransitioning = true;
                startRoundResolvedSplash(winSlot, sc);
            });
            server.onMatchOver = winSlot -> Platform.runLater(() -> showMatchOver(winSlot));
            server.onPlayerDisconnected = name -> Platform.runLater(() -> showDisconnectNotice(name));
        }

        startRenderLoop();
        Platform.runLater(root::requestFocus);
        return root;
    }

    private void startRenderLoop() {
        renderLoop = new AnimationTimer() {
            @Override public void handle(long now) {
                if (client != null) renderSnapshots();
                else                renderServerGame();
            }
        };
        renderLoop.start();
    }

    private void applyClientSnapshots(List<NetworkMessage.PlayerSnapshot> snaps) {
        if (roundTransitioning) return;

        int aliveCount = 0;
        NetworkMessage.PlayerSnapshot local = null;

        for (NetworkMessage.PlayerSnapshot s : snaps) {
            latestHeads.put(s.slot, s);
            if (s.alive) aliveCount++;
            if (s.slot == mySlot) local = s;

            if (s.trailPoints != null && !s.trailPoints.isEmpty()) {
                pendingClientTrailPoints
                        .computeIfAbsent(s.slot, k -> new ArrayList<>())
                        .addAll(s.trailPoints);
            }
        }

        if (local != null) updateLocalLifeState(local.alive, snaps.size(), aliveCount);
    }

    private void renderServerGame() {
        if (roundTransitioning) {
            clearDynamicCanvas();
            drawHud();
            drawMultiplayerOverlay();
            return;
        }
        if (server.getGame() == null) return;

        List<Player> players = new ArrayList<>(server.getGame().players);
        ensureServerTrailDrawCount(players.size());
        drawNewServerTrailPoints(players);

        clearDynamicCanvas();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.alive) drawHead(p.x, p.y, PLAYER_COLORS[i % PLAYER_COLORS.length]);
        }

        if (mySlot < players.size()) {
            int aliveCount = 0;
            for (Player p : players) if (p.alive) aliveCount++;
            updateLocalLifeState(players.get(mySlot).alive, players.size(), aliveCount);
        }

        drawHud();
        drawMultiplayerOverlay();
    }

    private void renderSnapshots() {
        if (roundTransitioning) {
            clearDynamicCanvas();
            drawHud();
            drawMultiplayerOverlay();
            return;
        }
        if (latestHeads.isEmpty()) return;

        drawPendingClientTrailPoints();
        clearDynamicCanvas();

        for (Map.Entry<Integer, NetworkMessage.PlayerSnapshot> entry : latestHeads.entrySet()) {
            int slot = entry.getKey();
            NetworkMessage.PlayerSnapshot s = entry.getValue();
            if (s.alive) drawHead(s.x, s.y, PLAYER_COLORS[slot % PLAYER_COLORS.length]);
        }

        drawHud();
        drawMultiplayerOverlay();
    }

    private void ensureServerTrailDrawCount(int playerCount) {
        if (serverTrailDrawCount != null && serverTrailDrawCount.length == playerCount) return;
        serverTrailDrawCount = new int[playerCount];
        clearTrailCanvas();
    }

    private void drawNewServerTrailPoints(List<Player> players) {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Color c = PLAYER_COLORS[i % PLAYER_COLORS.length];
            int from = Math.min(serverTrailDrawCount[i], p.trail.points.size());
            for (int j = from; j < p.trail.points.size(); j++) {
                Point pt = p.trail.points.get(j);
                drawTrailPoint(trailGc, pt.x, pt.y, c);
            }
            serverTrailDrawCount[i] = p.trail.points.size();
        }
    }

    private void drawPendingClientTrailPoints() {
        for (Map.Entry<Integer, List<double[]>> entry : pendingClientTrailPoints.entrySet()) {
            int slot = entry.getKey();
            Color c = PLAYER_COLORS[slot % PLAYER_COLORS.length];
            List<double[]> pts = entry.getValue();
            for (double[] pt : pts) drawTrailPoint(trailGc, pt[0], pt[1], c);
            pts.clear();
        }
    }

    private void clearTrailCanvas() {
        if (trailGc == null) return;
        trailGc.setFill(Color.BLACK);
        trailGc.fillRect(0, 0, settings.gridWidth, settings.gridHeight);
    }

    private void clearDynamicCanvas() {
        gc.clearRect(0, 0, settings.gridWidth, settings.gridHeight);
    }

    private void drawMultiplayerOverlay() {
        if (splashVisible) {
            drawSplashOverlay(splashTitle, splashSubtitle, splashColor);
        } else if (localPlayerDead && !roundTransitioning) {
            drawSplashOverlay("ELIMINATED", "spectating until the round ends…", Color.web("#a0aec0"));
        }
    }

    private void drawSplashOverlay(String title, String subtitle, Color titleColor) {
        double w = settings.gridWidth;
        double h = settings.gridHeight;
        gc.setFill(Color.web("#000000", 0.68));
        gc.fillRect(0, 0, w, h);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(titleColor);
        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 72));
        gc.fillText(title, w / 2.0, h / 2.0 - 22);
        gc.setFill(Color.web("#a0aec0"));
        gc.setFont(Font.font("Courier New", 20));
        gc.fillText(subtitle, w / 2.0, h / 2.0 + 32);
    }

    private void drawTrailPoint(GraphicsContext targetGc, double x, double y, Color c) {
        targetGc.setFill(c.deriveColor(0, 1, 0.6, 0.8));
        targetGc.fillRect(x - 1, y - 1, 4, 4);
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
            gc.setFont(Font.font("Courier New", 13));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText("P" + (i + 1) + ": " + scores[i] + " wins"
                    + (i == mySlot ? "  ←" : ""), 20, 30 + i * 30);
        }
    }

    private void handleKey(KeyCode code) {
        if (localPlayerDead || roundTransitioning) return;
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

    private void updateLocalLifeState(boolean isAlive, int totalPlayers, int aliveCount) {
        boolean justDied = lastLocalAlive && !isAlive;
        localPlayerDead = !isAlive;
        lastLocalAlive = isAlive;

        if (justDied && !roundTransitioning && !localDeathSplashShown) {
            localDeathSplashShown = true;
            if (totalPlayers > 2 && aliveCount > 1) {
                showTemporarySplash("YOU DIED", "round continues — spectating…", Color.web("#e53e3e"), 1.15);
            }
        }
    }

    private void startRoundResolvedSplash(int winSlot, int[] sc) {
        cancelSplashTimers();
        scores = sc;
        splashVisible = true;

        boolean localWon = winSlot == mySlot;
        boolean draw = winSlot < 0;

        if (localWon) {
            setSplash("YOU WON", scoreLine(sc), Color.web("#68d391"));
            scheduleNextRoundSplash(sc, 1.00);
        } else if (draw) {
            setSplash("DRAW", scoreLine(sc), Color.web("#f6e05e"));
            scheduleNextRoundSplash(sc, 1.00);
        } else if (!localDeathSplashShown) {
            setSplash("YOU DIED", "P" + (winSlot + 1) + " wins this round", Color.web("#e53e3e"));
            scheduleNextRoundSplash(sc, 1.00);
        } else {
            setSplash("NEXT GAME STARTING", scoreLine(sc), Color.web("#63b3ed"));
            finishRoundTransitionAfter(sc, 1.40);
        }
    }

    private void scheduleNextRoundSplash(int[] sc, double delaySeconds) {
        splashTimer = new PauseTransition(Duration.seconds(delaySeconds));
        splashTimer.setOnFinished(e -> {
            setSplash("NEXT GAME STARTING", scoreLine(sc), Color.web("#63b3ed"));
            finishRoundTransitionAfter(sc, 1.25);
        });
        splashTimer.play();
    }

    private void finishRoundTransitionAfter(int[] sc, double delaySeconds) {
        nextRoundTimer = new PauseTransition(Duration.seconds(delaySeconds));
        nextRoundTimer.setOnFinished(e -> resetForNextRound(sc));
        nextRoundTimer.play();
    }

    private void resetForNextRound(int[] sc) {
        scores = sc;
        localPlayerDead = false;
        lastLocalAlive = true;
        localDeathSplashShown = false;
        roundTransitioning = false;
        splashVisible = false;
        pendingClientTrailPoints.clear();
        latestHeads.clear();
        serverTrailDrawCount = null;
        clearTrailCanvas();
        clearDynamicCanvas();
    }

    private void showTemporarySplash(String title, String subtitle, Color color, double seconds) {
        if (splashTimer != null) splashTimer.stop();
        setSplash(title, subtitle, color);
        splashTimer = new PauseTransition(Duration.seconds(seconds));
        splashTimer.setOnFinished(e -> splashVisible = false);
        splashTimer.play();
    }

    private void setSplash(String title, String subtitle, Color color) {
        splashTitle = title;
        splashSubtitle = subtitle;
        splashColor = color;
        splashVisible = true;
    }

    private String scoreLine(int[] sc) {
        StringBuilder scoreLine = new StringBuilder("Score: ");
        for (int i = 0; i < sc.length; i++) {
            if (i > 0) scoreLine.append("  |  ");
            scoreLine.append("P").append(i + 1).append(": ").append(sc[i]);
        }
        return scoreLine.toString();
    }

    private void cancelSplashTimers() {
        if (splashTimer != null) splashTimer.stop();
        if (nextRoundTimer != null) nextRoundTimer.stop();
        splashTimer = null;
        nextRoundTimer = null;
    }

    private void showMatchOver(int winSlot) {
        cancelSplashTimers();
        renderLoop.stop();
        String who = winSlot == mySlot ? "YOU WON THE MATCH!" : "P" + (winSlot + 1) + " WINS THE MATCH!";
        if (server != null) server.close();
        app.showMultiplayerMatchOver(who, scores);
    }

    private void showDisconnectNotice(String name) {
        gc.setFill(Color.web("#e53e3e", 0.9));
        gc.setFont(Font.font("Courier New", 14));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("⚠  " + name + " disconnected", 20, settings.gridHeight - 20);
    }
}
