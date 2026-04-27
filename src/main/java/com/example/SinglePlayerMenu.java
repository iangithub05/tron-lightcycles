package com.example;

import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class SinglePlayerMenu {

    private final Main app;

    public SinglePlayerMenu(Main app) {
        this.app = app;
    }

    public Pane getView() {
        Pane layout = new Pane();
        layout.setBackground(UIHelper.createBackground("/images/background_singleplayer.png"));

        // -- game mode options -----------------------------------------------------

		Button aiButton = UIHelper.createButton("/images/button_ai.png", 0.30);
		aiButton.setOnAction(e -> app.showGame());
        
        Button selfButton = UIHelper.createButton("/images/button_self.png", 0.30);
		selfButton.setOnAction(e -> app.showGame());

        VBox playButtons = new VBox(5, aiButton, selfButton);
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