package com.example;

import com.example.controllers.GameOverScreen;
import com.example.controllers.GameScreen;
import com.example.controllers.LobbyScreen;
import com.example.controllers.MainMenu;
import com.example.controllers.MultiplayerGameScreen;
import com.example.network.GameClient;
import com.example.network.GameServer;
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

        scene = new Scene(new Pane(), 800, 600);

        // Global input handling
        scene.addEventFilter(KeyEvent.KEY_PRESSED,  e -> InputManager.press(e.getCode()));
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> InputManager.release(e.getCode()));

        showMainMenu();

        stage.setTitle("Tron: Light Cycles");
        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    // ── Screen navigation ─────────────────────────────────────────────────

    public void showMainMenu() {
        MainMenu menu = new MainMenu(this);
        swap(menu.getView());
    }

    /** Singleplayer — brand-new game session. */
    public void showGame() {
        GameScreen gs = new GameScreen(this);
        swap(gs.getView());
    }

    /** Singleplayer game-over → retry reuses existing session. */
    public void showGameOver(String result, GameSession session) {
        GameOverScreen over = new GameOverScreen(this, result, session);
        swap(over.getView());
    }

    /** Singleplayer retry. */
    public void restartGame(GameSession session) {
        GameScreen gs = new GameScreen(this, session);
        swap(gs.getView());
    }

    // ── Multiplayer navigation ────────────────────────────────────────────

    /** Opens the multiplayer lobby (host / join). */
    public void showMultiplayerLobby() {
        LobbyScreen lobby = new LobbyScreen(this);
        swap(lobby.getView());
    }

    /** Host launches game after guest connects — server is already started. */
    public void showMultiplayerGame(GameServer server) {
        MultiplayerGameScreen mgs = new MultiplayerGameScreen(this, server);
        swap(mgs.getView());
    }

    /** Guest launches game after connecting. */
    public void showMultiplayerGame(GameClient client) {
        MultiplayerGameScreen mgs = new MultiplayerGameScreen(this, client);
        swap(mgs.getView());
    }

    /** Multiplayer game-over screen (simple: just show result + back to menu). */
    public void showMultiplayerGameOver(String result) {
        // Reuse the GameOverScreen but without a retry session
        javafx.scene.control.Label label = new javafx.scene.control.Label(result);
        label.setTextFill(javafx.scene.paint.Color.CYAN);
        label.setFont(javafx.scene.text.Font.font("Monospace",
                javafx.scene.text.FontWeight.BOLD, 24));

        javafx.scene.control.Button menuBtn = new javafx.scene.control.Button("Main Menu");
        menuBtn.setOnAction(e -> showMainMenu());

        javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(20, label, menuBtn);
        layout.setAlignment(javafx.geometry.Pos.CENTER);
        layout.setStyle("-fx-background-color: #0a0a1e;");
        swap(layout);
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private void swap(javafx.scene.Parent root) {
        scene.setRoot(root);
        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public static void main(String[] args) {
        launch();
    }
}
