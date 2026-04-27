package com.example;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

public class UIHelper {

    public static Button createButton(String imagePath, double scale) {
        Image img = new Image(UIHelper.class.getResource(imagePath).toExternalForm());
        ImageView view = new ImageView(img);

        view.setFitWidth(img.getWidth() * scale);
        view.setPreserveRatio(true);
        view.setSmooth(true);

        Button button = new Button();
        button.setGraphic(view);
        button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        button.setCursor(Cursor.HAND);
        
        // Hover effects
        button.setOnMouseEntered(e -> view.setOpacity(0.8));
        button.setOnMouseExited(e -> view.setOpacity(1.0));

        return button;
    }

    public static Background createBackground(String imagePath) {
        Image bgImg = new Image(UIHelper.class.getResource(imagePath).toExternalForm());
        BackgroundImage myBI = new BackgroundImage(
            bgImg,
            BackgroundRepeat.NO_REPEAT, 
            BackgroundRepeat.NO_REPEAT, 
            BackgroundPosition.CENTER,
            new BackgroundSize(100, 100, true, true, false, true)
        );

        return new Background(myBI);
    }
}