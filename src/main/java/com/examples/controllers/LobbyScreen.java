package com.example.controllers;

import com.example.Main;
import com.example.network.GameClient;
import com.example.network.GameServer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Lobby screen shown when the player picks "Multiplayer" from the main menu.
 *
 * Layout:
 *  ┌─────────────────────────────┐
 *  │      MULTIPLAYER LOBBY      │
 *  │                             │
 *  │  ── Host ──                 │
 *  │  Port: [____]  [Host Game]  │
 *  │                             │
 *  │  ── Join ──                 │
 *  │  IP:   [____]               │
 *  │  Port: [____]  [Join Game]  │
 *  │                             │
 *  │  Status: Waiting…           │
 *  │                             │
 *  │           [Back]            │
 *  └─────────────────────────────┘
 */
public class LobbyScreen {

    private static final int DEFAULT_PORT = 5555;

    private final Main app;

    private GameServer activeServer;
    private GameClient activeClient;

    private Label statusLabel;

    public LobbyScreen(Main app) {
        this.app = app;
    }

    public VBox getView() {
        // ── Title ─────────────────────────────────────────────────────────
        Label title = new Label("MULTIPLAYER LOBBY");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        title.setTextFill(Color.CYAN);

        // ── Host section ──────────────────────────────────────────────────
        Label hostLabel = new Label("── Host a Game ──");
        hostLabel.setTextFill(Color.LIMEGREEN);

        TextField hostPortField = new TextField(String.valueOf(DEFAULT_PORT));
        hostPortField.setPromptText("Port");
        hostPortField.setMaxWidth(80);

        Button hostBtn = new Button("Host Game");
        hostBtn.setOnAction(e -> doHost(hostPortField.getText().trim()));

        HBox hostRow = new HBox(10, new Label("Port:"), hostPortField, hostBtn);
        hostRow.setAlignment(Pos.CENTER);

        // ── Join section ──────────────────────────────────────────────────
        Label joinLabel = new Label("── Join a Game ──");
        joinLabel.setTextFill(Color.ORANGERED);

        TextField joinIpField = new TextField("127.0.0.1");
        joinIpField.setPromptText("Host IP");
        joinIpField.setMaxWidth(130);

        TextField joinPortField = new TextField(String.valueOf(DEFAULT_PORT));
        joinPortField.setPromptText("Port");
        joinPortField.setMaxWidth(80);

        Button joinBtn = new Button("Join Game");
        joinBtn.setOnAction(e -> doJoin(joinIpField.getText().trim(), joinPortField.getText().trim()));

        HBox joinRow = new HBox(10,
                new Label("IP:"), joinIpField,
                new Label("Port:"), joinPortField,
                joinBtn);
        joinRow.setAlignment(Pos.CENTER);

        // ── Status ────────────────────────────────────────────────────────
        statusLabel = new Label("Choose to host or join a game.");
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setFont(Font.font("Monospace", 13));

        // ── Back button ───────────────────────────────────────────────────
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> {
            cleanup();
            app.showMainMenu();
        });

        // ── Layout ────────────────────────────────────────────────────────
        VBox root = new VBox(18,
                title,
                hostLabel, hostRow,
                joinLabel, joinRow,
                statusLabel,
                backBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #0a0a1e;");

        // Style labels to match game theme
        for (var node : new Label[]{hostLabel, joinLabel}) {
            node.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        }

        return root;
    }

    // ── Host flow ─────────────────────────────────────────────────────────

    private void doHost(String portText) {
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            setStatus("Invalid port number.", Color.ORANGERED);
            return;
        }

        cleanup();
        setStatus("Waiting for opponent on port " + port + "…", Color.YELLOW);

        activeServer = new GameServer();

        final int finalPort = port;
        Thread t = new Thread(() -> {
            activeServer.onGuestConnected = __ -> Platform.runLater(() -> {
                setStatus("Opponent connected! Starting game…", Color.LIMEGREEN);
                // Brief pause so the status is visible, then launch
                new Thread(() -> {
                    try { Thread.sleep(600); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> {
                        activeServer.startGame();
                        app.showMultiplayerGame(activeServer);
                    });
                }).start();
            });

            activeServer.onError = err ->
                    Platform.runLater(() -> setStatus("Error: " + err, Color.RED));

            activeServer.listen(finalPort);
        }, "server-listen");
        t.setDaemon(true);
        t.start();
    }

    // ── Join flow ─────────────────────────────────────────────────────────

    private void doJoin(String ip, String portText) {
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            setStatus("Invalid port number.", Color.ORANGERED);
            return;
        }

        cleanup();
        setStatus("Connecting to " + ip + ":" + port + "…", Color.YELLOW);

        activeClient = new GameClient();

        activeClient.onConnected = slot ->
                Platform.runLater(() -> {
                    setStatus("Connected! Waiting for host to start…", Color.LIMEGREEN);
                    app.showMultiplayerGame(activeClient);
                });

        activeClient.onError = err ->
                Platform.runLater(() -> setStatus("Error: " + err, Color.RED));

        final int finalPort = port;
        Thread t = new Thread(() -> activeClient.connect(ip, finalPort), "client-connect");
        t.setDaemon(true);
        t.start();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void setStatus(String msg, Color color) {
        statusLabel.setText("● " + msg);
        statusLabel.setTextFill(color);
    }

    private void cleanup() {
        if (activeServer != null) { activeServer.close(); activeServer = null; }
        if (activeClient != null) { activeClient.close(); activeClient = null; }
    }
}
