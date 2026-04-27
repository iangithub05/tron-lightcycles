package com.example;

import com.example.controllers.GameOverScreen;
import com.example.controllers.GameScreen;
import com.example.controllers.MainMenu;
import com.example.services.GameSession;
import com.example.utils.InputManager;
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

        // Scene with empty root — screens are swapped in-place
        scene = new Scene(new Pane(), 800, 600);

        // GLOBAL INPUT HANDLING — feeds InputManager (Task 2)
        scene.addEventFilter(KeyEvent.KEY_PRESSED,  e -> InputManager.press(e.getCode()));
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> InputManager.release(e.getCode()));

        showMainMenu();

        stage.setTitle("Tron: Light Cycles");
        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    // ── Screen navigation ────────────────────────────────────────────────────

    public void showMainMenu() {
        MainMenu menu = new MainMenu(this);
        swap(menu.getView());
    }

    /** Start a brand-new game (fresh GameScreen + GameSession). */
    public void showGame() {
        GameScreen gs = new GameScreen(this);
        swap(gs.getView());
    }

    /** Called by GameScreen when the human player dies. */
    public void showGameOver(String result, GameSession session) {
        GameOverScreen over = new GameOverScreen(this, result, session);
        swap(over.getView());
    }

    /** Called by GameOverScreen Retry button — reuses existing session. */
    public void restartGame(GameSession session) {
        GameScreen gs = new GameScreen(this, session);
        swap(gs.getView());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void swap(javafx.scene.Parent root) {
        scene.setRoot(root);
        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    // ── Entry point ──────────────────────────────────────────────────────────

    public static void main(String[] args) {
        launch();
    }
}