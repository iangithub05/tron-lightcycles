package com.example.controllers;

import com.example.Main;
import com.example.ui.Theme;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MatchOverScreen {

    private final Main   app;
    private final String result;
    private final int[]  scores;

    public MatchOverScreen(Main app, String result, int[] scores) {
        this.app = app;
        this.result = result;
        this.scores = scores;
    }

    public StackPane getView() {
        Label nav = new Label("MULTIPLAYER  /  MATCH OVER");
        nav.getStyleClass().add("nav-title");

        Label title = new Label(result);
        title.getStyleClass().add("game-title");

        VBox scoreBox = new VBox(6);
        if (scores != null) {
            Label scTitle = new Label("FINAL SCORES");
            scTitle.setStyle("-fx-text-fill: #a0aec0; -fx-font-family: 'Courier New';"
                    + " -fx-font-size: 13px;");
            scoreBox.getChildren().add(scTitle);
            String[] colors = {"#63b3ed", "#68d391", "#fc8181", "#b794f4"};
            for (int i = 0; i < scores.length; i++) {
                Label sc = new Label("Player " + (i + 1) + ": " + scores[i] + " rounds won");
                sc.setStyle("-fx-text-fill: " + colors[i % colors.length]
                        + "; -fx-font-family: 'Courier New'; -fx-font-size: 16px;");
                scoreBox.getChildren().add(sc);
            }
        }

        Button menu = new Button("MAIN MENU");
        menu.getStyleClass().add("tron-btn");
        menu.setOnAction(e -> app.showMainMenu());

        Button multi = new Button("PLAY AGAIN");
        multi.getStyleClass().add("tron-btn");
        multi.setOnAction(e -> app.showMultiplayer());

        VBox content = new VBox(10, nav, title,
                Theme.spacer(10), scoreBox,
                Theme.spacer(20), menu, multi,
                Theme.spacer(10), Theme.divider());
        content.setAlignment(Pos.TOP_LEFT);
        content.setMaxWidth(460);

        StackPane root = new StackPane(content);
        root.setAlignment(Pos.CENTER);
        Theme.apply(root);
        root.getStyleClass().add("screen-root");
        return root;
    }
}
