package com.example.network;

import com.example.models.Direction;
import com.example.models.LobbySettings;
import com.example.models.PlayerSnapshot;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;

public class GameClient {

    public Consumer<Integer> onConnected;
    public BiConsumer<List<String>, LobbySettings> onLobbyUpdate;
    public Consumer<LobbySettings> onSettingsChanged;
    public BiConsumer<String,String> onChatMessage;
    public Runnable onGameStart;
    public Consumer<List<PlayerSnapshot>> onStateUpdate;
    public BiConsumer<Integer,int[]> onRoundOver;
    public Consumer<Integer> onMatchOver;
    public Consumer<String> onPlayerDisconnected;
    public Consumer<String> onError;

    private int            mySlot = -1;
    private LobbySettings  settings;
    private Socket         socket;
    private PrintWriter    out;

    public void connect(String ip, int port, String playerName) {
        Thread t = new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                socket.setTcpNoDelay(true);
                socket.setKeepAlive(true);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                out.println(NetworkMessage.make(NetworkMessage.HELLO, playerName));

                String line;
                while ((line = in.readLine()) != null) {
                    handleMessage(line.trim());
                }
            } catch (Exception e) {
                if (onError != null) onError.accept(e.getMessage());
            }
        }, "game-client");
        t.setDaemon(true);
        t.start();
    }

    public void sendDirection(Direction dir) {
        if (out != null)
            out.println(NetworkMessage.make(NetworkMessage.INPUT, dir.name()));
    }

    public void sendChat(String name, String message) {
        if (out != null)
            out.println(NetworkMessage.make(NetworkMessage.CHAT, name, message));
    }

    public int    getMySlot() { return mySlot; }
    public LobbySettings getSettings() { return settings; }

    public void close() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }


    private void handleMessage(String raw) {
        String[] parts = NetworkMessage.parse(raw);
        switch (parts[0]) {
            case NetworkMessage.WELCOME -> {
                mySlot = Integer.parseInt(parts[1]);
                settings = LobbySettings.decode(parts[2]);
                if (onConnected != null) onConnected.accept(mySlot);
            }
            case NetworkMessage.LOBBY_STATE -> {
                List<String> names = Arrays.asList(parts[1].split(",", -1));
                if (onLobbyUpdate != null) onLobbyUpdate.accept(names, settings);
            }
            case NetworkMessage.SETTINGS -> {
                settings = LobbySettings.decode(parts[1]);
                if (onSettingsChanged != null) onSettingsChanged.accept(settings);
            }
            case NetworkMessage.CHAT -> {
                if (parts.length > 2 && onChatMessage != null)
                    onChatMessage.accept(parts[1], parts[2]);
            }
            case NetworkMessage.START -> {
                if (onGameStart != null) onGameStart.run();
            }
            case NetworkMessage.STATE -> {
                if (parts.length < 2 || onStateUpdate == null) break;
                List<PlayerSnapshot> snaps = NetworkMessage.decodeSnapshots(parts[1]);
                onStateUpdate.accept(snaps);
            }
            case NetworkMessage.ROUND_OVER -> {
                if (onRoundOver == null) break;
                int winSlot = Integer.parseInt(parts[1]);
                String[] sc = parts[2].split(",");
                int[] scores = new int[sc.length];
                for (int i = 0; i < sc.length; i++) scores[i] = Integer.parseInt(sc[i]);
                onRoundOver.accept(winSlot, scores);
            }
            case NetworkMessage.MATCH_OVER -> {
                if (onMatchOver != null) onMatchOver.accept(Integer.parseInt(parts[1]));
            }
            case NetworkMessage.DISCONNECT -> {
                if (parts.length > 1 && onPlayerDisconnected != null)
                    onPlayerDisconnected.accept(parts[1]);
            }
            case NetworkMessage.PING -> {
                if (out != null) out.println(NetworkMessage.PONG);
            }
        }
    }
}
