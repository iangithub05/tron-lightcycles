package com.example.controllers;

import com.example.Main;
import com.example.models.GameMode;
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

        Label subtitle = new Label("HELLO VINCE EDIT MO NALANG HEREE");
        subtitle.getStyleClass().add("screen-subtitle");

        Button back = new Button("←  BACK");
        back.getStyleClass().add("tron-btn-secondary");
        back.setOnAction(e -> app.showMainMenu());

        VBox content = new VBox(6,
            nav,
            title,
            subtitle,
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
