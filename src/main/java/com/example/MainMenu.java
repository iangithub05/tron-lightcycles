package com.example;

import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.geometry.Pos;

public class MainMenu {

    private final Main app;

    public MainMenu(Main app) {
        this.app = app;
    }

    public VBox getView() {
        Button start = new Button("Start Game");
        Button exit = new Button("Exit");

        start.setOnAction(e -> app.showGame());
        exit.setOnAction(e -> System.exit(0));

        VBox layout = new VBox(20, start, exit);
        layout.setAlignment(Pos.CENTER);

        return layout;
    }
}