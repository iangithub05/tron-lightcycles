package com.example.controllers;

import com.example.Main;
import com.example.models.Difficulty;
import com.example.services.SinglePlayerSession;
import com.example.ui.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class SingleplayerScreen {

    private final Main app;

    public SingleplayerScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        StackPane root = new StackPane();
        root.setBackground(UIHelper.createBackground("/images/background_2.png"));

        BorderPane menuLayout = new BorderPane();

        HBox topBar = UIHelper.createNavigationBar("|  SINGLEPLAYER", "WELCOME, " + app.getPlayerName());
        
        Label welcomeLabel = (Label) topBar.getChildren().get(2);
        welcomeLabel.setPadding(new Insets(12, 30, 12, 30));
        welcomeLabel.setStyle("-fx-background-color: linear-gradient(to right, #801a33, #4d1020); -fx-background-radius: 15;");

        menuLayout.setTop(topBar);

        VBox menuContainer = new VBox(15);
        menuContainer.setAlignment(Pos.CENTER_RIGHT);
        // Ensure the menu container is always aligned to the right, even if it's smaller than the screen
        HBox.setHgrow(menuContainer, Priority.ALWAYS);
        menuContainer.setPadding(new Insets(40, 0, 40, 20));

        HBox easyButton = UIHelper.createMenuButton("E", "EASY", "", "#2a4d34", "#7fff9e");
        HBox mediumButton = UIHelper.createMenuButton("M", "MEDIUM", "", "#3d2b36", "#e099b5");
        HBox hardButton = UIHelper.createMenuButton("H", "HARD", "", "#24334d", "#8da9df");
        HBox backButton = UIHelper.createMenuButton("B", "BACK", "RETURN TO MAIN MENU", "#242336", "#7774a7");

        easyButton.setOnMouseClicked(e -> startGame(Difficulty.EASY));
        mediumButton.setOnMouseClicked(e -> startGame(Difficulty.MEDIUM));
        hardButton.setOnMouseClicked(e -> startGame(Difficulty.HARD));
        backButton.setOnMouseClicked(e -> app.showMainMenu());

        menuContainer.getChildren().addAll(easyButton, mediumButton, hardButton, backButton);
        menuLayout.setCenter(menuContainer);

        HBox bottomBar = UIHelper.createNavigationBar("TRON: LIGHT CYCLES", "QUIT");
        menuLayout.setBottom(bottomBar);

        root.getChildren().add(menuLayout);
        return root;
    }

    private void startGame(Difficulty difficulty) {
        app.showGame(new SinglePlayerSession(difficulty));
    }
}
