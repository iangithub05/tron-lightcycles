package com.example;

import com.example.controllers.*;
import com.example.models.LobbySettings;
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
        // showHostLobby();
        Platform.runLater(() -> scene.getRoot().requestFocus());
    }


    public void showAskName() { swap(new AskNameScreen(this).getView()); }
    public void showMainMenu() { swap(new MainMenuScreen(this).getView()); }
    public void showSingleplayer(){ swap(new SingleplayerScreen(this).getView()); }
    public void showMultiplayer() { swap(new MultiplayerScreen(this).getView()); }
    public void showHostLobby() { swap(new HostLobbyScreen(this).getView()); }
    public void showJoinLobby() { swap(new JoinLobbyScreen(this).getView()); }
    public void showAbout() { swap(new AboutScreen(this).getView()); }

    public void showGame(GameSession session) {
        swap(new GameScreen(this, session).getView());
    }

    public void showGameOver(String result, GameSession session) {
        swap(new GameOverScreen(this, result, session).getView());
    }

    public void showMultiplayerHostGame(GameServer server,
                                        LobbySettings settings, String name) {
        swap(new MultiplayerGameScreen(this, server, settings, name).getView());
    }

    public void showMultiplayerClientGame(GameClient client,
                                          LobbySettings settings, String name) {
        swap(new MultiplayerGameScreen(this, client, settings, name).getView());
    }

    public void showMultiplayerMatchOver(String result, int[] scores) {
        swap(new MatchOverScreen(this, result, scores).getView());
    }

    public void showMultiplayerGame(GameServer server) {}
    public void showMultiplayerGame(GameClient client) {}
    public void showMultiplayerGameOver(String result) {
        swap(new MatchOverScreen(this, result, null).getView());
    }

    public String getPlayerName() { return playerName; }
    public void   setPlayerName(String n) { this.playerName = n; }


    private void swap(javafx.scene.Parent root) {
        scene.setRoot(root);
        Platform.runLater(() -> scene.getRoot().requestFocus());
    }

    public static void main(String[] args) { launch(); }
}
