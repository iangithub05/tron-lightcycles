package com.example;

import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Pos;

public class GameOverScreen {

    private final Main app;
    private final String result;

    public GameOverScreen(Main app, String result) {
        this.app = app;
        this.result = result;
    }

    public Pane getView() {
        Pane layout = new Pane();
        layout.setBackground(UIHelper.createBackground("/images/background_game_over.png"));

        // Label label = new Label(result);
        // label.setStyle("-fx-font-size: 40px; -fx-text-fill: white; -fx-font-family: 'Courier New';");

        Button retryButton = UIHelper.createButton("/images/button_retry.png", 0.50);
		retryButton.setOnAction(e -> app.showGame());
        
        Button menuButton = UIHelper.createButton("/images/button_menu.png", 0.50);
		menuButton.setOnAction(e -> app.showMainMenu());

        VBox playButtons = new VBox(5, retryButton, menuButton);
        playButtons.setAlignment(Pos.CENTER);
        playButtons.layoutXProperty().bind(layout.widthProperty().divide(2).subtract(playButtons.widthProperty().divide(2)));
        playButtons.layoutYProperty().bind(layout.heightProperty().divide(2).subtract(playButtons.heightProperty().divide(2)).add(100));

        layout.getChildren().addAll(playButtons);
        return layout;
    }
}