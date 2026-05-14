package com.example.controllers;

import com.example.Main;
import com.example.ui.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AskNameScreen {

    private final Main app;

    public AskNameScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        Label title = new Label("TRON: LIGHT CYCLES");
        title.getStyleClass().add("game-title");

        Label subtitle = new Label("enter your name to continue");
        subtitle.getStyleClass().add("screen-subtitle");

        TextField nameField = new TextField();
        nameField.getStyleClass().add("tron-input");
        nameField.setPromptText("username");

        Button confirm = new Button("PLAY  →");
        confirm.getStyleClass().add("tron-btn");

        Runnable submit = () -> {
            String raw = nameField.getText().trim();
            app.setPlayerName(raw.isEmpty() ? "PLAYER" : raw.toUpperCase());
            app.showMainMenu();
        };

        confirm.setOnAction(e -> submit.run());
        nameField.setOnAction(e -> submit.run());

        VBox content = new VBox(6, title, subtitle, Theme.spacer(20), nameField, Theme.spacer(4), confirm);
        content.setAlignment(Pos.TOP_LEFT);
        content.setMaxWidth(420);

        StackPane root = new StackPane(content);
        root.setAlignment(Pos.CENTER);
        StackPane.setMargin(content, new Insets(0, 0, 0, 0));
        Theme.apply(root);
        root.getStyleClass().add("screen-root");
        return root;
    }
}
