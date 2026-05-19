package com.example.controllers;

import com.example.Main;
import com.example.models.Direction;
import com.example.models.LobbySettings;
import com.example.network.GameClient;
import com.example.network.GameServer;
import com.example.network.NetworkMessage;
import com.example.models.GameSnapshot;
import com.example.models.PlayerSnapshot;
import com.example.ui.Theme;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
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

    private final Main app;
    private final LobbySettings settings;
    private final String myName;
    private final GameServer server;
    private final GameClient client;
    private final int mySlot;

    private Canvas trailCanvas;
    private Canvas canvas;
    private GraphicsContext trailGc;
    private GraphicsContext gc;
    private AnimationTimer renderLoop;

    private VBox scoreBox;
    private VBox playerBox;
    private Label roundStatusLabel;
    private Label timerLabel;
    private Label roleLabel;

    private final Map<Integer, List<double[]>> pendingClientTrailPoints = new HashMap<>();
    private final Map<Integer, double[]> lastDrawnTrailPoint = new HashMap<>();
    private final Map<Integer, PlayerSnapshot> latestHeads = new HashMap<>();
    private volatile List<PlayerSnapshot> queuedClientSnapshots;
    private int[] serverTrailDrawCount;
    private String lastSidebarSignature = "";

    private int[] scores;
    private boolean localPlayerDead = false;
    private boolean roundTransitioning = false;
    private boolean lastLocalAlive = true;
    private boolean localDeathSplashShown = false;

    private boolean splashVisible = false;
    private String splashTitle = "";
    private String splashSubtitle = "";
    private Color splashColor = Color.WHITE;
    private PauseTransition splashTimer;
    private PauseTransition nextRoundTimer;

    public MultiplayerGameScreen(Main app, GameServer server, LobbySettings settings, String myName) {
        this.app = app;
        this.server = server;
        this.client = null;
        this.settings = settings;
        this.myName = myName;
        this.mySlot = 0;
    }

    public MultiplayerGameScreen(Main app, GameClient client, LobbySettings settings, String myName) {
        this.app = app;
        this.server = null;
        this.client = client;
        this.settings = settings;
        this.myName = myName;
        this.mySlot = client.getMySlot();
    }

    public StackPane getView() {
        trailCanvas = new Canvas(settings.gridWidth, settings.gridHeight);
        canvas = new Canvas(settings.gridWidth, settings.gridHeight);
        trailGc = trailCanvas.getGraphicsContext2D();
        gc = canvas.getGraphicsContext2D();
        clearTrailCanvas();

        Pane canvasPane = new Pane(trailCanvas, canvas);
        canvasPane.setMaxSize(settings.gridWidth, settings.gridHeight);
        canvasPane.setStyle("-fx-border-color: #5f5f78; -fx-border-width: 3; -fx-background-color: black;");

        VBox sideBar = buildSideBar();
        HBox center = new HBox(10, canvasPane, sideBar);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(10, 18, 10, 18));

        BorderPane screen = new BorderPane();
        screen.setTop(buildTopBar());
        screen.setCenter(center);
        screen.setBottom(UIHelper.createNavigationBar("TRON: LIGHT CYCLES", "QUIT"));
        screen.setBackground(UIHelper.createBackground("/images/background_4.png"));

        StackPane root = new StackPane(screen);
        Theme.apply(root);
        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> handleKey(e.getCode()));

        installNetworkCallbacks();
        startRenderLoop();
        Platform.runLater(root::requestFocus);
        return root;
    }

    private HBox buildTopBar() {
        HBox topBar = UIHelper.createNavigationBar("| MULTIPLAYER GAME", "Welcome, " + myName);
        Label welcome = (Label) topBar.getChildren().get(2);
        welcome.setPadding(new Insets(12, 26, 12, 26));
        welcome.setStyle("-fx-background-color: linear-gradient(to right, #9b2447, #6d1831);"
                + "-fx-background-radius: 12; -fx-border-color: #4d1020; -fx-border-radius: 12;"
                + "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "'; -fx-font-size: 14px;"
                + "-fx-font-weight: bold; -fx-text-fill: white;");
        return topBar;
    }

    private VBox buildSideBar() {
        roleLabel = titleLabel((server != null ? "HOST" : "PLAYER") + "  P" + (mySlot + 1));
        roundStatusLabel = smallLabel("Round active");
        timerLabel = smallLabel("First to " + settings.gamesToWin + " wins");

        scoreBox = new VBox(6);
        playerBox = new VBox(6);
        refreshSidebar(null, null);

        VBox scoresPanel = panel("SCORES", scoreBox, 210, 170);
        VBox playersPanel = panel("PLAYERS", playerBox, 210, 220);
        VBox infoPanel = panel("STATUS", new VBox(6, roleLabel, roundStatusLabel, timerLabel), 210, 110);

        VBox side = new VBox(10, infoPanel, scoresPanel, playersPanel);
        side.setAlignment(Pos.TOP_CENTER);
        side.setPrefWidth(230);
        return side;
    }

    private VBox panel(String title, Pane body, int width, int height) {
        Label t = titleLabel(title);
        VBox box = new VBox(8, t, body);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(12));
        box.setPrefSize(width, height);
        box.setStyle(panelStyle());
        return box;
    }

    private void installNetworkCallbacks() {
        if (client != null) {
            client.onStateUpdate = snaps -> queuedClientSnapshots = snaps;
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
    }

    private void startRenderLoop() {
        renderLoop = new AnimationTimer() {
            @Override public void handle(long now) {
                if (client != null) renderSnapshots();
                else renderServerGame();
            }
        };
        renderLoop.start();
    }

    private void applyClientSnapshots(List<PlayerSnapshot> snaps) {
        if (roundTransitioning) return;

        int aliveCount = 0;
        PlayerSnapshot local = null;

        for (PlayerSnapshot s : snaps) {
            latestHeads.put(s.slot, s);
            if (s.alive) aliveCount++;
            if (s.slot == mySlot) local = s;

            if (s.trailPoints != null && !s.trailPoints.isEmpty()) {
                pendingClientTrailPoints.computeIfAbsent(s.slot, k -> new ArrayList<>()).addAll(s.trailPoints);
            }
        }

        refreshSidebar(snaps, null);
        if (local != null) updateLocalLifeState(local.alive, snaps.size(), aliveCount);
    }

    private void renderServerGame() {
        if (roundTransitioning) {
            clearDynamicCanvas();
            drawMultiplayerOverlay();
            return;
        }

        int playerCount = server.getGamePlayerCount();
        if (playerCount <= 0) return;
        ensureServerTrailDrawCount(playerCount);

        GameSnapshot snapshot = server.getRenderSnapshot(serverTrailDrawCount);
        if (snapshot == null || snapshot.players == null || snapshot.players.isEmpty()) return;

        for (PlayerSnapshot s : snapshot.players) {
            if (s.trailPoints != null && !s.trailPoints.isEmpty()) {
                pendingClientTrailPoints.computeIfAbsent(s.slot, k -> new ArrayList<>()).addAll(s.trailPoints);
            }
        }
        drawPendingClientTrailPoints();

        clearDynamicCanvas();
        int aliveCount = 0;
        PlayerSnapshot local = null;
        for (PlayerSnapshot s : snapshot.players) {
            if (s.alive) {
                aliveCount++;
                drawHead(s.x, s.y, PLAYER_COLORS[s.slot % PLAYER_COLORS.length]);
            }
            if (s.slot == mySlot) local = s;
        }

        refreshSidebar(snapshot.players, null);
        if (local != null) updateLocalLifeState(local.alive, snapshot.players.size(), aliveCount);
        drawMultiplayerOverlay();
    }

    private void renderSnapshots() {
        if (roundTransitioning) {
            clearDynamicCanvas();
            drawMultiplayerOverlay();
            return;
        }

        List<PlayerSnapshot> snaps = queuedClientSnapshots;
        if (snaps != null) {
            queuedClientSnapshots = null;
            applyClientSnapshots(snaps);
        }

        if (latestHeads.isEmpty()) return;

        drawPendingClientTrailPoints();
        clearDynamicCanvas();

        for (Map.Entry<Integer, PlayerSnapshot> entry : latestHeads.entrySet()) {
            int slot = entry.getKey();
            PlayerSnapshot s = entry.getValue();
            if (s.alive) drawHead(s.x, s.y, PLAYER_COLORS[slot % PLAYER_COLORS.length]);
        }

        drawMultiplayerOverlay();
    }

    private void refreshSidebar(List<PlayerSnapshot> snaps, List<?> unusedPlayers) {
        if (scoreBox == null || playerBox == null) return;

        int playerCount = 0;
        if (scores != null) playerCount = scores.length;
        else if (snaps != null) playerCount = snaps.size();
        else playerCount = settings.maxPlayers;

        StringBuilder signature = new StringBuilder();
        signature.append(playerCount).append('|');
        for (int i = 0; i < playerCount; i++) {
            int score = scores != null && i < scores.length ? scores[i] : 0;
            boolean alive = true;
            String name = "P" + (i + 1);

            if (snaps != null) {
                alive = false;
                for (PlayerSnapshot snap : snaps) {
                    if (snap.slot == i) {
                        alive = snap.alive;
                        score = snap.score;
                        if (snap.name != null && !snap.name.isBlank()) name = snap.name;
                        break;
                    }
                }
            }
            signature.append(i).append(':').append(score).append(':')
                    .append(alive ? '1' : '0').append(':').append(name).append(';');
        }

        String sig = signature.toString();
        if (sig.equals(lastSidebarSignature)) return;
        lastSidebarSignature = sig;

        scoreBox.getChildren().clear();
        for (int i = 0; i < playerCount; i++) {
            int score = scores != null && i < scores.length ? scores[i] : 0;
            if (snaps != null) {
                for (PlayerSnapshot snap : snaps) {
                    if (snap.slot == i) { score = snap.score; break; }
                }
            }
            scoreBox.getChildren().add(colorRow("P" + (i + 1), score + " / " + settings.gamesToWin, i, i == mySlot));
        }

        playerBox.getChildren().clear();
        for (int i = 0; i < playerCount; i++) {
            boolean alive = true;
            String name = "P" + (i + 1);
            if (snaps != null) {
                alive = false;
                for (PlayerSnapshot snap : snaps) {
                    if (snap.slot == i) {
                        alive = snap.alive;
                        if (snap.name != null && !snap.name.isBlank()) name = snap.name;
                        break;
                    }
                }
            }
            String status = alive ? "ALIVE" : "DEAD";
            playerBox.getChildren().add(colorRow(name, status + (i == mySlot ? "  YOU" : ""), i, i == mySlot));
        }
    }

    private HBox colorRow(String left, String right, int slot, boolean isMe) {
        Label l = new Label("● " + left);
        l.setStyle("-fx-text-fill: " + toHex(PLAYER_COLORS[slot % PLAYER_COLORS.length]) + "; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        Label r = new Label(right);
        r.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 11px;" + (isMe ? " -fx-font-weight: bold;" : ""));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(6, l, spacer, r);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 7, 5, 7));
        row.setStyle("-fx-background-color: " + (isMe ? "rgba(25,40,78,0.78)" : "rgba(10,15,25,0.55)") + "; -fx-background-radius: 6;");
        return row;
    }

    private void ensureServerTrailDrawCount(int playerCount) {
        if (serverTrailDrawCount != null && serverTrailDrawCount.length == playerCount) return;
        serverTrailDrawCount = new int[playerCount];
        lastDrawnTrailPoint.clear();
        clearTrailCanvas();
    }

    private void drawPendingClientTrailPoints() {
        for (Map.Entry<Integer, List<double[]>> entry : pendingClientTrailPoints.entrySet()) {
            int slot = entry.getKey();
            Color c = PLAYER_COLORS[slot % PLAYER_COLORS.length];
            List<double[]> pts = entry.getValue();
            double[] previous = lastDrawnTrailPoint.get(slot);

            for (double[] current : pts) {
                drawTrailSegment(trailGc, previous, current, c);
                previous = current;
            }

            if (previous != null) lastDrawnTrailPoint.put(slot, previous);
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
            drawSplashOverlay("YOU DIED", "spectating until the round ends...", Color.web("#e53e3e"));
        }
    }

    private void drawSplashOverlay(String title, String subtitle, Color titleColor) {
        double w = settings.gridWidth;
        double h = settings.gridHeight;
        gc.setFill(Color.web("#000000", 0.60));
        gc.fillRect(0, 0, w, h);
        gc.setFill(Color.web("#1b2030", 0.72));
        gc.fillRoundRect(w / 2.0 - 360, h / 2.0 - 145, 720, 280, 18, 18);
        gc.setStroke(Color.web("#000000", 0.85));
        gc.setLineWidth(2);
        gc.strokeRoundRect(w / 2.0 - 360, h / 2.0 - 145, 720, 280, 18, 18);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(titleColor);
        gc.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 46));
        gc.fillText(title, w / 2.0, h / 2.0 - 25);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 13));
        gc.fillText(subtitle, w / 2.0, h / 2.0 + 38);
    }

    private void drawTrailSegment(GraphicsContext targetGc, double[] previous, double[] current, Color c) {
        if (current == null) return;

        targetGc.setStroke(c.deriveColor(0, 1, 0.78, 0.88));
        targetGc.setLineWidth(5);
        targetGc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        if (previous == null || distanceSquared(previous, current) > 90_000) {
            // First point for this player, or a new round/spawn jump.
            targetGc.setFill(c.deriveColor(0, 1, 0.78, 0.88));
            targetGc.fillOval(current[0] - 2.5, current[1] - 2.5, 5, 5);
        } else {
            targetGc.strokeLine(previous[0], previous[1], current[0], current[1]);
        }
    }

    private double distanceSquared(double[] a, double[] b) {
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        return dx * dx + dy * dy;
    }

    private void drawHead(double x, double y, Color c) {
        gc.setFill(c);
        gc.fillOval(x - 3, y - 3, 8, 8);
        gc.setStroke(c.deriveColor(0, 1, 1.4, 0.3));
        gc.setLineWidth(2);
        gc.strokeOval(x - 5, y - 5, 12, 12);
    }

    private void handleKey(KeyCode code) {
        if (localPlayerDead || roundTransitioning) return;
        Direction dir = switch (code) {
            case UP, W -> Direction.UP;
            case DOWN, S -> Direction.DOWN;
            case LEFT, A -> Direction.LEFT;
            case RIGHT, D -> Direction.RIGHT;
            default -> null;
        };
        if (dir == null) return;
        if (server != null) server.queueHostDirection(dir);
        else client.sendDirection(dir);
    }

    private void updateLocalLifeState(boolean isAlive, int totalPlayers, int aliveCount) {
        boolean justDied = lastLocalAlive && !isAlive;
        localPlayerDead = !isAlive;
        lastLocalAlive = isAlive;

        if (justDied && !roundTransitioning && !localDeathSplashShown) {
            localDeathSplashShown = true;
            if (totalPlayers > 2 && aliveCount > 1) {
                showTemporarySplash("YOU DIED", "round continues — spectating...", Color.web("#e53e3e"), 1.25);
                if (roundStatusLabel != null) roundStatusLabel.setText("Eliminated - spectating");
            }
        }
    }

    private void startRoundResolvedSplash(int winSlot, int[] sc) {
        cancelSplashTimers();
        scores = sc;
        splashVisible = true;
        refreshSidebar(null, null);

        boolean localWonRound = winSlot == mySlot;
        boolean draw = winSlot < 0;

        if (localWonRound) {
            setSplash("YOU WON THE ROUND", scoreLine(sc), Color.web("#68d391"));
        } else if (draw) {
            setSplash("ROUND DRAW", scoreLine(sc), Color.web("#f6e05e"));
        } else {
            setSplash(localDeathSplashShown ? "ROUND OVER" : "YOU DIED", "P" + (winSlot + 1) + " won the round", Color.web("#e53e3e"));
        }

        scheduleNextRoundCountdown(sc, 1.25);
    }

    private void scheduleNextRoundCountdown(int[] sc, double firstDelay) {
        splashTimer = new PauseTransition(Duration.seconds(firstDelay));
        splashTimer.setOnFinished(e -> runCountdown(sc, 5));
        splashTimer.play();
    }

    private void runCountdown(int[] sc, int secondsLeft) {
        if (secondsLeft <= 0) {
            resetForNextRound(sc);
            return;
        }
        setSplash("NEXT GAME STARTING IN", String.valueOf(secondsLeft), Color.web("#63b3ed"));
        if (timerLabel != null) timerLabel.setText("Next round in " + secondsLeft + "...");
        nextRoundTimer = new PauseTransition(Duration.seconds(1));
        nextRoundTimer.setOnFinished(e -> runCountdown(sc, secondsLeft - 1));
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
        lastDrawnTrailPoint.clear();
        latestHeads.clear();
        serverTrailDrawCount = null;
        lastSidebarSignature = "";
        if (roundStatusLabel != null) roundStatusLabel.setText("Round active");
        if (timerLabel != null) timerLabel.setText("First to " + settings.gamesToWin + " wins");
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
        StringBuilder line = new StringBuilder("Score: ");
        for (int i = 0; i < sc.length; i++) {
            if (i > 0) line.append("  |  ");
            line.append("P").append(i + 1).append(": ").append(sc[i]);
        }
        return line.toString();
    }

    private void cancelSplashTimers() {
        if (splashTimer != null) splashTimer.stop();
        if (nextRoundTimer != null) nextRoundTimer.stop();
        splashTimer = null;
        nextRoundTimer = null;
    }

    private void showMatchOver(int winSlot) {
        cancelSplashTimers();
        if (renderLoop != null) renderLoop.stop();
        String result = winSlot == mySlot ? "YOU WON THE GAME" : "YOU LOST";
        if (server != null) server.close();
        app.showMultiplayerMatchOver(result, scores);
    }

    private void showDisconnectNotice(String name) {
        if (gc == null) return;
        gc.setFill(Color.web("#e53e3e", 0.9));
        gc.setFont(Font.font("Courier New", 14));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("⚠  " + name + " disconnected", 20, settings.gridHeight - 20);
    }

    private static Label titleLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.WHITE);
        l.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 11));
        return l;
    }

    private static Label smallLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#c7d2fe"));
        l.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        return l;
    }

    private static String panelStyle() {
        return "-fx-background-color: rgba(20, 22, 35, 0.74);"
             + "-fx-background-radius: 8; -fx-border-color: black; -fx-border-radius: 8; -fx-border-width: 1;";
    }

    private static String toHex(Color c) {
        return String.format("#%02x%02x%02x", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }
}
