package com.example.controllers;

import com.example.Main;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainMenu {

    private final Main app;

    public MainMenu(Main app) {
        this.app = app;
    }

    public VBox getView() {
        Label title = new Label("TRON: LIGHT CYCLES");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 26));
        title.setTextFill(Color.CYAN);

        Button singleplayer = new Button("Singleplayer");
        Button multiplayer  = new Button("Multiplayer");
        Button exit         = new Button("Exit");

        singleplayer.setPrefWidth(180);
        multiplayer.setPrefWidth(180);
        exit.setPrefWidth(180);

        singleplayer.setOnAction(e -> app.showGame());
        multiplayer.setOnAction(e -> app.showMultiplayerLobby());
        exit.setOnAction(e -> System.exit(0));

        VBox layout = new VBox(16, title, singleplayer, multiplayer, exit);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #0a0a1e;");

        return layout;
    }
}
