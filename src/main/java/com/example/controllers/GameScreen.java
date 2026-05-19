package com.example.controllers;

import com.example.Main;
import com.example.engine.GameEngine;
import com.example.services.GameSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class GameScreen {

    private final Main app;
    private final GameSession session;

    public GameScreen(Main app, GameSession session) {
        this.app = app;
        this.session = session;
    }

    public StackPane getView() {

        // ================= GAME ENGINE =================
        GameEngine engine = new GameEngine(app, session);
        Pane canvas = engine.buildCanvas();

        // ================= ROOT =================
        StackPane root = new StackPane();
        root.setBackground(UIHelper.createBackground("/images/background_2.png"));

        BorderPane layoutFrame = new BorderPane();

        // =====================================================
        // TOP BAR
        // =====================================================
        HBox topBar = UIHelper.createNavigationBar("|  SINGLEPLAYER", "WELCOME, " + app.getPlayerName());

        // Style the welcome label
        Label welcomeLabel = (Label) topBar.getChildren().get(2);
        welcomeLabel.setPadding(new Insets(12, 30, 12, 30));
        welcomeLabel.setStyle("-fx-background-color: linear-gradient(to right, #801a33, #4d1020); -fx-background-radius: 15;");

        layoutFrame.setTop(topBar);

        // =====================================================
        // CENTER GAME CANVAS
        // =====================================================
        StackPane centerWrapper = new StackPane(canvas);
        centerWrapper.setAlignment(Pos.CENTER);

        layoutFrame.setCenter(centerWrapper);

        // =====================================================
        // BOTTOM BAR
        // =====================================================
        HBox bottomBar = UIHelper.createNavigationBar("TRON: LIGHT CYCLES", "QUIT");

        layoutFrame.setBottom(bottomBar);

        // =====================================================
        // FINAL ROOT
        // =====================================================
        root.getChildren().add(layoutFrame);

        // Start game
        engine.start();

        return root;
    }
}