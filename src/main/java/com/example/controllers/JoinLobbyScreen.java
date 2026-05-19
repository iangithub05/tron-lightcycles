package com.example.controllers;

import com.example.Main;
import com.example.models.LobbySettings;
import com.example.network.GameClient;
import com.example.network.LanDiscovery;
import com.example.network.RoomCode;
import com.example.ui.Theme;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class JoinLobbyScreen {

    private final Main app;
    private final GameClient client = new GameClient();
    private final String myName;

    private LobbySettings currentSettings;

    private volatile List<String> pendingLobbyNames;
    private volatile LobbySettings pendingLobbySettings;

    private Label roomCodeLabel;
    private Label hostInfoLabel;
    private Label statusLabel;
    private VBox playerList;
    private VBox chatMessages;
    private ScrollPane chatScroll;
    private TextField chatInput;

    private StackPane rootPane;

    public JoinLobbyScreen(Main app) {
        this.app = app;
        this.myName = app.getPlayerName();
    }

    public StackPane getView() {
        rootPane = new StackPane();
        rootPane.setAlignment(Pos.CENTER);
        // Theme.apply(rootPane);
        rootPane.getStyleClass().add("screen-root");

        showConnectView();
        return rootPane;
    }

    private void showConnectView() {
        rootPane.setBackground(UIHelper.createBackground("/images/background_5.png"));

        BorderPane screen = new BorderPane();
        screen.setTop(styledTopBar("| JOIN A GAME"));
        screen.setBottom(UIHelper.createNavigationBar("TRON: LIGHT CYCLEs", "BACK  QUIT"));

        VBox card = new VBox(14);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(24, 95, 24, 95));
        card.setMaxWidth(725);
        card.setStyle(panelStyle());

        Label title = bigTitle("JOIN A GAME", 34);
        Label subtitle = new Label("enter a room code or auto-discover on LAN");
        subtitle.setStyle("-fx-text-fill: white; -fx-font-family: '" + UIHelper.pixelFont.getFamily() + "'; -fx-font-size: 10px;");

        Label codeLabel = labelWhite("Room Code", 15);
        TextField codeField = new TextField();
        codeField.setPromptText("eg. 1A 2B 3C");
        codeField.setPrefSize(410, 36);
        codeField.setStyle(inputStyle());
        codeField.textProperty().addListener((o, a, b) -> {
            String up = b.toUpperCase();
            if (!up.equals(b)) { codeField.setText(up); codeField.positionCaret(up.length()); }
        });

        Button connectBtn = styledButton("CONNECT", 120, 36, "#17274a", "#111d39");
        HBox codeRow = new HBox(8, codeField, connectBtn);
        codeRow.setAlignment(Pos.CENTER_LEFT);

        VBox discoveredList = new VBox(7);
        discoveredList.setPrefHeight(52);
        discoveredList.setPadding(new Insets(13));
        discoveredList.setStyle("-fx-background-color: #17274a; -fx-background-radius: 8; -fx-border-color: black; -fx-border-radius: 8;");
        Label noneInitial = new Label("No games found on LAN.");
        noneInitial.setStyle("-fx-text-fill: #8f98b3; -fx-font-family: '" + UIHelper.pixelFont.getFamily() + "'; -fx-font-size: 9px;");
        discoveredList.getChildren().add(noneInitial);

        Button scanBtn = styledButton("SCAN LAN FOR GAMES", 535, 36, "#17274a", "#111d39");
        scanBtn.setOnAction(e -> {
            discoveredList.getChildren().clear();
            Label scanning = new Label("Scanning...");
            scanning.setStyle("-fx-text-fill: #8f98b3; -fx-font-family: '" + UIHelper.pixelFont.getFamily() + "'; -fx-font-size: 9px;");
            discoveredList.getChildren().add(scanning);
            scanBtn.setDisable(true);
            new Thread(() -> {
                List<LanDiscovery.DiscoveredGame> found =
                    LanDiscovery.scan(game -> Platform.runLater(() -> {
                        discoveredList.getChildren().remove(scanning);
                        discoveredList.getChildren().add(makeDiscoveredRow(game, codeField, connectBtn));
                    }));
                Platform.runLater(() -> {
                    scanBtn.setDisable(false);
                    if (found.isEmpty()) {
                        discoveredList.getChildren().clear();
                        Label none = new Label("No games found on LAN.");
                        none.setStyle("-fx-text-fill: #8f98b3; -fx-font-family: '" + UIHelper.pixelFont.getFamily() + "'; -fx-font-size: 9px;");
                        discoveredList.getChildren().add(none);
                    }
                });
            }, "lan-scan").start();
        });

        Label connectStatus = new Label("");
        connectStatus.setStyle("-fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        connectBtn.setOnAction(e -> {
            String raw = codeField.getText().trim().replace(" ", "");
            if (raw.isEmpty()) { connectStatus.setText("enter a room code first"); return; }
            try {
                String[] decoded = RoomCode.decode(raw);
                String ip = decoded[0];
                int port = Integer.parseInt(decoded[1]);
                connectStatus.setText("connecting to " + ip + ":" + port + "...");
                connectBtn.setDisable(true);
                scanBtn.setDisable(true);

                client.onConnected = slot -> Platform.runLater(() -> showLobbyView(raw, ip, port));
                client.onLobbyUpdate = (names, settings) -> {
                    pendingLobbyNames = names;
                    pendingLobbySettings = settings;
                    Platform.runLater(() -> {
                        if (playerList != null) {
                            currentSettings = settings;
                            refreshPlayerList(names);
                        }
                    });
                };
                client.onError = err -> Platform.runLater(() -> {
                    connectStatus.setText("✕ " + err);
                    connectBtn.setDisable(false);
                    scanBtn.setDisable(false);
                });
                client.connect(ip, port, myName);
            } catch (Exception ex) {
                connectStatus.setText("✕ invalid room code");
                connectBtn.setDisable(false);
            }
        });
        codeField.setOnAction(e -> connectBtn.fire());

        Button back = styledButton("BACK", 120, 34, "#111827", "#0a0f18");
        back.setOnAction(e -> { client.close(); app.showMultiplayer(); });

        VBox form = new VBox(8, codeLabel, codeRow, thinLine(), scanBtn, discoveredList, connectStatus, back);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(535);

        card.getChildren().addAll(title, subtitle, Theme.spacer(14), form);
        screen.setCenter(card);
        BorderPane.setAlignment(card, Pos.CENTER);
        BorderPane.setMargin(card, new Insets(16));
        rootPane.getChildren().setAll(screen);
    }

    private void showLobbyView(String roomCode, String ip, int port) {
        rootPane.setBackground(UIHelper.createBackground("/images/background_4.png"));

        BorderPane screen = new BorderPane();
        screen.setTop(styledTopBar("| JOINED LOBBY"));
        screen.setBottom(UIHelper.createNavigationBar("TRON: LIGHT CYCLEs", "WAITING FOR HOST"));

        HBox main = new HBox(12);
        main.setAlignment(Pos.CENTER);
        main.setPadding(new Insets(18, 38, 18, 38));

        VBox left = new VBox(10);
        left.setAlignment(Pos.TOP_CENTER);
        left.setPrefWidth(540);

        VBox roomPanel = new VBox(10);
        roomPanel.setAlignment(Pos.CENTER);
        roomPanel.setPrefSize(540, 105);
        roomPanel.setPadding(new Insets(14, 18, 14, 18));
        roomPanel.setStyle(panelStyle());

        Label roomTitle = bigTitle("JOINED LOBBY", 27);
        Label waitingText = labelWhite("WAITING FOR HOST TO START", 11);

        roomCodeLabel = pillLabel(formatCode(roomCode), 245, 30);
        hostInfoLabel = labelWhite("HOST  " + ip + "  :  " + port, 9);

        VBox roomInfo = new VBox(4, roomCodeLabel, hostInfoLabel);
        roomInfo.setAlignment(Pos.CENTER);

        roomPanel.getChildren().addAll(roomTitle, waitingText, roomInfo);

        HBox middle = new HBox(10);
        middle.setAlignment(Pos.TOP_CENTER);

        VBox playersPanel = new VBox(7);
        playersPanel.setAlignment(Pos.TOP_LEFT);
        playersPanel.setPadding(new Insets(14, 16, 14, 16));
        playersPanel.setPrefSize(265, 230);
        playersPanel.setStyle(panelStyle());

        Label playersTitle = labelWhite("PLAYERS IN LOBBY", 12);
        statusLabel = labelWhite("CONNECTED - WAITING", 8);
        playerList = new VBox(6);
        addLoadingRow(playerList, "loading players...");

        playersPanel.getChildren().addAll(playersTitle, statusLabel, playerList);

        VBox settingsPanel = new VBox(8);
        settingsPanel.setAlignment(Pos.TOP_LEFT);
        settingsPanel.setPadding(new Insets(14, 16, 14, 16));
        settingsPanel.setPrefSize(265, 230);
        settingsPanel.setStyle(panelStyle());

        Label settingsTitle = labelWhite("GAME SETTINGS", 12);
        Label lockedText = labelWhite("SET BY HOST", 8);

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(12);
        settingsGrid.setVgap(8);
        addReadOnlyRow(settingsGrid, 0, "Max Players", "...");
        addReadOnlyRow(settingsGrid, 1, "Games To Win", "...");
        addReadOnlyRow(settingsGrid, 2, "Time Limit", "...");
        addReadOnlyRow(settingsGrid, 3, "Speed", "...");
        addReadOnlyRow(settingsGrid, 4, "Grid Size", "...");

        settingsPanel.getChildren().addAll(settingsTitle, lockedText, settingsGrid);

        middle.getChildren().addAll(playersPanel, settingsPanel);

        Button waitingBtn = styledButton("WAITING FOR HOST...", 260, 42, "#23402c", "#16261b");
        waitingBtn.setDisable(true);
        waitingBtn.setOpacity(0.75);

        Button leaveBtn = styledButton("LEAVE LOBBY", 260, 42, "#111827", "#0a0f18");
        leaveBtn.setOnAction(e -> {
            client.close();
            app.showMultiplayer();
        });

        HBox buttons = new HBox(10, leaveBtn, waitingBtn);
        buttons.setAlignment(Pos.CENTER);

        left.getChildren().addAll(roomPanel, middle, buttons);

        VBox chatPanel = buildChatPanel();
        chatPanel.setPrefWidth(300);
        chatPanel.setMinWidth(300);
        chatPanel.setMaxWidth(300);

        main.getChildren().addAll(left, chatPanel);
        screen.setCenter(main);
        rootPane.getChildren().setAll(screen);

        client.onConnected = null;

        client.onLobbyUpdate = (names, settings) -> Platform.runLater(() -> {
            currentSettings = settings;
            refreshPlayerList(names);
            refreshSettingsGrid(settingsGrid, settings);
            int maxP = settings != null ? settings.maxPlayers : 4;
            statusLabel.setText("CONNECTED  (" + names.size() + "/" + maxP + ")");
        });

        if (pendingLobbyNames != null && pendingLobbySettings != null) {
            currentSettings = pendingLobbySettings;
            refreshPlayerList(pendingLobbyNames);
            refreshSettingsGrid(settingsGrid, pendingLobbySettings);
            statusLabel.setText("CONNECTED  (" + pendingLobbyNames.size() + "/" + pendingLobbySettings.maxPlayers + ")");
        }

        client.onSettingsChanged = settings -> Platform.runLater(() -> {
            currentSettings = settings;
            refreshSettingsGrid(settingsGrid, settings);
            appendChat("SYSTEM", "Host updated game settings.");
        });

        client.onChatMessage = (name, msg) -> Platform.runLater(() ->
            appendChat(name, msg));

        client.onPlayerDisconnected = name -> Platform.runLater(() -> {
            appendChat("SYSTEM", name + " disconnected from the lobby.");
            statusLabel.setText("PLAYER LEFT - WAITING");
        });

        client.onGameStart = () -> Platform.runLater(() ->
            app.showMultiplayerClientGame(client, currentSettings, myName));

        client.onError = err -> Platform.runLater(() ->
            statusLabel.setText("ERROR: " + err));

        appendChat("SYSTEM", "Connected. Waiting for host to start.");
    }


    private void refreshPlayerList(List<String> names) {
        playerList.getChildren().clear();
        String[] slotColors = {"#63b3ed", "#68d391", "#fc8181", "#b794f4"};
        int mySlot = client.getMySlot();

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String color = slotColors[i % slotColors.length];
            boolean isMe = i == mySlot;
            String icon = i == 0 ? "♟" : "♙";
            String tag = i == 0 ? "  [HOST]" : (isMe ? "  [YOU]" : "  [P" + (i + 1) + "]");

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: " + (isMe ? "#0f1f2e" : "#111827")
                    + "; -fx-border-color: " + (isMe ? "#2a5a8c" : "#1f2937")
                    + "; -fx-border-width: 1; -fx-padding: 8 12 8 12;");

            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 10px;");

            Label nameLbl = new Label(icon + "  " + name + tag);
            nameLbl.setStyle("-fx-text-fill: " + color
                    + "; -fx-font-family: 'Courier New', monospace; -fx-font-size: 13px;"
                    + (isMe ? " -fx-font-weight: bold;" : ""));

            row.getChildren().addAll(dot, nameLbl);
            playerList.getChildren().add(row);
        }

        int maxP = currentSettings != null ? currentSettings.maxPlayers : 4;
        for (int i = names.size(); i < maxP; i++) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #0d1117; -fx-border-color: #1f2937;"
                    + " -fx-border-width: 1; -fx-border-style: dashed;"
                    + " -fx-padding: 8 12 8 12;");
            Label empty = new Label("○  waiting for player " + (i + 1) + "…");
            empty.setStyle("-fx-text-fill: #4a5568; -fx-font-family: 'Courier New', monospace;"
                    + " -fx-font-size: 13px;");
            row.getChildren().add(empty);
            playerList.getChildren().add(row);
        }
    }

    private static void addLoadingRow(VBox box, String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #4a5568; -fx-font-family: 'Courier New', monospace;"
                + " -fx-font-size: 13px;");
        box.getChildren().add(lbl);
    }
    private static void addReadOnlyRow(GridPane grid, int row, String key, String value) {
        Label k = new Label(key);
        k.setStyle("-fx-text-fill: #a0aec0; -fx-font-family: 'Courier New', monospace;"
                + " -fx-font-size: 13px; -fx-pref-width: 160px;");

        Label v = new Label(value);
        v.setId("settings-val-" + row);
        v.setStyle("-fx-text-fill: #63b3ed; -fx-font-family: 'Courier New', monospace;"
                + " -fx-font-size: 13px;");

        Label lock = new Label("🔒");
        lock.setStyle("-fx-font-size: 10px; -fx-opacity: 0.4;");

        HBox valRow = new HBox(6, v, lock);
        valRow.setAlignment(Pos.CENTER_LEFT);

        grid.add(k, 0, row);
        grid.add(valRow, 1, row);
    }

    private static void refreshSettingsGrid(GridPane grid, LobbySettings s) {
        String[] values = {
            String.valueOf(s.maxPlayers),
            String.valueOf(s.gamesToWin),
            s.timeLimitSecs == 0 ? "no limit" : s.timeLimitSecs + "s",
            String.valueOf(s.speed),
            s.gridWidth + " × " + s.gridHeight
        };
        for (int i = 0; i < values.length; i++) {
            final int row = i;
            final String val = values[i];
            grid.getChildren().stream()
                .filter(n -> val != null && (n instanceof HBox))
                .forEach(hbox -> {
                    if (GridPane.getRowIndex(hbox) != null
                            && GridPane.getRowIndex(hbox) == row
                            && GridPane.getColumnIndex(hbox) != null
                            && GridPane.getColumnIndex(hbox) == 1) {
                        ((HBox) hbox).getChildren().stream()
                            .filter(c -> c instanceof Label
                                    && ("settings-val-" + row).equals(c.getId()))
                            .forEach(c -> ((Label) c).setText(val));
                    }
                });
        }
    }

    private VBox buildChatPanel() {
        Label chatTitle = bigTitle("LOBBY CHAT", 20);
        chatTitle.setAlignment(Pos.CENTER);
        chatTitle.setPrefSize(300, 46);
        chatTitle.setStyle(panelStyle()
                + "-fx-text-fill: white; -fx-font-family: '" + UIHelper.pixelFont.getFamily()
                + "'; -fx-font-size: 20px; -fx-font-weight: bold;");

        chatMessages = new VBox(5);
        chatMessages.setPadding(new Insets(10));

        chatScroll = new ScrollPane(chatMessages);
        chatScroll.setFitToWidth(true);
        chatScroll.setPrefSize(300, 282);
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScroll.setStyle(
            "-fx-background: rgba(20, 22, 35, 0.72);" +
            "-fx-background-color: rgba(20, 22, 35, 0.72);" +
            "-fx-border-color: black;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );

        chatInput = new TextField();
        chatInput.setPromptText("Enter message...");
        chatInput.setPrefSize(226, 32);
        chatInput.setStyle(inputStyle());

        Button sendBtn = styledButton(">", 44, 32, "#17274a", "#111d39");

        Runnable doSend = () -> {
            String msg = chatInput.getText().trim();
            if (msg.isEmpty()) return;
            client.sendChat(myName, msg);
            chatInput.clear();
        };
        sendBtn.setOnAction(e -> doSend.run());
        chatInput.setOnAction(e -> doSend.run());

        HBox inputRow = new HBox(8, chatInput, sendBtn);
        inputRow.setAlignment(Pos.CENTER);

        VBox chatBody = new VBox(8, chatScroll, inputRow);
        chatBody.setAlignment(Pos.TOP_CENTER);
        chatBody.setPadding(new Insets(10));
        chatBody.setPrefSize(300, 342);
        chatBody.setStyle(panelStyle());

        VBox panel = new VBox(8, chatTitle, chatBody);
        panel.setAlignment(Pos.TOP_CENTER);
        return panel;
    }


    private void appendChat(String name, String msg) {
        if (chatMessages == null) return;
        Label lbl = new Label("[" + name + "] " + msg);
        lbl.setWrapText(true);
        lbl.setMaxWidth(240);
        lbl.setStyle("-fx-text-fill: " + ("SYSTEM".equals(name) ? "#718096" : "#e2e8f0")
                + "; -fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;");
        chatMessages.getChildren().add(lbl);
        chatScroll.layout();
        chatScroll.setVvalue(1.0);
    }


    private HBox makeDiscoveredRow(LanDiscovery.DiscoveredGame game,
                                   TextField codeField, Button connectBtn) {
        Label info = new Label(game.hostName + "  "
                + game.currentPlayers + "/" + game.maxPlayers + "  [" + game.roomCode + "]");
        info.setStyle("-fx-text-fill: #e2e8f0; -fx-font-family: 'Courier New', monospace;"
                + " -fx-font-size: 12px;");

        Button join = new Button("JOIN");
        join.setStyle("-fx-background-color: #1a3a5c; -fx-text-fill: #63b3ed;"
                + " -fx-font-family: 'Courier New', monospace; -fx-border-color: #2a5a8c;"
                + " -fx-border-width: 1; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
        join.setOnAction(e -> {
            codeField.setText(game.roomCode);
            connectBtn.fire();
        });

        HBox row = new HBox(10, info, Theme.hspacer(), join);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #0d1117; -fx-border-color: #2d3748;"
                + " -fx-border-width: 1; -fx-padding: 8 12 8 12;");
        return row;
    }


    private HBox styledTopBar(String leftText) {
        HBox topBar = UIHelper.createNavigationBar(leftText, "Welcome, " + myName);
        Label welcome = (Label) topBar.getChildren().get(2);
        welcome.setPadding(new Insets(12, 26, 12, 26));
        welcome.setStyle("-fx-background-color: linear-gradient(to right, #9b2447, #6d1831);"
                + "-fx-background-radius: 12; -fx-border-color: #4d1020; -fx-border-radius: 12;"
                + "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "'; -fx-font-size: 14px;"
                + "-fx-font-weight: bold; -fx-text-fill: white;");
        return topBar;
    }

    private static Label bigTitle(String text, int size) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: white; -fx-font-family: '" + UIHelper.pixelFont.getFamily() + "'; -fx-font-size: " + size + "px; -fx-font-weight: bold;");
        return l;
    }

    private static Label labelWhite(String text, int size) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: white; -fx-font-family: '" + UIHelper.pixelFont.getFamily() + "'; -fx-font-size: " + size + "px; -fx-font-weight: bold;");
        return l;
    }

    private static Label pillLabel(String text, int width, int height) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setPrefSize(width, height);
        label.setStyle("-fx-background-color: linear-gradient(to right, #9b2447, #6d1831);"
                + "-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #4d1020;"
                + "-fx-text-fill: white; -fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';"
                + "-fx-font-size: 11px; -fx-font-weight: bold;");
        return label;
    }

    private static Button styledButton(String text, int w, int h, String top, String bottom) {
        Button b = new Button(text);
        b.setCursor(javafx.scene.Cursor.HAND);
        b.setPrefSize(w, h);
        b.setStyle("-fx-background-color: linear-gradient(to bottom, " + top + ", " + bottom + ");"
                + "-fx-text-fill: white; -fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';"
                + "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 8;"
                + "-fx-border-color: black; -fx-border-radius: 8; -fx-border-width: 1;");
        return b;
    }

    private static Region thinLine() {
        Region r = new Region();
        r.setPrefHeight(2);
        r.setStyle("-fx-background-color: rgba(255,255,255,0.35);");
        return r;
    }

    private static String inputStyle() {
        return "-fx-background-color: #17274a; -fx-text-fill: white; -fx-prompt-text-fill: #8f98b3;"
                + "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "'; -fx-font-size: 12px;"
                + "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: black;";
    }

    private static String panelStyle() {
        return "-fx-background-color: rgba(20, 22, 35, 0.72);"
             + "-fx-background-radius: 8; -fx-border-color: black; -fx-border-radius: 8; -fx-border-width: 1;";
    }

    private static String formatCode(String code) {
        code = code.replace(" ", "").toUpperCase();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            if (i > 0 && i % 2 == 0) sb.append(' ');
            sb.append(code.charAt(i));
        }
        return sb.toString();
    }
}