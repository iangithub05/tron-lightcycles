package com.example.controllers;

import com.example.Main;
import com.example.models.Difficulty;
import com.example.services.SinglePlayerSession;
import com.example.ui.Theme;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SingleplayerScreen {

    private final Main app;

    public SingleplayerScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        Label nav = new Label("MAIN MENU  /  SINGLEPLAYER");
        nav.getStyleClass().add("nav-title");

        Label title = new Label("SINGLEPLAYER");
        title.getStyleClass().add("game-title");

        Label subtitle = new Label("SELECT DIFFICULTY  —  1 HUMAN  VS  3 AI");
        subtitle.getStyleClass().add("screen-subtitle");

        Button easy = new Button("EASY");
        easy.getStyleClass().add("tron-btn-secondary");
        easy.setOnAction(e -> startGame(Difficulty.EASY));

        Button medium = new Button("MEDIUM");
        medium.getStyleClass().add("tron-btn");
        medium.setOnAction(e -> startGame(Difficulty.MEDIUM));

        Button hard = new Button("HARD");
        hard.getStyleClass().add("tron-btn-secondary");
        hard.setOnAction(e -> startGame(Difficulty.HARD));

        Button back = new Button("←  BACK");
        back.getStyleClass().add("tron-btn-secondary");
        back.setOnAction(e -> app.showMainMenu());

        VBox content = new VBox(6,
            nav,
            title,
            subtitle,
            Theme.spacer(10),
            Theme.divider(),
            Theme.spacer(8),
            easy,
            medium,
            hard,
            Theme.spacer(14),
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

    private void startGame(Difficulty difficulty) {
        app.showGame(new SinglePlayerSession(difficulty));
    }
}
