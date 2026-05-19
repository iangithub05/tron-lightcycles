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
        Theme.apply(rootPane);
        rootPane.getStyleClass().add("screen-root");

        showConnectView();
        return rootPane;
    }

    private void showConnectView() {
        Label nav = new Label("MAIN MENU  /  MULTIPLAYER  /  JOIN A GAME");
        nav.getStyleClass().add("nav-title");

        Label title = new Label("JOIN A GAME");
        title.getStyleClass().add("game-title");

        Label subtitle = new Label("enter a room code or auto-discover on LAN");
        subtitle.getStyleClass().add("screen-subtitle");

        Label codeLabel = new Label("Room Code");
        codeLabel.getStyleClass().add("tron-label");

        TextField codeField = new TextField();
        codeField.setPromptText("e.g. 1A 2B 3C");
        codeField.getStyleClass().add("tron-input");
        codeField.setStyle("-fx-pref-width: 300px; -fx-min-width: 300px;");
        codeField.textProperty().addListener((o, a, b) -> {
            String up = b.toUpperCase();
            if (!up.equals(b)) { codeField.setText(up); codeField.positionCaret(up.length()); }
        });

        Button connectBtn = new Button("CONNECT");
        connectBtn.getStyleClass().add("tron-btn");
        connectBtn.setStyle("-fx-pref-width: 140px; -fx-min-width: 140px;");

        HBox codeRow = new HBox(8, codeField, connectBtn);
        codeRow.setAlignment(Pos.CENTER_LEFT);

        Label orLabel = new Label("─────  or auto-discover  ─────");
        orLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-family: 'Courier New', monospace;"
                + " -fx-font-size: 11px;");

        VBox discoveredList = new VBox(4);
        Button scanBtn = new Button("⟳  SCAN LAN FOR GAMES");
        scanBtn.getStyleClass().add("tron-btn");

        scanBtn.setOnAction(e -> {
            discoveredList.getChildren().clear();
            Label scanning = new Label("scanning…");
            scanning.setStyle("-fx-text-fill: #718096; -fx-font-family: 'Courier New', monospace;");
            discoveredList.getChildren().add(scanning);
            scanBtn.setDisable(true);
            new Thread(() -> {
                List<LanDiscovery.DiscoveredGame> found =
                    LanDiscovery.scan(game -> Platform.runLater(() -> {
                        discoveredList.getChildren().remove(scanning);
                        discoveredList.getChildren().add(
                            makeDiscoveredRow(game, codeField, connectBtn));
                    }));
                Platform.runLater(() -> {
                    scanBtn.setDisable(false);
                    if (found.isEmpty()) {
                        discoveredList.getChildren().clear();
                        Label none = new Label("no games found on LAN");
                        none.setStyle("-fx-text-fill: #718096;"
                                + " -fx-font-family: 'Courier New', monospace;");
                        discoveredList.getChildren().add(none);
                    }
                });
            }, "lan-scan").start();
        });

        Label connectStatus = new Label("");
        connectStatus.getStyleClass().add("status-label");

        connectBtn.setOnAction(e -> {
            String raw = codeField.getText().trim().replace(" ", "");
            if (raw.isEmpty()) { connectStatus.setText("enter a room code first"); return; }
            try {
                String[] decoded = RoomCode.decode(raw);
                String ip = decoded[0];
                int    port = Integer.parseInt(decoded[1]);
                connectStatus.setText("● connecting to " + ip + ":" + port + "…");
                connectBtn.setDisable(true);
                scanBtn.setDisable(true);

                client.onConnected = slot -> Platform.runLater(() ->
                    showLobbyView(raw, ip, port));
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

        Button back = new Button("←  BACK");
        back.getStyleClass().add("tron-btn-secondary");
        back.setOnAction(e -> { client.close(); app.showMultiplayer(); });

        VBox content = new VBox(14,
            nav, title, subtitle,
            codeLabel, codeRow,
            orLabel, scanBtn, discoveredList,
            connectStatus,
            Theme.spacer(6),
            Theme.divider(),
            back
        );
        content.setAlignment(Pos.TOP_LEFT);
        content.setMaxWidth(460);
        content.setMinWidth(460);

        VBox wrapper = new VBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setPadding(new Insets(40));

        rootPane.getChildren().setAll(wrapper);
    }


    private void showLobbyView(String roomCode, String ip, int port) {
        Label nav = new Label("MAIN MENU  /  MULTIPLAYER  /  JOINED LOBBY");
        nav.getStyleClass().add("nav-title");

        Label title = new Label("JOINED LOBBY");
        title.getStyleClass().add("game-title");

        Label subtitle = new Label("waiting for the host to start the game");
        subtitle.getStyleClass().add("screen-subtitle");

        roomCodeLabel = new Label(formatCode(roomCode));
        roomCodeLabel.setStyle(
            "-fx-text-fill: #63b3ed; -fx-font-size: 32px; -fx-font-weight: bold;"
            + " -fx-font-family: 'Courier New', monospace;");

        hostInfoLabel = new Label("host IP: " + ip + "  •  port: " + port);
        hostInfoLabel.getStyleClass().add("tron-label-dim");

        Label readOnly = new Label("🔒  room code  —  read only");
        readOnly.setStyle("-fx-text-fill: #4a5568; -fx-font-family: 'Courier New', monospace;"
                + " -fx-font-size: 11px;");

        VBox codeBox = new VBox(4, roomCodeLabel, hostInfoLabel, readOnly);
        codeBox.setAlignment(Pos.TOP_LEFT);
        codeBox.setStyle("-fx-background-color: #0d1117; -fx-border-color: #2d3748;"
                + " -fx-border-width: 1; -fx-padding: 16 20 16 20;");

        statusLabel = new Label("● connected — waiting for host to start…");
        statusLabel.getStyleClass().add("status-label");

        Label settingsTitle = new Label("GAME SETTINGS  (set by host)");
        settingsTitle.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px;"
                + " -fx-font-family: 'Courier New', monospace;");

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(12);
        settingsGrid.setVgap(8);
        addReadOnlyRow(settingsGrid, 0, "Max players", "…");
        addReadOnlyRow(settingsGrid, 1, "Games to win", "…");
        addReadOnlyRow(settingsGrid, 2, "Time limit (sec)", "…");
        addReadOnlyRow(settingsGrid, 3, "Speed", "…");
        addReadOnlyRow(settingsGrid, 4, "Grid size", "…");

        VBox settingsBox = new VBox(8, settingsTitle, settingsGrid);
        settingsBox.setStyle("-fx-background-color: #0d1117; -fx-border-color: #2d3748;"
                + " -fx-border-width: 1; -fx-padding: 14 20 14 20;");

        Label playersTitle = new Label("PLAYERS IN LOBBY");
        playersTitle.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px;"
                + " -fx-font-family: 'Courier New', monospace;");

        playerList = new VBox(4);
        addLoadingRow(playerList, "loading players…");

        VBox playersBox = new VBox(8, playersTitle, playerList);
        playersBox.setStyle("-fx-background-color: #0d1117; -fx-border-color: #2d3748;"
                + " -fx-border-width: 1; -fx-padding: 14 20 14 20;");

        Button waitingBtn = new Button("⏳  WAITING FOR HOST TO START…");
        waitingBtn.getStyleClass().add("tron-btn");
        waitingBtn.setDisable(true);
        waitingBtn.setStyle("-fx-opacity: 0.6;");

        Button back = new Button("←  LEAVE LOBBY");
        back.getStyleClass().add("tron-btn-secondary");
        back.setOnAction(e -> { client.close(); app.showMultiplayer(); });

        VBox left = new VBox(14,
            nav, title, subtitle,
            codeBox,
            statusLabel,
            settingsBox,
            playersBox,
            Theme.spacer(6),
            waitingBtn,
            Theme.divider(),
            back
        );
        left.setAlignment(Pos.TOP_LEFT);
        left.setMaxWidth(460);
        left.setMinWidth(460);

        VBox chatBox = buildChatPanel();

        HBox root = new HBox(24, left, chatBox);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40));

        rootPane.getChildren().setAll(root);

        client.onConnected = null;

        client.onLobbyUpdate = (names, settings) -> Platform.runLater(() -> {
            currentSettings = settings;
            refreshPlayerList(names);
            refreshSettingsGrid(settingsGrid, settings);
        });

        if (pendingLobbyNames != null && pendingLobbySettings != null) {
            currentSettings = pendingLobbySettings;
            refreshPlayerList(pendingLobbyNames);
            refreshSettingsGrid(settingsGrid, pendingLobbySettings);
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
            statusLabel.setText("⚠  " + name + " disconnected");
        });

        client.onGameStart = () -> Platform.runLater(() ->
            app.showMultiplayerClientGame(client, currentSettings, myName));

        client.onError = err -> Platform.runLater(() ->
            statusLabel.setText("✕ error: " + err));

        appendChat("SYSTEM", "Connected! Waiting for the host to start…");
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
        Label chatTitle = new Label("LOBBY CHAT");
        chatTitle.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11px;"
                + " -fx-font-family: 'Courier New', monospace;");

        chatMessages = new VBox(4);
        chatMessages.setPadding(new Insets(8));

        chatScroll = new ScrollPane(chatMessages);
        chatScroll.setFitToWidth(true);
        chatScroll.setPrefHeight(280);
        chatScroll.setStyle("-fx-background: #0d1117; -fx-background-color: #0d1117;");
        chatScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        chatInput = new TextField();
        chatInput.setPromptText("type a message…");
        chatInput.getStyleClass().add("tron-input");
        chatInput.setStyle("-fx-pref-width: 260px; -fx-min-width: 260px;");

        Button sendBtn = new Button("SEND");
        sendBtn.getStyleClass().add("tron-btn");
        sendBtn.setStyle("-fx-pref-width: 80px; -fx-min-width: 80px;");

        Runnable doSend = () -> {
            String msg = chatInput.getText().trim();
            if (msg.isEmpty()) return;
            client.sendChat(myName, msg);
            chatInput.clear();
        };
        sendBtn.setOnAction(e -> doSend.run());
        chatInput.setOnAction(e -> doSend.run());

        HBox inputRow = new HBox(8, chatInput, sendBtn);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(8, chatTitle, chatScroll, inputRow);
        panel.setStyle("-fx-background-color: #0d1117; -fx-border-color: #2d3748;"
                + " -fx-border-width: 1; -fx-padding: 10 15 10 15;");
        panel.setMinWidth(300);
        panel.setMaxWidth(300);
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