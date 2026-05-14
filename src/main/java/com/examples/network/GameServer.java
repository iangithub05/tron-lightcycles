package com.example.network;

import com.example.models.Direction;
import com.example.models.Game;
import java.util.function.Consumer;

public class GameServer {

    public Consumer<Integer> onGuestConnected;
    public Consumer<String> onError;
    public java.util.function.BiConsumer<String, String> onGameOver;

    private Game game;

    public void listen(int port) {
    }

    public void startGame() {
    }

    public void queueHostDirection(Direction dir) {
    }

    public void close() {
    }

    public Game getGame() {
        return game;
    }
}
