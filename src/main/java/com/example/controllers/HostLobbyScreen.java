package com.example.controllers;

import com.example.Main;
import com.example.models.LobbySettings;
import com.example.network.GameServer;
import com.example.ui.Theme;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;

public class HostLobbyScreen {

    private static final int DEFAULT_PORT = 5555;

    private final Main app;
    private final GameServer server = new GameServer();
    private final LobbySettings settings = new LobbySettings();

    private Label statusLabel;
    private Label roomCodeLabel;
    private Label ipLabel;
    private Label portLabel;
    private Label playerCountLabel;
    private VBox playerList;
    private VBox chatMessages;
    private ScrollPane chatScroll;
    private TextField chatInput;
    private Button startBtn;

    private Spinner<Integer> maxPlayersSpinner;
    private Spinner<Integer> gamesToWinSpinner;
    private Spinner<Integer> timeLimitSpinner;
    private Spinner<Double> speedSpinner;
    private ComboBox<String> gridSizeBox;

    public HostLobbyScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        StackPane root = new StackPane();
        root.setBackground(UIHelper.createBackground("/images/background_4.png"));
        Theme.apply(root);

        BorderPane screen = new BorderPane();
        screen.setTop(UIHelper.createNavigationBar("| HOST A GAME", "Welcome, " + app.getPlayerName()));
        screen.setBottom(UIHelper.createNavigationBar("TRON: Light Cycles", "QUIT"));

        VBox content = new VBox(14);
        content.setPadding(new Insets(18, 38, 18, 38));
        content.setAlignment(Pos.TOP_CENTER);

        HBox topInfo = new HBox(14, buildRoomPanel(), buildQuickActionsPanel());
        topInfo.setAlignment(Pos.CENTER);

        HBox mainPanels = new HBox(14, buildPlayersPanel(), buildSettingsPanel(), buildChatPanel());
        mainPanels.setAlignment(Pos.CENTER);
        HBox.setHgrow(mainPanels, Priority.ALWAYS);

        content.getChildren().addAll(topInfo, mainPanels);
        screen.setCenter(content);
        root.getChildren().add(screen);

        setupServerCallbacks();
        startServer();

        return root;
    }

    private VBox buildRoomPanel() {
        Label title = titleLabel("ROOM CODE");
        Label helper = smallDimLabel("Give this code to players who want to join your lobby.");

        roomCodeLabel = new Label("STARTING...");
        roomCodeLabel.setAlignment(Pos.CENTER);
        roomCodeLabel.setPrefHeight(46);
        roomCodeLabel.setMaxWidth(Double.MAX_VALUE);
        roomCodeLabel.setStyle(
            "-fx-background-color: rgba(15, 25, 52, 0.95);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: black;" +
            "-fx-border-radius: 10;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;"
        );

        ipLabel = infoPill("IP: starting...");
        portLabel = infoPill("PORT: " + DEFAULT_PORT);

        HBox serverInfo = new HBox(10, ipLabel, portLabel);
        serverInfo.setAlignment(Pos.CENTER);
        HBox.setHgrow(ipLabel, Priority.ALWAYS);
        HBox.setHgrow(portLabel, Priority.ALWAYS);

        VBox panel = new VBox(9, title, helper, roomCodeLabel, serverInfo);
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setPadding(new Insets(18));
        panel.setPrefWidth(520);
        panel.setMinHeight(160);
        panel.setStyle(panelStyle());
        return panel;
    }

    private VBox buildQuickActionsPanel() {
        Label title = titleLabel("HOST CONTROLS");

        statusLabel = smallDimLabel("Starting server...");

        Button copyBtn = actionButton("COPY ROOM CODE", "#1d355f", "#10213d");
        copyBtn.setOnAction(e -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(roomCodeLabel.getText().replace(" ", ""));
            Clipboard.getSystemClipboard().setContent(cc);
            copyBtn.setText("COPIED!");
            PauseTransition pt = new PauseTransition(Duration.seconds(1.5));
            pt.setOnFinished(ev -> copyBtn.setText("COPY ROOM CODE"));
            pt.play();
        });

        startBtn = actionButton("WAITING FOR PLAYERS", "#23402c", "#16261b");
        startBtn.setDisable(true);
        startBtn.setOnAction(e -> {
            server.startGame();
            app.showMultiplayerHostGame(server, settings, app.getPlayerName());
        });

        Button back = actionButton("BACK TO MULTIPLAYER", "#2b2f42", "#151824");
        back.setOnAction(e -> {
            server.close();
            app.showMultiplayer();
        });

        VBox panel = new VBox(10, title, statusLabel, copyBtn, startBtn, back);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(18));
        panel.setPrefWidth(260);
        panel.setMinHeight(160);
        panel.setStyle(panelStyle());
        return panel;
    }

    private VBox buildPlayersPanel() {
        Label title = titleLabel("PLAYERS IN LOBBY");
        playerCountLabel = smallDimLabel("1 / " + settings.maxPlayers + " players connected");

        playerList = new VBox(7);
        playerList.setPadding(new Insets(10));
        playerList.setStyle(innerPanelStyle());
        VBox.setVgrow(playerList, Priority.ALWAYS);
        refreshPlayerList(List.of(app.getPlayerName()));

        VBox panel = new VBox(9, title, playerCountLabel, playerList);
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(245);
        panel.setMinHeight(295);
        panel.setStyle(panelStyle());
        return panel;
    }

    private VBox buildSettingsPanel() {
        Label title = titleLabel("GAME SETTINGS");
        Label helper = smallDimLabel("Press SAVE after changing settings.");

        maxPlayersSpinner = new Spinner<>(2, 4, 2);
        gamesToWinSpinner = new Spinner<>(1, 10, 3);
        timeLimitSpinner = new Spinner<>(0, 600, 120, 30);
        speedSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 10.0, 2.5, 0.5));

        gridSizeBox = new ComboBox<>();
        gridSizeBox.getItems().addAll("1280 × 720", "1920 × 1080", "800 × 600");
        gridSizeBox.setValue("1280 × 720");

        styleSpinner(maxPlayersSpinner);
        styleSpinner(gamesToWinSpinner);
        styleSpinner(timeLimitSpinner);
        styleSpinner(speedSpinner);
        styleCombo(gridSizeBox);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        addSettingsRow(grid, 0, "MAX PLAYERS", maxPlayersSpinner);
        addSettingsRow(grid, 1, "GAMES TO WIN", gamesToWinSpinner);
        addSettingsRow(grid, 2, "TIME LIMIT", timeLimitSpinner);
        addSettingsRow(grid, 3, "PLAYER SPEED", speedSpinner);
        addSettingsRow(grid, 4, "GRID SIZE", gridSizeBox);

        Button saveBtn = actionButton("SAVE SETTINGS", "#8f2348", "#5f1730");
        saveBtn.setOnAction(e -> {
            settings.maxPlayers = maxPlayersSpinner.getValue();
            settings.gamesToWin = gamesToWinSpinner.getValue();
            settings.timeLimitSecs = timeLimitSpinner.getValue();
            settings.speed = speedSpinner.getValue();

            String[] gs = gridSizeBox.getValue().replace(" ", "").split("×");
            settings.gridWidth = Integer.parseInt(gs[0]);
            settings.gridHeight = Integer.parseInt(gs[1]);

            server.updateSettings(settings);
            if (playerCountLabel != null) {
                playerCountLabel.setText(server.getPlayerNames().size() + " / " + settings.maxPlayers + " players connected");
            }
        });

        VBox panel = new VBox(10, title, helper, grid, saveBtn);
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(280);
        panel.setMinHeight(295);
        panel.setStyle(panelStyle());
        return panel;
    }

    private VBox buildChatPanel() {
        Label title = titleLabel("LOBBY CHAT");
        Label helper = smallDimLabel("Messages sent here stay inside this lobby.");

        chatMessages = new VBox(5);
        chatMessages.setPadding(new Insets(10));

        chatScroll = new ScrollPane(chatMessages);
        chatScroll.setFitToWidth(true);
        chatScroll.setPrefHeight(205);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScroll.setStyle(
            "-fx-background: rgba(15, 25, 52, 0.92);" +
            "-fx-background-color: rgba(15, 25, 52, 0.92);" +
            "-fx-border-color: black;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );

        chatInput = new TextField();
        chatInput.setPromptText("Type message...");
        chatInput.setPrefHeight(34);
        chatInput.setStyle(inputStyle());
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        Button sendBtn = actionButton("SEND", "#1d355f", "#10213d");
        sendBtn.setPrefWidth(74);

        Runnable doSend = () -> {
            String msg = chatInput.getText().trim();
            if (msg.isEmpty()) return;
            server.sendChat(app.getPlayerName(), msg);
            chatInput.clear();
        };

        sendBtn.setOnAction(e -> doSend.run());
        chatInput.setOnAction(e -> doSend.run());

        HBox inputRow = new HBox(7, chatInput, sendBtn);
        inputRow.setAlignment(Pos.CENTER);

        VBox panel = new VBox(9, title, helper, chatScroll, inputRow);
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(295);
        panel.setMinHeight(295);
        panel.setStyle(panelStyle());

        appendChat("SYSTEM", "Lobby created. Share the room code with other players.");
        return panel;
    }

    private void setupServerCallbacks() {
        server.onPlayerCountChanged = count -> Platform.runLater(() -> {
            refreshPlayerList(server.getPlayerNames());

            boolean canStart = count >= 2;
            startBtn.setDisable(!canStart);
            startBtn.setText(canStart ? "START GAME" : "WAITING FOR PLAYERS");

            statusLabel.setText(canStart
                ? "Ready to start with " + count + " players."
                : "Waiting for at least 1 more player.");

            playerCountLabel.setText(count + " / " + settings.maxPlayers + " players connected");
        });

        server.onChatMessage = (name, msg) -> Platform.runLater(() -> appendChat(name, msg));

        server.onPlayerDisconnected = name -> Platform.runLater(() ->
            appendChat("SYSTEM", name + " disconnected."));

        server.onError = err -> Platform.runLater(() ->
            statusLabel.setText("Server error: " + err));
    }

    private void startServer() {
        new Thread(() -> {
            server.listen(DEFAULT_PORT, settings, app.getPlayerName());
            Platform.runLater(() -> {
                String code = server.getRoomCode();
                String ip = server.getLocalIp();

                roomCodeLabel.setText(formatCode(code));
                ipLabel.setText("IP: " + ip);
                portLabel.setText("PORT: " + DEFAULT_PORT);
                statusLabel.setText("Waiting for players to join.");
                playerCountLabel.setText("1 / " + settings.maxPlayers + " players connected");
            });
        }, "server-start").start();
    }

    private void refreshPlayerList(List<String> names) {
        playerList.getChildren().clear();
        if (names == null) return;

        for (int i = 0; i < names.size(); i++) {
            Label lbl = new Label((i == 0 ? "HOST  " : "PLAYER ") + (i + 1) + "  •  " + names.get(i));
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setStyle(
                "-fx-background-color: rgba(25, 38, 76, 0.90);" +
                "-fx-background-radius: 7;" +
                "-fx-padding: 8 10 8 10;" +
                "-fx-text-fill: " + (i == 0 ? "#8db7ff" : "#ffffff") + ";" +
                "-fx-font-family: 'Courier New', monospace;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;"
            );
            playerList.getChildren().add(lbl);
        }
    }

    private void appendChat(String name, String msg) {
        if (chatMessages == null) return;

        Label lbl = new Label("[" + name + "] " + msg);
        lbl.setWrapText(true);
        lbl.setMaxWidth(250);
        lbl.setStyle(
            "-fx-text-fill: " + ("SYSTEM".equals(name) ? "#aeb7d0" : "white") + ";" +
            "-fx-font-family: Arial;" +
            "-fx-font-size: 13px;"
        );

        chatMessages.getChildren().add(lbl);
        chatScroll.layout();
        chatScroll.setVvalue(1.0);
    }

    private static void addSettingsRow(GridPane grid, int row, String label, Control ctrl) {
        Label lbl = new Label(label);
        lbl.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 8px;"
        );
        lbl.setPrefWidth(145);
        grid.add(lbl, 0, row);
        grid.add(ctrl, 1, row);
    }

    private static void styleSpinner(Spinner<?> sp) {
        sp.setPrefWidth(105);
        sp.setEditable(true);
        sp.setStyle(
            "-fx-background-color: rgba(143, 35, 72, 0.95);" +
            "-fx-background-radius: 7;" +
            "-fx-border-color: black;" +
            "-fx-border-radius: 7;"
        );
        sp.getEditor().setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: black;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 12px;" +
            "-fx-alignment: center;"
        );
    }

    private static void styleCombo(ComboBox<?> cb) {
        cb.setPrefWidth(130);
        cb.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: black;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 12px;"
        );
    }

    private static Label titleLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 14));
        return label;
    }

    private static Label smallDimLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setTextFill(Color.web("#c6ccdd"));
        label.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        return label;
    }

    private static Label infoPill(String text) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setPrefHeight(30);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(
            "-fx-background-color: linear-gradient(to right, #9b2447, #6d1831);" +
            "-fx-background-radius: 9;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Courier New', monospace;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );
        return label;
    }

    private static Button actionButton(String text, String top, String bottom) {
        Button btn = new Button(text);
        btn.setCursor(Cursor.HAND);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(38);
        btn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " + top + ", " + bottom + ");" +
            "-fx-text-fill: white;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 9px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 9;" +
            "-fx-border-color: black;" +
            "-fx-border-radius: 9;" +
            "-fx-border-width: 1;"
        );
        return btn;
    }

    private static String panelStyle() {
        return "-fx-background-color: rgba(20, 22, 35, 0.78);" +
               "-fx-background-radius: 10;" +
               "-fx-border-color: black;" +
               "-fx-border-radius: 10;" +
               "-fx-border-width: 1;";
    }

    private static String innerPanelStyle() {
        return "-fx-background-color: rgba(15, 25, 52, 0.88);" +
               "-fx-background-radius: 8;" +
               "-fx-border-color: black;" +
               "-fx-border-radius: 8;";
    }

    private static String inputStyle() {
        return "-fx-background-color: #17274a;" +
               "-fx-text-fill: white;" +
               "-fx-prompt-text-fill: #c6ccdd;" +
               "-fx-font-size: 12px;" +
               "-fx-background-radius: 9;" +
               "-fx-border-radius: 9;" +
               "-fx-border-color: black;";
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