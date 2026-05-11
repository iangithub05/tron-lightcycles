package com.example.models;

import java.util.ArrayList;
import java.util.List;

// Core game engine — handles movement, collision, and win detection.
// Has no concept of UI, input, or networking; just pure game rules.
public class Game {

    public List<Player> players = new ArrayList<>();
    public GameRules    rules;

    public Game(GameRules rules) {
        this.rules = rules;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    // Advance the game by one step: move everyone and check for collisions.
    public void update() {
        for (Player player : players) {
            if (!player.alive) continue;
            player.move();
            checkCollisions(player);
        }
    }

    private void checkCollisions(Player player) {
        // Hit a wall
        if (player.x < 0 || player.x > rules.gridWidth ||
            player.y < 0 || player.y > rules.gridHeight) {
            player.alive = false;
            return;
        }

        // Hit any trail (including their own).
        // collisionTolerance is the pixel margin — should match playerSpeed.
        for (Player p : players) {
            if (p.trail.contains(player.x, player.y, rules.collisionTolerance)) {
                player.alive = false;
                return;
            }
        }
    }

    // Returns the last surviving player, or null if everyone is dead.
    public Player getWinner() {
        for (Player p : players) {
            if (p.alive) return p;
        }
        return null;
    }
}
