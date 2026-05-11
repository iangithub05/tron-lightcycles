package com.example.network;

import com.example.models.Direction;
import com.example.network.NetworkMessage.PlayerSnapshot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.function.Consumer;

/**
 * Runs on the guest machine.
 *
 * Lifecycle:
 *  1. {@link #connect(String, int)} — connects to the host, completes handshake.
 *  2. Fires {@code onConnected} with the assigned slot.
 *  3. Listens for STATE and GAME_OVER messages from the server.
 *  4. {@link #sendDirection(Direction)} — sends direction changes to the host.
 */
public class GameClient {

    // ── callbacks ─────────────────────────────────────────────────────────
    /** Fired once the handshake is done. Arg = our assigned slot (should be 1). */
    public Consumer<Integer>             onConnected;
    /** Fired every time the server sends a new game state. */
    public Consumer<List<PlayerSnapshot>> onStateUpdate;
    /** Fired when the server declares game over. Arg = result text for this client. */
    public Consumer<String>              onGameOver;
    /** Fired on fatal errors. */
    public Consumer<String>              onError;

    // ── state ─────────────────────────────────────────────────────────────
    private Socket             socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;

    private int     mySlot = 1;
    private boolean connected = false;

    // ── public API ────────────────────────────────────────────────────────

    /**
     * Connects to host:port and performs the handshake.
     * Blocks until connected — run on a background thread.
     */
    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);

            // ObjectOutputStream first, then flush, then ObjectInputStream
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in  = new ObjectInputStream(socket.getInputStream());

            // Receive ASSIGN message
            NetworkMessage assign = (NetworkMessage) in.readObject();
            if (assign.type != NetworkMessage.Type.ASSIGN) {
                error("Expected ASSIGN, got: " + assign.type);
                return;
            }
            mySlot = assign.assignedSlot;

            // Reply READY
            send(NetworkMessage.ready());

            connected = true;
            if (onConnected != null) onConnected.accept(mySlot);

            // Start listening for game messages
            receiveLoop();

        } catch (Exception e) {
            error("Client connect error: " + e.getMessage());
        }
    }

    /** Send a direction change to the server. Thread-safe. */
    public void sendDirection(Direction dir) {
        if (!connected) return;
        send(NetworkMessage.direction(mySlot, dir));
    }

    /** Close the connection. */
    public void close() {
        connected = false;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    public int getMySlot() { return mySlot; }

    // ── private ───────────────────────────────────────────────────────────

    private void receiveLoop() {
        while (connected) {
            try {
                NetworkMessage msg = (NetworkMessage) in.readObject();

                switch (msg.type) {
                    case STATE -> {
                        if (onStateUpdate != null) onStateUpdate.accept(msg.players);
                    }
                    case GAME_OVER -> {
                        connected = false;
                        if (onGameOver != null) onGameOver.accept(msg.resultText);
                    }
                    default -> { /* ignore unknown types */ }
                }

            } catch (Exception e) {
                if (connected) error("Client receive error: " + e.getMessage());
                break;
            }
        }
    }

    private synchronized void send(NetworkMessage msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            if (connected) error("Client send error: " + e.getMessage());
        }
    }

    private void error(String msg) {
        connected = false;
        if (onError != null) onError.accept(msg);
    }
}
