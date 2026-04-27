package com.example;

import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class MultiplayerMenu {

    private final Main app;

    public MultiplayerMenu(Main app) {
        this.app = app;
    }

    public Pane getView() {
        Pane layout = new Pane();
        layout.setBackground(UIHelper.createBackground("/images/background_multiplayer.png"));

        // -- game mode options -----------------------------------------------------

		Button joinButton = UIHelper.createButton("/images/button_join.png", 0.30);
		joinButton.setOnAction(e -> app.showGame());
        
        Button createButton = UIHelper.createButton("/images/button_create.png", 0.30);
		createButton.setOnAction(e -> app.showGame());

        VBox playButtons = new VBox(5, joinButton, createButton);
        playButtons.setLayoutX(550);
        playButtons.setLayoutY(375);

        // -- misc -----------------------------------------------------

        Button backButton = UIHelper.createButton("/images/button_back.png", 0.20);
        backButton.setOnAction(e -> app.showMainMenu());
        backButton.setLayoutX(10); 
        backButton.setLayoutY(10);

        Button exit = UIHelper.createButton("/images/button_exit.png", 0.10);
        exit.setOnAction(e -> System.exit(0));
        exit.setLayoutX(663);
        exit.setLayoutY(99);

        layout.getChildren().addAll(playButtons, backButton, exit);

        return layout;
    }

}