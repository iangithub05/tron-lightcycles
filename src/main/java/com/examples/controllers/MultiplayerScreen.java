package com.example.controllers;

import com.example.Main;
import com.example.ui.Theme;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MultiplayerScreen {

    private final Main app;

    public MultiplayerScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        Label nav = new Label("MAIN MENU  /  MULTIPLAYER");
        nav.getStyleClass().add("nav-title");

        Label title = new Label("MULTIPLAYER");
        title.getStyleClass().add("game-title");

        Label subtitle = new Label("host or join a game");
        subtitle.getStyleClass().add("screen-subtitle");

        Button host = new Button("HOST A GAME");
        Button join = new Button("JOIN A GAME");

        host.getStyleClass().add("tron-btn");
        join.getStyleClass().add("tron-btn");

        host.setOnAction(e -> app.showHostLobby());
        join.setOnAction(e -> app.showJoinLobby());

        Button back = new Button("←  BACK");
        back.getStyleClass().add("tron-btn-secondary");
        back.setOnAction(e -> app.showMainMenu());

        VBox content = new VBox(6,
            nav,
            title,
            subtitle,
            host,
            join,
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
