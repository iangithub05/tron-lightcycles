package com.example.controllers;

import com.example.Main;
import com.example.services.GameSession;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * UI only — displays result and offers Restart / Main Menu.
 */
public class GameOverScreen {

    private final Main        app;
    private final String      result;
    private final GameSession session;

    public GameOverScreen(Main app, String result, GameSession session) {
        this.app     = app;
        this.result  = result;
        this.session = session;
    }

    public VBox getView() {
        Label label = new Label(result);

        Button retry = new Button("Retry");
        Button menu  = new Button("Main Menu");

        retry.setOnAction(e -> app.restartGame(session));
        menu.setOnAction(e -> app.showMainMenu());

        VBox layout = new VBox(20, label, retry, menu);
        layout.setAlignment(Pos.CENTER);
        return layout;
    }
}
