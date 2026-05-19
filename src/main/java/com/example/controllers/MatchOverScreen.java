package com.example.controllers;

import com.example.Main;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class MatchOverScreen {

    private final Main app;
    private final String result;
    private final int[] scores;

    public MatchOverScreen(Main app, String result, int[] scores) {
        this.app = app;
        this.result = result;
        this.scores = scores;
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
            "-fx-background-radius: 20;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );
        layoutStructure.setTop(topBar);

        // --- Centered Status & Content Container ---
        VBox centerContent = new VBox(25); 
        centerContent.setAlignment(Pos.CENTER);

        // --- Strict Multi-Line Header (YOU \n WON vs P1 \n WON THE GAME) ---
        VBox outcomeTextFlow = new VBox(5);
        outcomeTextFlow.setAlignment(Pos.CENTER);

        Label line1Label = new Label();
        Label line2Label = new Label();

        // Check if the current player won
        if (result != null && result.contains("WON")) {
            line1Label.setText("YOU");
            line2Label.setText("WON");
        } else {
            // Find which player won by scanning the scores array for the highest score
            int winningPlayerNum = 1; 
            int maxScore = -1;
            
            if (scores != null) {
                for (int i = 0; i < scores.length; i++) {
                    if (scores[i] > maxScore) {
                        maxScore = scores[i];
                        winningPlayerNum = i + 1;
                    }
                }
            }
            line1Label.setText("P" + winningPlayerNum);
            line2Label.setText("WON THE GAME");
        }

        // Apply matching typography styles to both header lines
        line1Label.setTextFill(Color.WHITE);
        line1Label.setStyle(
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 90px;" + // Scaled down slightly to fit "WON THE GAME" perfectly
            "-fx-font-weight: bold;"
        );

        line2Label.setTextFill(Color.WHITE);
        line2Label.setStyle(
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 75px;" + 
            "-fx-font-weight: bold;"
        );

        outcomeTextFlow.getChildren().addAll(line1Label, line2Label);

        // --- Dark Overlay Score Card Block ---
        VBox scoreCardOverlay = new VBox(15);
        scoreCardOverlay.setAlignment(Pos.CENTER);
        scoreCardOverlay.setMaxWidth(550);
        scoreCardOverlay.setPadding(new Insets(25, 40, 25, 40));
        scoreCardOverlay.setStyle(
            "-fx-background-color: rgba(30, 30, 30, 0.75);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: rgba(255, 255, 255, 0.15);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 20;"
        );

        Label scTitle = new Label("FINAL SCORE");
        scTitle.setTextFill(Color.WHITE);
        scTitle.setStyle(
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;"
        );
        scoreCardOverlay.getChildren().add(scTitle);

        if (scores != null) {
            VBox scoresListBlock = new VBox(10);
            scoresListBlock.setAlignment(Pos.CENTER);
            
            for (int i = 0; i < scores.length; i++) {
                Label sc = new Label("P" + (i + 1) + ":  " + scores[i]);
                sc.setTextFill(Color.WHITE);
                sc.setStyle(
                    "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
                    "-fx-font-size: 26px;" +
                    "-fx-font-weight: bold;"
                );
                scoresListBlock.getChildren().add(sc);
            }
            scoreCardOverlay.getChildren().add(scoresListBlock);
        }

        // --- Horizontal Action Buttons Layout ---
        HBox actionMenu = new HBox(20);
        actionMenu.setAlignment(Pos.CENTER);

        // "PLAY AGAIN" Button setup
        Button retryButton = new Button("PLAY AGAIN");
        retryButton.setPrefWidth(265);
        retryButton.setPrefHeight(65);
        retryButton.setCursor(Cursor.HAND);
        retryButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #23402c, #16261b);" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 45;" +
            "-fx-border-color: #122117;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 45;"
        );

        // "BACK TO MENU" Button setup
        Button menuButton = new Button("BACK TO MENU");
        menuButton.setPrefWidth(265);
        menuButton.setPrefHeight(65);
        menuButton.setCursor(Cursor.HAND);
        menuButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #801a33, #4d1020);" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 45;" +
            "-fx-border-color: #380b17;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 45;"
        );

        // --- Apply Interactive Scaling Animations ---
        addButtonEffects(retryButton);
        addButtonEffects(menuButton);

        retryButton.setOnAction(e -> app.showMultiplayer());
        menuButton.setOnAction(e -> app.showMainMenu());

        actionMenu.getChildren().addAll(retryButton, menuButton);
        
        centerContent.getChildren().addAll(outcomeTextFlow, scoreCardOverlay, actionMenu);
        layoutStructure.setCenter(centerContent);

        // --- Bottom Window Navigation Footer ---
        HBox bottomBar = UIHelper.createNavigationBar("TRON: Light Cycles", "QUIT");
        bottomBar.getChildren().remove(0); 
        layoutStructure.setBottom(bottomBar);

        root.getChildren().add(layoutStructure);
        return root;
    }

    private void addButtonEffects(Button targetButton) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(120), targetButton);
        
        targetButton.setOnMouseEntered(e -> {
            targetButton.setOpacity(0.85);
            scaleTransition.stop();
            scaleTransition.setToX(1.06); 
            scaleTransition.setToY(1.06);
            scaleTransition.play();
        });

        targetButton.setOnMouseExited(e -> {
            targetButton.setOpacity(1.0);
            scaleTransition.stop();
            scaleTransition.setToX(1.0); 
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
    }
}
