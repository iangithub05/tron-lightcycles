package com.example.controllers;

import com.example.Main;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class AboutScreen {

    private final Main app;

    public AboutScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        // --- Main Root Canvas ---
        StackPane root = new StackPane();
        root.setBackground(UIHelper.createBackground("/images/background_5.png"));

        BorderPane layoutFrame = new BorderPane();

        // --- Top Header Panel ---
        HBox topBar = UIHelper.createNavigationBar("|  ABOUT", "WELCOME, " + app.getPlayerName());
        
        // Target and custom-style the injected right-side player welcome element from UIHelper
        Label welcomeLabel = (Label) topBar.getChildren().get(2);
        welcomeLabel.setPadding(new Insets(12, 30, 12, 30));
        welcomeLabel.setStyle("-fx-background-color: linear-gradient(to right, #801a33, #4d1020); -fx-background-radius: 20; -fx-text-fill: white;");
        welcomeLabel.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 22));

        layoutFrame.setTop(topBar);

        // --- Center Canvas: Split Content Area ---
        HBox splitBody = new HBox(100); 
        splitBody.setAlignment(Pos.CENTER);
        splitBody.setPadding(new Insets(20, 40, 20, 40));

        // ================= LEFT COLUMN: KEYBOARD MATRIX =================
        VBox leftColumn = new VBox(40);
        leftColumn.setAlignment(Pos.CENTER);

        // WASD Input Box Cluster Node
        VBox wasdGroup = new VBox(8);
        wasdGroup.setAlignment(Pos.CENTER);
        
        HBox wRow = new HBox(); 
        wRow.setAlignment(Pos.CENTER);
        wRow.getChildren().add(createKeyCapNode("W"));
        
        HBox asdRow = new HBox(8); 
        asdRow.setAlignment(Pos.CENTER);
        asdRow.getChildren().addAll(createKeyCapNode("A"), createKeyCapNode("S"), createKeyCapNode("D"));
        wasdGroup.getChildren().addAll(wRow, asdRow);

        // Arrow Keys Input Box Cluster Node
        VBox arrowGroup = new VBox(8);
        arrowGroup.setAlignment(Pos.CENTER);
        
        HBox upRow = new HBox(); 
        upRow.setAlignment(Pos.CENTER);
        upRow.getChildren().add(createKeyCapNode("↑"));
        
        HBox downRow = new HBox(8); 
        downRow.setAlignment(Pos.CENTER);
        downRow.getChildren().addAll(createKeyCapNode("←"), createKeyCapNode("↓"), createKeyCapNode("→"));
        arrowGroup.getChildren().addAll(upRow, downRow);

        // Controls Baseline Subtitle Label Text
        Label controlsLabel = new Label("Controls");
        controlsLabel.setTextFill(Color.WHITE);
        controlsLabel.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 32));

        leftColumn.getChildren().addAll(wasdGroup, arrowGroup, controlsLabel);

        // ================= RIGHT COLUMN: INFO PANEL =================
        VBox rightColumn = new VBox(25);
        rightColumn.setAlignment(Pos.CENTER);

        Label titleMain = new Label("TRON");
        titleMain.setTextFill(Color.WHITE);
        titleMain.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 100));

        Label titleSub = new Label("LIGHT CYCLES");
        titleSub.setTextFill(Color.WHITE);
        titleSub.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 42));

        Label instructions = new Label(
            "Outmaneuver your opponents. Leave a trail. Force them to crash. Don't crash yourself."
        );
        instructions.setTextFill(Color.WHITE);
        instructions.setWrapText(true);
        instructions.setMaxWidth(500);
        instructions.setStyle("-fx-text-alignment: center; -fx-line-spacing: 12px;");
        instructions.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.NORMAL, 20));
        instructions.setPadding(new Insets(15, 0, 25, 0));

        Button backBtn = new Button("BACK TO MENU");
        backBtn.setPrefWidth(450);
        backBtn.setPrefHeight(80);
        backBtn.setCursor(Cursor.HAND);
        backBtn.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 22));
        
        backBtn.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #801a33, #4d1020);" +
            "-fx-text-fill: #ffffff;" +
            "-fx-background-radius: 28;" +
            "-fx-border-color: #380b17;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 28;"
        );
        
        addButtonEffects(backBtn);
        backBtn.setOnAction(e -> app.showMainMenu());

        rightColumn.getChildren().addAll(titleMain, titleSub, instructions, backBtn);
        splitBody.getChildren().addAll(rightColumn, leftColumn);
        layoutFrame.setCenter(splitBody);

        // --- Bottom Window Navigation Footer ---
        // Dynamically built using UIHelper.createNavigationBar. This fires System.exit(0) automatically when "QUIT" is triggered.
        HBox bottomBar = UIHelper.createNavigationBar("TRON: LIGHT CYCLES", "QUIT");
        layoutFrame.setBottom(bottomBar);

        root.getChildren().add(layoutFrame);
        return root;
    }

    private StackPane createKeyCapNode(String keySymbol) {
        StackPane keyCap = new StackPane();
        keyCap.setPrefSize(75, 75);
        keyCap.setMinSize(75, 75);
        keyCap.setMaxSize(75, 75);
        
        keyCap.setStyle(
            "-fx-background-color: #21283d;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(255,255,255,0.1);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;"
        );

        Label label = new Label(keySymbol);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 24));
        
        keyCap.getChildren().add(label);
        return keyCap;
    }

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