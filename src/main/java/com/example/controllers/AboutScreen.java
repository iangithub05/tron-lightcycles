package com.example.controllers;

import com.example.Main;
import com.example.ui.Theme;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AboutScreen {

    private final Main app;

    public AboutScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        Label nav = new Label("MAIN MENU  /  ABOUT");
        nav.getStyleClass().add("nav-title");

        Label title = new Label("ABOUT");
        title.getStyleClass().add("game-title");

        Label body = new Label(
            "TRON: LIGHT CYCLES\n\n" +
            "Outmaneuver your opponents.\n" +
            "Leave a trail. Force them to crash.\n" +
            "Don't crash yourself.\n\n" +
            "Controls\n" +
            "  Move       WASD / Arrow Keys\n" +
            "  Pause      ESC\n\n" +
            "Built with JavaFX."
        );
        body.getStyleClass().add("tron-label");
        body.setWrapText(true);
        body.setMaxWidth(420);

        Button back = new Button("←  BACK");
        back.getStyleClass().add("tron-btn-secondary");
        back.setOnAction(e -> app.showMainMenu());

        VBox content = new VBox(6,
            nav,
            title,
            Theme.spacer(16),
            body,
            Theme.spacer(24),
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
