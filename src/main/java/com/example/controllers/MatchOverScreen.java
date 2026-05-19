package com.example.controllers;

import com.example.Main;
import com.example.ui.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MatchOverScreen {

    private final Main app;
    private final String result;
    private final int[] scores;

    public MatchOverScreen(Main app, String result, int[] scores) {
        this.app = app;
        this.result = result;
        this.scores = scores;
    }

    public StackPane getView() {
        StackPane root = new StackPane();
        root.setBackground(UIHelper.createBackground("/images/background_4.png"));
        Theme.apply(root);

        BorderPane screen = new BorderPane();
        screen.setTop(styledTopBar("| GAME OVER"));
        screen.setBottom(UIHelper.createNavigationBar("TRON: LIGHT CYCLES", "QUIT"));

        Label title = new Label(result == null ? "GAME OVER" : result.toUpperCase());
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, 48));

        VBox scoreBox = new VBox(6);
        scoreBox.setAlignment(Pos.CENTER);
        if (scores != null) {
            Label scTitle = smallWhite("FINAL SCORES", 12);
            scoreBox.getChildren().add(scTitle);
            String[] colors = {"#63b3ed", "#68d391", "#fc8181", "#b794f4"};
            for (int i = 0; i < scores.length; i++) {
                Label sc = new Label("P" + (i + 1) + ": " + scores[i] + " rounds won");
                sc.setStyle("-fx-text-fill: " + colors[i % colors.length] + "; -fx-font-family: 'Courier New'; -fx-font-size: 15px; -fx-font-weight: bold;");
                scoreBox.getChildren().add(sc);
            }
        }

        Button playAgain = styledButton("PLAY AGAIN", 230, 52, "#235c35", "#13351f");
        playAgain.setOnAction(e -> app.showMultiplayer());

        Button menu = styledButton("BACK TO MENU", 230, 52, "#a83258", "#6d1831");
        menu.setOnAction(e -> app.showMainMenu());

        VBox panel = new VBox(24, title, scoreBox, playAgain, menu);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(30));
        panel.setStyle("-fx-background-color: rgba(20,22,35,0.35);");

        screen.setCenter(panel);
        root.getChildren().add(screen);
        return root;
    }

    private HBox styledTopBar(String leftText) {
        HBox topBar = UIHelper.createNavigationBar(leftText, "Welcome, " + app.getPlayerName());
        Label welcome = (Label) topBar.getChildren().get(2);
        welcome.setPadding(new Insets(12, 26, 12, 26));
        welcome.setStyle("-fx-background-color: linear-gradient(to right, #9b2447, #6d1831);"
                + "-fx-background-radius: 12; -fx-border-color: #4d1020; -fx-border-radius: 12;"
                + "-fx-font-family: '" + UIHelper.pixelFont.getFamily() + "'; -fx-font-size: 14px;"
                + "-fx-font-weight: bold; -fx-text-fill: white;");
        return topBar;
    }

    private static Label smallWhite(String text, int size) {
        Label l = new Label(text);
        l.setTextFill(Color.WHITE);
        l.setFont(Font.font(UIHelper.pixelFont.getFamily(), FontWeight.BOLD, size));
        return l;
    }

    private static Button styledButton(String text, int w, int h, String top, String bottom) {
        Button b = new Button(text);
        b.setCursor(Cursor.HAND);
        b.setPrefSize(w, h);
        b.setStyle("-fx-background-color: linear-gradient(to bottom, " + top + ", " + bottom + ");"
                + "-fx-text-fill: white; -fx-font-family: '" + UIHelper.pixelFont.getFamily() + "';"
                + "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 28;"
                + "-fx-border-color: black; -fx-border-radius: 28; -fx-border-width: 1;");
        return b;
    }
}
