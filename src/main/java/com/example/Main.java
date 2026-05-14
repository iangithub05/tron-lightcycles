package com.example;

import com.example.controllers.AskNameScreen;
import com.example.controllers.GameOverScreen;
import com.example.controllers.GameScreen;
import com.example.controllers.JoinLobbyScreen;
import com.example.controllers.HostLobbyScreen;
import com.example.controllers.MainMenuScreen;
import com.example.controllers.MultiplayerScreen;
import com.example.controllers.AboutScreen;
import com.example.controllers.SingleplayerScreen;
import com.example.models.GameMode;
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
import javafx.stage.StageStyle;

public class Main extends Application {

    private Stage stage;
    private Scene scene;
    private String playerName = "PLAYER";

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        scene = new Scene(new Pane());

        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> InputManager.press(e.getCode()));
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> InputManager.release(e.getCode()));

        stage.setTitle("TRON: LIGHT CYCLES");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.show();

        showAskName();

        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public void showAskName() {
        AskNameScreen s = new AskNameScreen(this);
        swap(s.getView());
    }

    public void showMainMenu() {
        MainMenuScreen s = new MainMenuScreen(this);
        swap(s.getView());
    }

    public void showSingleplayer() {
        SingleplayerScreen s = new SingleplayerScreen(this);
        swap(s.getView());
    }

    public void showMultiplayer() {
        MultiplayerScreen s = new MultiplayerScreen(this);
        swap(s.getView());
    }

    public void showHostLobby() {
        HostLobbyScreen s = new HostLobbyScreen(this);
        swap(s.getView());
    }

    public void showAbout() {
        AboutScreen s = new AboutScreen(this);
        swap(s.getView());
    }

    public void showJoinLobby() {
        JoinLobbyScreen s = new JoinLobbyScreen(this);
        swap(s.getView());
    }

    public void showGame(GameSession session) {
        GameScreen s = new GameScreen(this, session);
        swap(s.getView());
    }

    public void showGameOver(String result, GameSession session) {
        GameOverScreen s = new GameOverScreen(this, result, session);
        swap(s.getView());
    }

    public void showMultiplayerGame(GameServer server) {
    }

    public void showMultiplayerGame(GameClient client) {
    }

    public void showMultiplayerGameOver(String result) {
        GameOverScreen s = new GameOverScreen(this, result, null);
        swap(s.getView());
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    private void swap(javafx.scene.Parent root) {
        scene.setRoot(root);
        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public static void main(String[] args) {
        launch();
    }
}
