package com.example.controllers;

import com.example.Main;
import com.example.engine.GameEngine;
import com.example.services.GameSession;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class GameScreen {

    private final Main app;
    private final GameSession session;

    public GameScreen(Main app, GameSession session) {
        this.app = app;
        this.session = session;
    }

    public StackPane getView() {
        GameEngine engine = new GameEngine(app, session);
        Pane canvas = engine.buildCanvas();

        StackPane root = new StackPane(canvas);
        root.setStyle("-fx-background-color: black;");
        engine.start();
        return root;
    }
}
