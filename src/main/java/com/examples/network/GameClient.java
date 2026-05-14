package com.example.network;

import com.example.models.Direction;
import com.example.network.NetworkMessage.PlayerSnapshot;
import java.util.List;
import java.util.function.Consumer;

public class GameClient {

    public Consumer<Integer> onConnected;
    public Consumer<List<PlayerSnapshot>> onStateUpdate;
    public Consumer<String> onGameOver;
    public Consumer<String> onError;

    public void connect(String ip, int port) {
    }

    public void sendDirection(Direction dir) {
    }

    public void close() {
    }
}
