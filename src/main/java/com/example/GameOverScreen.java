package com.example;

import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Pos;

public class GameOverScreen {

    private final Main app;
    private final String result;

    public GameOverScreen(Main app, String result) {
        this.app = app;
        this.result = result;
    }

    public VBox getView() {
        Label label = new Label(result);

        Button retry = new Button("Retry");
        Button menu = new Button("Main Menu");

        retry.setOnAction(e -> app.showGame());
        menu.setOnAction(e -> app.showMainMenu());

        VBox layout = new VBox(20, label, retry, menu);
        layout.setAlignment(Pos.CENTER);

        return layout;
    }
}