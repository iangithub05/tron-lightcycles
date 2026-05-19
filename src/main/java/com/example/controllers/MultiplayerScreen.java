package com.example.controllers;

import com.example.Main;
import com.example.ui.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class MultiplayerScreen {

    private final Main app;

    public MultiplayerScreen(Main app) {
        this.app = app;
    }

    public StackPane getView() {
        StackPane root = new StackPane();
        root.setBackground(UIHelper.createBackground("/images/background_3.png"));

        BorderPane menuLayout = new BorderPane();

        HBox topBar = UIHelper.createNavigationBar("|  MULTIPLAYER", "WELCOME, " + app.getPlayerName());
        
        Label welcomeLabel = (Label) topBar.getChildren().get(2);
        welcomeLabel.setPadding(new Insets(12, 30, 12, 30));
        welcomeLabel.setStyle("-fx-background-color: linear-gradient(to right, #801a33, #4d1020); -fx-background-radius: 15;");

        menuLayout.setTop(topBar);

        VBox menuContainer = new VBox(15);
        menuContainer.setAlignment(Pos.CENTER_RIGHT);
        // Ensure the menu container is always aligned to the right, even if it's smaller than the screen
        HBox.setHgrow(menuContainer, Priority.ALWAYS);
        menuContainer.setPadding(new Insets(40, 0, 40, 20));

        HBox hostButton = UIHelper.createMenuButton("H", "HOST A GAME", "CREATE A ROOM FOR YOUR FRIENDS", "#2a4d34", "#7fff9e");
        HBox joinButton = UIHelper.createMenuButton("J", "JOIN A GAME", "JOIN A ROOM CREATED BY YOUR FRIENDS", "#3d2b36", "#e099b5");
        HBox backButton = UIHelper.createMenuButton("B", "BACK", "RETURN TO MAIN MENU", "#24334d", "#8da9df");

        hostButton.setOnMouseClicked(e -> app.showHostLobby());
        joinButton.setOnMouseClicked(e -> app.showJoinLobby());
        backButton.setOnMouseClicked(e -> app.showMainMenu());

        menuContainer.getChildren().addAll(hostButton, joinButton, backButton);
        menuLayout.setCenter(menuContainer);

        HBox bottomBar = UIHelper.createNavigationBar("TRON: LIGHT CYCLES", "QUIT");
        menuLayout.setBottom(bottomBar);

        root.getChildren().add(menuLayout);
        return root;
    }
}
