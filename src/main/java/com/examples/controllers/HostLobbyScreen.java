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

public class HostLobbyScreen {

    private final Main app;

    public HostLobbyScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        Label nav = new Label("MAIN MENU  /  MULTIPLAYER  /  HOST A GAME");
        nav.getStyleClass().add("nav-title");

        Label title = new Label("HOST A GAME");
        title.getStyleClass().add("game-title");

        Label subtitle = new Label("share your details with a friend to join");
        subtitle.getStyleClass().add("screen-subtitle");

        Label portLabel = new Label("Port");
        portLabel.getStyleClass().add("tron-label");

        TextField portField = new TextField("5555");
        portField.getStyleClass().add("tron-input");

        Label statusLabel = new Label("not hosting yet");
        statusLabel.getStyleClass().add("status-label");

        Button startHosting = new Button("START HOSTING");
        startHosting.getStyleClass().add("tron-btn");

        Button configureGame = new Button("CONFIGURE GAME");
        configureGame.getStyleClass().add("tron-btn");

        startHosting.setOnAction(e -> {
            statusLabel.setText("● waiting for opponent on port " + portField.getText().trim() + "...");
        });

        Button back = new Button("←  BACK");
        back.getStyleClass().add("tron-btn-secondary");
        back.setOnAction(e -> app.showMultiplayer());

        VBox content = new VBox(6,
            nav,
            title,
            subtitle,
            portLabel,
            portField,
            Theme.spacer(4),
            startHosting,
            configureGame,
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
