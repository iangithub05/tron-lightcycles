package com.example.models;

import java.util.ArrayList;
import java.util.List;

public class Game {

    public List<Player> players = new ArrayList<>();
    public GameRules rules;

    public Game(GameRules rules) {
        this.rules = rules;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void update() {
        for (Player player : players) {
            if (!player.alive) continue;
            player.move();
            checkCollisions(player);
        }
    }

    private void checkCollisions(Player player) {
        if (player.x < 0 || player.x > rules.gridWidth ||
            player.y < 0 || player.y > rules.gridHeight) {
            player.alive = false;
            return;
        }
        for (Player p : players) {
            if (p.trail.contains(player.x, player.y, rules.collisionTolerance)) {
                player.alive = false;
                return;
            }
        }
    }

    public Player getWinner() {
        Player alive = null;
        for (Player p : players) {
            if (p.alive) {
                if (alive != null) return null;
                alive = p;
            }
        }
        return alive;
    }

    public boolean isOver() {
        int aliveCount = 0;
        for (Player p : players) {
            if (p.alive) aliveCount++;
        }
        return aliveCount <= 1;
    }
}
