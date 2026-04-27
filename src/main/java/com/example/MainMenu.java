package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class MainMenu {

    private final Main app;

    public MainMenu(Main app) {
        this.app = app;
    }

    public Pane getView() {
        Pane layout = new Pane();
        layout.setBackground(UIHelper.createBackground("/images/background_main.png"));

        // -- game mode options -----------------------------------------------------

		Button multiplayerButton = UIHelper.createButton("/images/button_multiplayer.png", 0.30);
		multiplayerButton.setOnAction(e -> app.showMultiplayerMenu());
        
        Button singleplayerButton = UIHelper.createButton("/images/button_singleplayer.png", 0.30);
		singleplayerButton.setOnAction(e -> app.showSinglePlayerMenu());

        VBox playButtons = new VBox(5, multiplayerButton, singleplayerButton);
        playButtons.setLayoutX(50);
        playButtons.setLayoutY(375);

        // -- misc -----------------------------------------------------

        Button configButton = UIHelper.createButton("/images/button_config.png", 0.10);
        Button aboutButton = UIHelper.createButton("/images/button_about.png", 0.10);

        VBox settingsButtons = new VBox(5, configButton, aboutButton);
        settingsButtons.setAlignment(Pos.CENTER);
        settingsButtons.setPadding(new Insets(10, 10, 10, 10));

        Button exit = UIHelper.createButton("/images/button_exit.png", 0.10);
        exit.setOnAction(e -> System.exit(0));
        exit.setLayoutX(663);
        exit.setLayoutY(99);

        layout.getChildren().addAll(playButtons, settingsButtons, exit);
        return layout;
    }
}