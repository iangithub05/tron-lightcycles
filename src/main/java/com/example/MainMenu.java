package com.example;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.geometry.Pos;

public class MainMenu {

    private final Main app;

    public MainMenu(Main app) {
        this.app = app;
    }

    public Pane getView() {
        Pane layout = new Pane();
        layout.setBackground(createBackground("/images/background.png"));

        // -- game mode options -----------------------------------------------------
        
		Button multiplayerButton = createButton("/images/button_multiplayer.png", 0.30);
		multiplayerButton.setOnAction(e -> app.showGame());
        
        Button singleplayerButton = createButton("/images/button_singleplayer.png", 0.30);
		singleplayerButton.setOnAction(e -> app.showGame());

        VBox playButtons = new VBox(5, multiplayerButton, singleplayerButton);
        playButtons.setLayoutX(50);
        playButtons.setLayoutY(375);

        // -- misc -----------------------------------------------------

        Button configButton = createButton("/images/button_config.png", 0.10);
        Button aboutButton = createButton("/images/button_about.png", 0.10);

        VBox settingsButtons = new VBox(5, configButton, aboutButton);
        settingsButtons.setAlignment(Pos.CENTER);

        Button exit = createButton("/images/button_exit.png", 0.10);
        exit.setOnAction(e -> System.exit(0));
        exit.setLayoutX(663);
        exit.setLayoutY(97);

        layout.getChildren().addAll(playButtons, settingsButtons, exit);
        return layout;
    }



    // -- helper methods -----------------------------------------------------

    public static Button createButton(String imagePath, double scale) {
        Image img = new Image(MainMenu.class.getResource(imagePath).toExternalForm());
        ImageView view = new ImageView(img);

        view.setFitWidth(img.getWidth() * scale);
        view.setPreserveRatio(true);
        view.setSmooth(true);

        Button button = new Button();
        button.setGraphic(view);
        button.setStyle("-fx-background-color: transparent");
        button.setCursor(javafx.scene.Cursor.HAND);
        button.setOnMouseEntered(e -> view.setOpacity(0.8));
        button.setOnMouseExited(e -> view.setOpacity(1.0));

        return button;
    }

    public static Background createBackground(String imagePath) {
        Image bgImg = new Image(MainMenu.class.getResource(imagePath).toExternalForm());
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