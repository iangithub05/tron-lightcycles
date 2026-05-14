package com.example.controllers;

import com.example.Main;
import com.example.models.GameMode;
import com.example.ui.Theme;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class JoinLobbyScreen {

    private final Main app;

    public JoinLobbyScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        Label nav = new Label("MAIN MENU  /  MULTIPLAYER  /  JOIN A GAME");
        nav.getStyleClass().add("nav-title");

        Label title = new Label("JOIN A GAME");
        title.getStyleClass().add("game-title");

        Label subtitle = new Label("enter the host's connection details");
        subtitle.getStyleClass().add("screen-subtitle");

        Label ipLabel = new Label("Host IP");
        ipLabel.getStyleClass().add("tron-label");

        TextField ipField = new TextField("127.0.0.1");
        ipField.getStyleClass().add("tron-input");

        Label portLabel = new Label("Port");
        portLabel.getStyleClass().add("tron-label");

        TextField portField = new TextField("5555");
        portField.getStyleClass().add("tron-input");

        Label statusLabel = new Label("not connected");
        statusLabel.getStyleClass().add("status-label");

        Button connect = new Button("CONNECT");
        connect.getStyleClass().add("tron-btn");

        connect.setOnAction(e -> {
            statusLabel.setText("● connecting to " + ipField.getText().trim() + ":" + portField.getText().trim() + "...");
        });

        Button back = new Button("←  BACK");
        back.getStyleClass().add("tron-btn-secondary");
        back.setOnAction(e -> app.showMultiplayer());

        VBox content = new VBox(6,
            nav,
            title,
            subtitle,
            ipLabel,
            ipField,
            Theme.spacer(4),
            portLabel,
            portField,
            Theme.spacer(4),
            connect,
            statusLabel,
            Theme.spacer(10),
            Theme.divider(),
            Theme.spacer(4),
            back
        );
        content.setAlignment(Pos.TOP_LEFT);
        content.setMaxWidth(420);

        StackPane root = new StackPane(content);
        root.setAlignment(Pos.CENTER);
        Theme.apply(root);
        root.getStyleClass().add("screen-root");
        return root;
    }
}
