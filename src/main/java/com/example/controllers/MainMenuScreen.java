package com.example.controllers;

import com.example.Main;
import com.example.ui.Theme;
import javafx.animation.TranslateTransition;
import javafx.scene.Cursor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class MainMenuScreen {

    private final Main app;

    public MainMenuScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        StackPane root = new StackPane();
        root.setBackground(UIHelper.createBackground("/images/background_1.png"));

        BorderPane menuLayout = new BorderPane();

        HBox topBar = UIHelper.createNavigationBar("|  HOME", "WELCOME, " + app.getPlayerName());
        
        Label welcomeLabel = (Label) topBar.getChildren().get(2);
        welcomeLabel.setPadding(new Insets(12, 30, 12, 30));
        welcomeLabel.setStyle("-fx-background-color: linear-gradient(to right, #801a33, #4d1020); -fx-background-radius: 15;");

        menuLayout.setTop(topBar);

        VBox menuContainer = new VBox(15);
        menuContainer.setAlignment(Pos.CENTER_RIGHT); 
        menuContainer.setPadding(new Insets(40, 0, 40, 20));

        HBox mpButton = UIHelper.createMenuButton("M", "MULTIPLAYER", "PLAY ONLINE WITH FRIENDS", "#2a4d34", "#7fff9e");
        HBox spButton = UIHelper.createMenuButton("S", "SINGLEPLAYER", "CHALLENGE YOURSELF", "#3d2b36", "#e099b5");
        HBox aboutButton = UIHelper.createMenuButton("A", "ABOUT", "VIEW ABOUT INFO", "#24334d", "#8da9df");

        spButton.setOnMouseClicked(e -> app.showSingleplayer());
        mpButton.setOnMouseClicked(e -> app.showMultiplayer());
        aboutButton.setOnMouseClicked(e -> app.showAbout());

        menuContainer.getChildren().addAll(mpButton, spButton, aboutButton);
        menuLayout.setCenter(menuContainer);

        HBox bottomBar = UIHelper.createNavigationBar("TRON: LIGHT CYCLES", "QUIT");
        menuLayout.setBottom(bottomBar);

        root.getChildren().add(menuLayout);
        return root;
    }
}