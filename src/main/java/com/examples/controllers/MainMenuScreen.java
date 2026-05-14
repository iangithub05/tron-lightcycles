package com.example.controllers;

import com.example.Main;
import com.example.ui.Theme;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainMenuScreen {

    private final Main app;

    public MainMenuScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        Label nav = new Label("TRON: LIGHT CYCLES");
        nav.getStyleClass().add("nav-title");

        Label title = new Label(app.getPlayerName());
        title.getStyleClass().add("game-title");

        Label subtitle = new Label("what do you want to do?");
        subtitle.getStyleClass().add("screen-subtitle");

        Button singleplayer = new Button("SINGLEPLAYER");
        Button multiplayer = new Button("MULTIPLAYER");
        Button about = new Button("ABOUT");

        singleplayer.getStyleClass().add("tron-btn");
        multiplayer.getStyleClass().add("tron-btn");
        about.getStyleClass().add("tron-btn-secondary");

        singleplayer.setOnAction(e -> app.showSingleplayer());
        multiplayer.setOnAction(e -> app.showMultiplayer());
        about.setOnAction(e -> app.showAbout());

        VBox content = new VBox(6,
            nav,
            title,
            subtitle,
            singleplayer,
            multiplayer,
            Theme.spacer(10),
            Theme.divider(),
            Theme.spacer(4),
            about
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
