package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.control.Labeled;
import javafx.scene.layout.StackPane;
import javafx.beans.binding.Bindings;

public class UIHelper {

    private static final Font pixelFont;

    static {
        Font loadedFont = null;
        try {
            loadedFont = Font.loadFont(UIHelper.class.getResourceAsStream("/fonts/PressStart2P.ttf"), 30);
        } catch (Exception e) {
            System.err.println("Font not found! Falling back to Monospaced.");
        }
        pixelFont = (loadedFont != null) ? loadedFont : Font.font("Monospaced", 30);
    }

    public static void bindFontSize(Labeled node, StackPane root, double baseSize) {
        node.fontProperty().bind(Bindings.createObjectBinding(() -> {
            double scale = root.getWidth() / 1000.0;
            if (scale < 0.5) scale = 0.5;
            
            return Font.font(pixelFont.getFamily(), baseSize * scale);
        }, root.widthProperty()));
    }

    public static VBox createLogo(String mainTitle, String subTitle) {
        VBox logoBox = new VBox(-10);
        logoBox.setAlignment(Pos.CENTER);

        Label mainLabel = new Label(mainTitle);
        mainLabel.setFont(Font.font(pixelFont.getFamily(), 80)); 
        mainLabel.setTextFill(Color.WHITE);
        mainLabel.setEffect(new DropShadow(20, Color.WHITE));

        Label subLabel = new Label(subTitle);
        subLabel.setFont(Font.font(pixelFont.getFamily(), 20));
        subLabel.setTextFill(Color.WHITE);
        subLabel.setEffect(new DropShadow(10, Color.WHITE));

        logoBox.getChildren().addAll(mainLabel, subLabel);
        VBox.setMargin(logoBox, new Insets(0, 0, 50, 0)); 
        
        return logoBox;
    }

    public static Button createMenuButton(String text, double fontSize) {
        Button button = new Button(text);
        button.setFont(Font.font(pixelFont.getFamily(), fontSize));

        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        button.setEffect(new DropShadow(25, Color.WHITE));
        button.setCursor(Cursor.HAND);


        button.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(0.9);
            st.setToY(0.9);
            st.play();
        });

        button.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        button.setOnMouseEntered(e -> {
            button.setText("> " + text + " <");
            button.setEffect(new DropShadow(50, Color.WHITE));
        });

        button.setOnMouseExited(e -> {
            button.setText(text);
            button.setEffect(new DropShadow(25, Color.WHITE));
        });

        return button;
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
}