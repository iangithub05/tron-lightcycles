package com.example.controllers;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class UIHelper {

    public static final Font pixelFont;

    static {
        Font loadedFont = null;
        try {
            loadedFont = Font.loadFont(UIHelper.class.getResourceAsStream("/fonts/PressStart2P.ttf"), 30);
        } catch (Exception e) {
            System.err.println("Font not found! Falling back to Monospaced.");
        }
        pixelFont = (loadedFont != null) ? loadedFont : Font.font("Monospaced", 30);
    }

    public static Background createBackground(String imagePath) {
        Image bgImg = new Image(UIHelper.class.getResource(imagePath).toExternalForm());
        
        BackgroundSize bgSize = new BackgroundSize(
            BackgroundSize.AUTO, 
            BackgroundSize.AUTO, 
            false, 
            false, 
            false, 
            true
        );

        BackgroundImage myBI = new BackgroundImage(
            bgImg,
            BackgroundRepeat.NO_REPEAT, 
            BackgroundRepeat.NO_REPEAT, 
            BackgroundPosition.CENTER,
            bgSize
        );

        return new Background(myBI);
    }

    public static HBox createMenuButton(String iconText, String titleText, String subtitleText, String bgColor, String accentColor) {
        HBox layout = new HBox(40);
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setPadding(new Insets(45, 300, 45, 80)); 
        layout.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 30 0 0 30;" + 
                "-fx-border-color: rgba(255,255,255,0.3);" +
                "-fx-border-width: 3 0 3 3;" + 
                "-fx-border-radius: 30 0 0 30;"
        );
        
        layout.setCursor(Cursor.HAND); 
        layout.setTranslateX(350);

        Label icon = new Label(iconText);
        icon.setTextFill(Color.web(accentColor));
        icon.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 64));
        icon.setMinWidth(100);
        icon.setAlignment(Pos.CENTER);

        VBox textBlock = new VBox(10);
        textBlock.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(titleText);
        title.setTextFill(Color.web(accentColor));
        title.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 36));
        textBlock.getChildren().add(title);

        // Only create and add the subtitle if the text is provided and not empty
        if (subtitleText != null && !subtitleText.isEmpty()) {
            Label subtitle = new Label(subtitleText);
            subtitle.setTextFill(Color.web(accentColor).deriveColor(0, 1, 0.8, 0.7)); 
            subtitle.setFont(Font.font(pixelFont.getFamily(), FontWeight.NORMAL, 16));
            textBlock.getChildren().add(subtitle);
        }

        layout.getChildren().addAll(icon, textBlock);

        TranslateTransition hoverTransition = new TranslateTransition(Duration.millis(250), layout);

        layout.setOnMouseEntered(e -> {
            hoverTransition.stop();

            // Pulled back slightly less to keep the content closer to the edge
            hoverTransition.setToX(200); 
            hoverTransition.play();
            layout.setOpacity(0.85);
        });

        layout.setOnMouseExited(e -> {
            hoverTransition.stop();
            hoverTransition.setToX(350);
            hoverTransition.play();
            layout.setOpacity(1.0);
        });

        return layout;
    }

    /**
     * @param leftText
     * @param rightText
     * @return
     */
    public static HBox createNavigationBar(String leftText, String rightText) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(30, 50, 30, 50)); 
        bar.setStyle("-fx-background-color: linear-gradient(to right, #1c263e, #88738f);");

        Label leftLabel = new Label(leftText);
        leftLabel.setTextFill(Color.web("#a3a3c2"));
        leftLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 24));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label rightLabel = new Label(rightText);
        rightLabel.setTextFill(Color.WHITE);
        rightLabel.setFont(Font.font(pixelFont.getFamily(), FontWeight.BOLD, 18));

        if (rightText.equalsIgnoreCase("QUIT")) {
            rightLabel.setTextFill(Color.BLACK);
            rightLabel.setCursor(Cursor.HAND);
            rightLabel.setOnMouseEntered(e -> rightLabel.setTextFill(Color.WHITE));
            rightLabel.setOnMouseExited(e -> rightLabel.setTextFill(Color.BLACK));
            rightLabel.setOnMouseClicked(e -> System.exit(0));
        }

        bar.getChildren().addAll(leftLabel, spacer, rightLabel);
        return bar;
    }
}