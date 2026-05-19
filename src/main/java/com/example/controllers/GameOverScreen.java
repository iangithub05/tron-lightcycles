package com.example.controllers;

import com.example.Main;
import com.example.services.GameSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

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
        // Generates the exact top-bar layout matching your title text formatting rules
        HBox topBar = UIHelper.createNavigationBar("|  GAME OVER", "Welcome, " + app.getPlayerName());
        
        // Grab the username badge element directly to replicate your maroon pill design
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
        VBox centerContent = new VBox(40);
        centerContent.setAlignment(Pos.CENTER);

        // Dynamic result checking to display either "YOU LOSE" or "YOU WIN" in massive retro typography
        String outcomeText = (result != null && result.toUpperCase().contains("WIN")) ? "YOU WIN" : "YOU LOSE";
        
        Label outcomeLabel = new Label(outcomeText);
        outcomeLabel.setTextFill(Color.WHITE);
        outcomeLabel.setStyle(
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 110px;" +
            "-fx-text-alignment: center;"
        );

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

        // Subtle opacity adjustments during hover states for a crisp user interface feel
        retryButton.setOnMouseEntered(e -> retryButton.setOpacity(0.85));
        retryButton.setOnMouseExited(e -> retryButton.setOpacity(1.0));
        menuButton.setOnMouseEntered(e -> menuButton.setOpacity(0.85));
        menuButton.setOnMouseExited(e -> menuButton.setOpacity(1.0));

        // Preserves your logical actions seamlessly
        retryButton.setOnAction(e -> {
            if (session != null) {
                session.restart();
                app.showGame(session);
            }
        });
        menuButton.setOnAction(e -> app.showMainMenu());

        actionMenu.getChildren().addAll(retryButton, menuButton);
        centerContent.getChildren().addAll(outcomeLabel, actionMenu);
        layoutStructure.setCenter(centerContent);

        // --- Full-Width Bottom Bar Layout Footer ---
        HBox bottomBar = UIHelper.createNavigationBar("TRON: LIGHT CYCLES", "QUIT");
        layoutStructure.setBottom(bottomBar);

        root.getChildren().add(layoutStructure);
        return root;
    }
}