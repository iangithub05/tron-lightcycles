package com.example.controllers;

import com.example.Main;
import com.example.models.Difficulty;
import com.example.models.LobbySettings;
import com.example.network.GameServer;
import com.example.network.RoomCode;
import com.example.ui.Theme;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class HostLobbyScreen {

    private static final int DEFAULT_PORT = 5555;

    private final Main       app;
    private final GameServer server = new GameServer();
    private final LobbySettings settings = new LobbySettings();

    private Label statusLabel;
    private Label roomCodeLabel;
    private Label ipLabel;
    private VBox playerList;
    private VBox chatMessages;
    private ScrollPane chatScroll;
    private TextField chatInput;
    private Label startBtn_label;
    private Button startBtn;
    private int connectedCount = 1;

    private Spinner<Integer> maxPlayersSpinner;
    private Spinner<Integer> gamesToWinSpinner;
    private Spinner<Integer> timeLimitSpinner;
    private ComboBox<String> difficultyBox;
    private ComboBox<String> gridSizeBox;

    public HostLobbyScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        Label nav   = new Label("MAIN MENU  /  MULTIPLAYER  /  HOST A GAME");
        nav.getStyleClass().add("nav-title");

        Label title = new Label("HOST A GAME");
        title.getStyleClass().add("game-title");

        Label subtitle = new Label("configure, invite friends, start when ready");
        subtitle.getStyleClass().add("screen-subtitle");

        roomCodeLabel = new Label("─ ─ ─ ─ ─ ─");
        roomCodeLabel.setStyle(
            "-fx-text-fill: #63b3ed; -fx-font-size: 32px; -fx-font-weight: bold;"
            + " -fx-font-family: 'Courier New', monospace; -fx-letter-spacing: 6px;");

        ipLabel = new Label("starting server…");
        ipLabel.getStyleClass().add("tron-label-dim");

        Button copyBtn = new Button("⎘  COPY CODE");
        copyBtn.getStyleClass().add("tron-btn-secondary");
        copyBtn.setStyle("-fx-pref-width: 200px; -fx-min-width: 200px;");
        copyBtn.setOnAction(e -> {
            javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
            cc.putString(roomCodeLabel.getText());
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(cc);
            copyBtn.setText("✓  COPIED!");
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(
                javafx.util.Duration.seconds(2));
            pt.setOnFinished(ev -> copyBtn.setText("⎘  COPY CODE"));
            pt.play();
        });

        VBox codeBox = new VBox(4, roomCodeLabel, ipLabel, copyBtn);
        codeBox.setAlignment(Pos.TOP_LEFT);
        codeBox.setStyle("-fx-background-color: #0d1117; -fx-border-color: #2d3748;"
                + " -fx-border-width: 1; -fx-padding: 16 20 16 20;");

        statusLabel = new Label("● starting server…");
        statusLabel.getStyleClass().add("status-label");

        Label settingsTitle = new Label("GAME SETTINGS");
        settingsTitle.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px; "
                + "-fx-font-family: 'Courier New', monospace;");

        maxPlayersSpinner = new Spinner<>(2, 4, 2);
        gamesToWinSpinner = new Spinner<>(1, 10, 3);
        timeLimitSpinner  = new Spinner<>(0, 600, 120, 30);

        difficultyBox = new ComboBox<>();
        difficultyBox.getItems().addAll("EASY", "MEDIUM", "HARD");
        difficultyBox.setValue("MEDIUM");

        gridSizeBox = new ComboBox<>();
        gridSizeBox.getItems().addAll("1280 × 720", "1920 × 1080", "800 × 600");
        gridSizeBox.setValue("1280 × 720");

        styleSpinner(maxPlayersSpinner);
        styleSpinner(gamesToWinSpinner);
        styleSpinner(timeLimitSpinner);
        styleCombo(difficultyBox);
        styleCombo(gridSizeBox);

        Runnable applySettings = () -> {
            settings.maxPlayers    = maxPlayersSpinner.getValue();
            settings.gamesToWin    = gamesToWinSpinner.getValue();
            settings.timeLimitSecs = timeLimitSpinner.getValue();
            settings.difficulty    = Difficulty.valueOf(difficultyBox.getValue());
            String[] gs = gridSizeBox.getValue().replace(" ", "").split("×");
            settings.gridWidth  = Integer.parseInt(gs[0]);
            settings.gridHeight = Integer.parseInt(gs[1]);
            server.updateSettings(settings);
        };
        maxPlayersSpinner.valueProperty().addListener((o,a,b) -> applySettings.run());
        gamesToWinSpinner.valueProperty().addListener((o,a,b) -> applySettings.run());
        timeLimitSpinner.valueProperty() .addListener((o,a,b) -> applySettings.run());
        difficultyBox.setOnAction(e -> applySettings.run());
        gridSizeBox.setOnAction(e   -> applySettings.run());

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(12);
        settingsGrid.setVgap(8);
        addSettingsRow(settingsGrid, 0, "Max players",      maxPlayersSpinner);
        addSettingsRow(settingsGrid, 1, "Games to win",     gamesToWinSpinner);
        addSettingsRow(settingsGrid, 2, "Time limit (sec)", timeLimitSpinner);
        addSettingsRow(settingsGrid, 3, "Difficulty",       difficultyBox);
        addSettingsRow(settingsGrid, 4, "Grid size",        gridSizeBox);

        VBox settingsBox = new VBox(8, settingsTitle, settingsGrid);
        settingsBox.setStyle("-fx-background-color: #0d1117; -fx-border-color: #2d3748;"
                + " -fx-border-width: 1; -fx-padding: 14 20 14 20;");

        Label playersTitle = new Label("PLAYERS IN LOBBY");
        playersTitle.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px; "
                + "-fx-font-family: 'Courier New', monospace;");

        playerList = new VBox(4);
        refreshPlayerList(List.of(app.getPlayerName()));

        VBox playersBox = new VBox(8, playersTitle, playerList);
        playersBox.setStyle("-fx-background-color: #0d1117; -fx-border-color: #2d3748;"
                + " -fx-border-width: 1; -fx-padding: 14 20 14 20;");

        VBox chatBox = buildChatPanel();

        startBtn = new Button("WAITING FOR PLAYERS…");
        startBtn.getStyleClass().add("tron-btn");
        startBtn.setDisable(true);
        startBtn.setOnAction(e -> {
            server.startGame();
            app.showMultiplayerHostGame(server, settings, app.getPlayerName());
        });

        Button back = new Button("←  BACK");
        back.getStyleClass().add("tron-btn-secondary");
        back.setOnAction(e -> { server.close(); app.showMultiplayer(); });

        VBox left = new VBox(14,
            nav, title, subtitle,
            codeBox,
            statusLabel,
            settingsBox,
            playersBox,
            Theme.spacer(6),
            startBtn,
            Theme.divider(),
            back
        );
        left.setAlignment(Pos.TOP_LEFT);
        left.setMaxWidth(460);
        left.setMinWidth(460);

        HBox root = new HBox(24, left, chatBox);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40));

        StackPane sp = new StackPane(root);
        sp.setAlignment(Pos.CENTER);
        Theme.apply(sp);
        sp.getStyleClass().add("screen-root");

        setupServerCallbacks();
        startServer();

        return sp;
    }

    private void setupServerCallbacks() {
        server.onPlayerCountChanged = count -> Platform.runLater(() -> {
            connectedCount = count;
            refreshPlayerList(null);
            boolean canStart = count >= 2;
            startBtn.setDisable(!canStart);
            startBtn.setText(canStart
                ? "▶  START GAME  (" + count + " players)"
                : "WAITING FOR PLAYERS…");
            statusLabel.setText("● " + count + " / " + settings.maxPlayers + " players");
        });

        server.onChatMessage = (name, msg) -> Platform.runLater(() ->
            appendChat(name, msg));

        server.onPlayerDisconnected = name -> Platform.runLater(() ->
            appendChat("SYSTEM", name + " disconnected."));

        server.onError = err -> Platform.runLater(() ->
            statusLabel.setText("✕ error: " + err));
    }

    private void startServer() {
        new Thread(() -> {
            server.listen(DEFAULT_PORT, settings, app.getPlayerName());
            Platform.runLater(() -> {
                String code = server.getRoomCode();
                String ip   = server.getLocalIp();
                roomCodeLabel.setText(formatCode(code));
                ipLabel.setText("your IP: " + ip + "  •  port: " + DEFAULT_PORT);
                statusLabel.setText("● waiting for players  (1 / " + settings.maxPlayers + ")");
            });
        }, "server-start").start();
    }

    private void refreshPlayerList(List<String> names) {
        playerList.getChildren().clear();
        if (names == null) return;
        for (int i = 0; i < names.size(); i++) {
            Label lbl = new Label((i == 0 ? "♟  " : "♙  ") + names.get(i)
                    + (i == 0 ? "  [HOST]" : ""));
            lbl.setStyle("-fx-text-fill: " + (i == 0 ? "#63b3ed" : "#e2e8f0")
                    + "; -fx-font-family: 'Courier New', monospace; -fx-font-size: 13px;");
            playerList.getChildren().add(lbl);
        }
    }

    private VBox buildChatPanel() {
        Label chatTitle = new Label("LOBBY CHAT");
        chatTitle.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px; "
                + "-fx-font-family: 'Courier New', monospace;");

        chatMessages = new VBox(4);
        chatMessages.setPadding(new Insets(8));

        chatScroll = new ScrollPane(chatMessages);
        chatScroll.setFitToWidth(true);
        chatScroll.setPrefHeight(300);
        chatScroll.setStyle("-fx-background: #0d1117; -fx-background-color: #0d1117;");
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        chatInput = new TextField();
        chatInput.setPromptText("type a message…");
        chatInput.getStyleClass().add("tron-input");
        chatInput.setStyle("-fx-pref-width: 340px; -fx-min-width: 340px;");

        Button sendBtn = new Button("SEND");
        sendBtn.getStyleClass().add("tron-btn");
        sendBtn.setStyle("-fx-pref-width: 80px; -fx-min-width: 80px;");

        Runnable doSend = () -> {
            String msg = chatInput.getText().trim();
            if (msg.isEmpty()) return;
            server.sendChat(app.getPlayerName(), msg);
            chatInput.clear();
        };
        sendBtn.setOnAction(e -> doSend.run());
        chatInput.setOnAction(e -> doSend.run());

        HBox inputRow = new HBox(8, chatInput, sendBtn);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        appendChat("SYSTEM", "Welcome! Share your room code for friends to join.");

        VBox panel = new VBox(8, chatTitle,
            new VBox(0, chatScroll),
            inputRow);
        panel.setStyle("-fx-background-color: #0d1117; -fx-border-color: #2d3748;"
                + " -fx-border-width: 1; -fx-padding: 14 20 14 20;");
        panel.setMinWidth(340);
        panel.setMaxWidth(340);
        return panel;
    }

    private void appendChat(String name, String msg) {
        Label lbl = new Label("[" + name + "] " + msg);
        lbl.setWrapText(true);
        lbl.setMaxWidth(300);
        lbl.setStyle("-fx-text-fill: " + ("SYSTEM".equals(name) ? "#718096" : "#e2e8f0")
                + "; -fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;");
        chatMessages.getChildren().add(lbl);
        chatScroll.layout();
        chatScroll.setVvalue(1.0);
    }

    private static void addSettingsRow(GridPane grid, int row, String label, Control ctrl) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #a0aec0; -fx-font-family: 'Courier New', monospace;"
                + " -fx-font-size: 13px; -fx-pref-width: 160px;");
        grid.add(lbl, 0, row);
        grid.add(ctrl, 1, row);
    }

    private static void styleSpinner(Spinner<?> sp) {
        sp.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #2d3748;"
                + " -fx-border-width: 1; -fx-pref-width: 100px;");
        sp.getEditor().setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #e2e8f0;"
                + " -fx-font-family: 'Courier New', monospace;");
        sp.setEditable(true);
    }

    private static void styleCombo(ComboBox<?> cb) {
        cb.setStyle("-fx-background-color: #1a1a2e; -fx-border-color: #2d3748;"
                + " -fx-border-width: 1; -fx-text-fill: #e2e8f0;"
                + " -fx-font-family: 'Courier New', monospace; -fx-pref-width: 160px;");
    }

    private static String formatCode(String code) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            if (i > 0 && i % 2 == 0) sb.append(' ');
            sb.append(code.charAt(i));
        }
        return sb.toString().toUpperCase();
    }
}
