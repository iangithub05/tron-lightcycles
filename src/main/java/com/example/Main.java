package com.example;

import com.example.input.InputManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage stage;
    private Scene scene;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        scene = new Scene(new Pane(), Game.WIDTH, Game.HEIGHT);

        // Global keyboard input
        scene.addEventFilter(KeyEvent.KEY_PRESSED,  e -> InputManager.press(e.getCode()));
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> InputManager.release(e.getCode()));

        showMainMenu();

        stage.setTitle("Tron: Light Cycles");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    // ── Screen Navigation ──────────────────────────────────────────────────

    public void showMainMenu() {
        MainMenu menu = new MainMenu(this);
        scene.setRoot(menu.getView());
        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public void showGame(GameMode mode) {
        GameScreen gameScreen = new GameScreen(this, mode);
        scene.setRoot(gameScreen.getView());
        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public void showGameOver(boolean won, GameMode mode, String message) {
        GameOverScreen over = new GameOverScreen(this, won, mode, message);
        scene.setRoot(over.getView());
        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    // ── Entry Point ────────────────────────────────────────────────────────

    public static void main(String[] args) {
        launch();
    }
}
