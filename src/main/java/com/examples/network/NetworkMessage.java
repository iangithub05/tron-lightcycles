package com.example.network;

import com.example.models.Direction;

import java.io.Serializable;
import java.util.List;

/**
 * All data exchanged between server and client over the socket.
 *
 * Message types:
 *   DIRECTION  — a player changed direction (client → server, server → both)
 *   STATE      — full game state snapshot     (server → client, every frame)
 *   GAME_OVER  — match ended                  (server → client)
 *   READY      — client signals it is ready   (client → server)
 *   ASSIGN     — server tells client its slot  (server → client)
 */
public class NetworkMessage implements Serializable {

    public enum Type {
        DIRECTION,
        STATE,
        GAME_OVER,
        READY,
        ASSIGN
    }

    // ── common ──────────────────────────────────────────────────────────────
    public final Type type;

    // ── DIRECTION ────────────────────────────────────────────────────────────
    public Direction direction;
    public int       playerSlot; // 0 = host, 1 = guest

    // ── ASSIGN ───────────────────────────────────────────────────────────────
    public int assignedSlot; // slot the client is told it occupies

    // ── STATE ────────────────────────────────────────────────────────────────
    public List<PlayerSnapshot> players;

    // ── GAME_OVER ────────────────────────────────────────────────────────────
    public String resultText; // e.g. "You Win!" / "You Crashed!"

    // ── constructors (factory style for readability) ─────────────────────────

    private NetworkMessage(Type type) {
        this.type = type;
    }

    public static NetworkMessage direction(int slot, Direction dir) {
        NetworkMessage m = new NetworkMessage(Type.DIRECTION);
        m.playerSlot = slot;
        m.direction  = dir;
        return m;
    }

    public static NetworkMessage state(List<PlayerSnapshot> players) {
        NetworkMessage m = new NetworkMessage(Type.STATE);
        m.players = players;
        return m;
    }

    public static NetworkMessage gameOver(String text) {
        NetworkMessage m = new NetworkMessage(Type.GAME_OVER);
        m.resultText = text;
        return m;
    }

    public static NetworkMessage ready() {
        return new NetworkMessage(Type.READY);
    }

    public static NetworkMessage assign(int slot) {
        NetworkMessage m = new NetworkMessage(Type.ASSIGN);
        m.assignedSlot = slot;
        return m;
    }

    // ── snapshot ─────────────────────────────────────────────────────────────

    /** Lightweight serialisable snapshot of one player's state. */
    public static class PlayerSnapshot implements Serializable {
        public double  x, y;
        public boolean alive;
        public List<double[]> trailPoints; // each entry is [x, y]

        public PlayerSnapshot(double x, double y, boolean alive, List<double[]> trailPoints) {
            this.x           = x;
            this.y           = y;
            this.alive       = alive;
            this.trailPoints = trailPoints;
        }
    }
}
