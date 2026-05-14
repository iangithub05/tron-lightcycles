package com.example.controllers;

import com.example.Main;
import com.example.services.GameSession;
import com.example.ui.Theme;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GameOverScreen {

    private final Main app;
    private final String result;
    private final GameSession session;

    public GameOverScreen(Main app, String result, GameSession session) {
        this.app = app;
        this.result = result;
        this.session = session;
    }

    public StackPane getView() {
        Label nav = new Label("GAME OVER");
        nav.getStyleClass().add("nav-title");

        Label title = new Label(result);
        title.getStyleClass().add("game-title");

        Button retry = new Button("PLAY AGAIN  →");
        retry.getStyleClass().add("tron-btn");
        retry.setDisable(session == null);
        retry.setOnAction(e -> {
            session.restart();
            app.showGame(session);
        });

        Button menu = new Button("←  MAIN MENU");
        menu.getStyleClass().add("tron-btn-secondary");
        menu.setOnAction(e -> app.showMainMenu());

        VBox content = new VBox(6,
            nav,
            title,
            Theme.spacer(20),
            retry,
            menu
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
