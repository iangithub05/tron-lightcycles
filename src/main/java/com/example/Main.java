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

        // Start with empty root (we will swap screens)
        scene = new Scene(new Pane(), 800, 600);

        // GLOBAL INPUT HANDLING (IMPORTANT)
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            System.out.println("KEY PRESSED: " + e.getCode());
            InputManager.press(e.getCode());
        });

        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            System.out.println("KEY RELEASED: " + e.getCode());
            InputManager.release(e.getCode());
        });

        // Start at main menu
        showMainMenu();

        stage.setTitle("Tron: Light Cycles");
        stage.setScene(scene);
        stage.show();

        // ensures focus works immediately
        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    // ---------------- SCREEN NAVIGATION ----------------

    public void showMainMenu() {
        MainMenu menu = new MainMenu(this);
        scene.setRoot(menu.getView());

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public void showGame() {
        GameScreen gameScreen = new GameScreen(this);
        scene.setRoot(gameScreen.getView());

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public void showMode() {
        ModeScreen modeScreen = new ModeScreen(this);
        scene.setRoot(modeScreen.getView());

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public void showAbout() {
        AboutScreen abtScreen = new AboutScreen(this);
        scene.setRoot(abtScreen.getView());

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public void showConfig() {
        ConfigScreen abtScreen = new ConfigScreen(this);
        scene.setRoot(abtScreen.getView());

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    // public void showGameOver(String result) {
    //     GameOverScreen over = new GameOverScreen(this, result);
    //     scene.setRoot(over.getView());

    //     Platform.runLater(() -> scene.getRoot().requestFocus());
    // }

    // ---------------- ENTRY POINT ----------------

    public static void main(String[] args) {
        launch();
    }
}