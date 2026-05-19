package com.example.controllers;

import com.example.Main;
import com.example.ui.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AskNameScreen {

    private final Main app;

    public AskNameScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        // --- Root Scene Frame Container ---
        StackPane root = new StackPane();
        Theme.apply(root);
        root.getStyleClass().add("screen-root");
        
        // Deep solid dark workspace backdrop styling
        root.setStyle("-fx-background-color: #15171c;");

        // --- Center Dialog Modal Window Panel ---
        VBox dialogCard = new VBox(0); // 0 spacing to keep items perfectly flush
        dialogCard.setAlignment(Pos.TOP_CENTER);
        dialogCard.setMaxWidth(950); // Increased width
        dialogCard.setMaxHeight(500); // Adjusted height for a wider aspect ratio
        
        // Crisp clean white card canvas with rounded corners matching the reference image
        dialogCard.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-background-radius: 20;" +
            "-fx-border-radius: 20;" +
            "-fx-border-color: transparent;"
        );

        // --- Card Top Header Section Layout ---
        VBox topHeaderSection = new VBox(5);
        topHeaderSection.setAlignment(Pos.CENTER);
        topHeaderSection.setPadding(new Insets(40, 50, 25, 50));

        Label title = new Label("TRON");
        title.setTextFill(Color.web("#2c2d35"));
        // Updated to theme-consistent pixel font
        title.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 70));

        Label subtitle = new Label("Light Cycles");
        subtitle.setTextFill(Color.web("#2c2d35"));
        subtitle.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 28));

        // Thin elegant separation divider line between header section and input zone
        VBox horizontalDivider = new VBox();
        horizontalDivider.setMinHeight(4);
        horizontalDivider.setMaxHeight(4);
        horizontalDivider.setStyle("-fx-background-color: #dbdcde; -fx-background-radius: 2;");
        VBox.setMargin(horizontalDivider, new Insets(15, 0, 5, 0));

        topHeaderSection.getChildren().addAll(title, subtitle, horizontalDivider);

        // --- Card Interactive Input Form Zone ---
        VBox inputFormSection = new VBox(15);
        inputFormSection.setAlignment(Pos.CENTER_LEFT);
        inputFormSection.setPadding(new Insets(10, 65, 45, 65));

        Label instructionLabel = new Label("Enter a username, or leave it blank.");
        instructionLabel.setTextFill(Color.BLACK);
        instructionLabel.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 18)); // Increased font size

        TextField nameField = new TextField();
        nameField.setPromptText("USERNAME");
        nameField.setPrefHeight(85); // Made the field taller
        
        // Custom styling replicating your exact pitch black solid entry look
        nameField.setStyle(
            "-fx-background-color: #000000;" +
            "-fx-text-fill: #9a9b9d;" +
            "-fx-prompt-text-fill: #5a5b5d;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 0 20 0 20;"
        );

        inputFormSection.getChildren().addAll(instructionLabel, nameField);
        
        // Spacer to push the confirm button to the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // --- Bottom Full Width Actions Row ---
        Button confirm = new Button("JOIN");
        confirm.setMaxWidth(Double.MAX_VALUE); // Expand completely horizontally
        confirm.setPrefHeight(80);
        confirm.setCursor(Cursor.HAND);
        
        // Flat crisp concrete grey footer look wrapping tight to the bottom curves
        confirm.setStyle(
            "-fx-background-color: #bcbfc2;" +
            "-fx-text-fill: #000000;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 0 0 20 20;" + // Round only the very bottom edge of the button
            "-fx-border-radius: 0 0 20 20;"
        );

        // Hover effect for the Join button to make it interactive
        confirm.setOnMouseEntered(e -> confirm.setStyle(
            "-fx-background-color: #a3a6a9;" +
            "-fx-text-fill: #000000;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 0 0 20 20;"
        ));
        confirm.setOnMouseExited(e -> confirm.setStyle(
            "-fx-background-color: #bcbfc2;" +
            "-fx-text-fill: #000000;" +
            "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';" +
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 0 0 20 20;"
        ));

        // Execution logical mapping definitions
        Runnable submit = () -> {
            String raw = nameField.getText().trim();
            app.setPlayerName(raw.isEmpty() ? "PLAYER" : raw.toUpperCase());
            app.showMainMenu();
        };

        confirm.setOnAction(e -> submit.run());
        nameField.setOnAction(e -> submit.run());

        // Composing structural hierarchy block segments
        dialogCard.getChildren().addAll(topHeaderSection, inputFormSection, spacer, confirm);
        root.getChildren().add(dialogCard);
        
        return root;
    }
}