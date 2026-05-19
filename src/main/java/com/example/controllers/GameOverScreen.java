package com.example.controllers;

import com.example.Main;
import com.example.services.GameSession;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

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
        // --- Main Screen Canvas Base Layer ---
        StackPane root = new StackPane();
        root.setBackground(UIHelper.createBackground("/images/background_4.png"));

        BorderPane layoutStructure = new BorderPane();

        // --- Top Bar Layout ---
        HBox topBar = UIHelper.createNavigationBar("|  GAME OVER", "Welcome, " + app.getPlayerName());
        
        Label welcomeLabel = (Label) topBar.getChildren().get(2);
        welcomeLabel.setPadding(new Insets(12, 30, 12, 30));
        welcomeLabel.setStyle(
            "-fx-background-color: linear-gradient(to right, #801a33, #4d1020);" +
            "-fx-background-radius: 15;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        layoutStructure.setTop(topBar);

        // --- Centered Status Display Container ---
        VBox centerContent = new VBox(30); // Decreased slightly to account for the multi-line text
        centerContent.setAlignment(Pos.CENTER);

        // --- Two-Line Strict Typography Layout ---
        VBox outcomeTextFlow = new VBox(5);
        outcomeTextFlow.setAlignment(Pos.CENTER);

        Label youLabel = new Label("YOU");
        youLabel.setTextFill(Color.WHITE);
        youLabel.setStyle(
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 100px;" +
            "-fx-font-weight: bold;"
        );

        String dynamicStatus = (result != null && result.toUpperCase().contains("WIN")) ? "WIN" : "LOSE";
        Label statusLabel = new Label(dynamicStatus);
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setStyle(
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 100px;" +
            "-fx-font-weight: bold;"
        );

        outcomeTextFlow.getChildren().addAll(youLabel, statusLabel);

        // Interactive Options Menu Layout
        VBox actionMenu = new VBox(15);
        actionMenu.setAlignment(Pos.CENTER);

        // "PLAY AGAIN" Green Pill-Button Setup
        Button retryButton = new Button("PLAY AGAIN");
        retryButton.setPrefWidth(550);
        retryButton.setPrefHeight(90);
        retryButton.setCursor(Cursor.HAND);
        retryButton.setDisable(session == null);
        retryButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #23402c, #16261b);" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 45;" +
            "-fx-border-color: #122117;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 45;"
        );

        // "BACK TO MENU" Maroon Pill-Button Setup
        Button menuButton = new Button("BACK TO MENU");
        menuButton.setPrefWidth(550);
        menuButton.setPrefHeight(90);
        menuButton.setCursor(Cursor.HAND);
        menuButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #801a33, #4d1020);" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 45;" +
            "-fx-border-color: #380b17;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 45;"
        );

        // --- Apply Hover Animations (Bulge + Opacity Transition) ---
        addButtonEffects(retryButton);
        addButtonEffects(menuButton);

        // Logic action routing setups
        retryButton.setOnAction(e -> {
            if (session != null) {
                session.restart();
                app.showGame(session);
            }
        });
        menuButton.setOnAction(e -> app.showMainMenu());

        actionMenu.getChildren().addAll(retryButton, menuButton);
        centerContent.getChildren().addAll(outcomeTextFlow, actionMenu);
        layoutStructure.setCenter(centerContent);

        // --- Full-Width Bottom Bar Layout Footer ---
        HBox bottomBar = UIHelper.createNavigationBar("TRON: Light Cycles", "QUIT");
        layoutStructure.setBottom(bottomBar);

        root.getChildren().add(layoutStructure);
        return root;
    }

    /**
     * Helper method to attach smooth scaling/bulging effects alongside opacity drops on hover
     */
    private void addButtonEffects(Button targetButton) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), targetButton);
        
        targetButton.setOnMouseEntered(e -> {
            targetButton.setOpacity(0.85);
            scaleTransition.stop();
            scaleTransition.setToX(1.05); // Standard 5% scale expansion outward
            scaleTransition.setToY(1.05);
            scaleTransition.play();
        });

        targetButton.setOnMouseExited(e -> {
            targetButton.setOpacity(1.0);
            scaleTransition.stop();
            scaleTransition.setToX(1.0); // Snap back cleanly to standard scale boundaries
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
    }
}