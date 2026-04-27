package com.example;

import com.example.input.InputManager;
import com.example.input.KeyBindings;

import javafx.scene.input.KeyCode;

public class Game {

    public Player player;

    public Game() {
        player = new Player(400, 300); // MUST NOT BE NULL
    }

    public void update() {

        // 1. HANDLE INPUT FIRST
        if (InputManager.isDown(KeyBindings.UP)) {
            player.setDirection(Direction.UP);
        }
        if (InputManager.isDown(KeyBindings.DOWN)) {
            player.setDirection(Direction.DOWN);
        }
        if (InputManager.isDown(KeyBindings.LEFT)) {
            player.setDirection(Direction.LEFT);
        }
        if (InputManager.isDown(KeyBindings.RIGHT)) {
            player.setDirection(Direction.RIGHT);
        }

        // 2. MOVE PLAYER EVERY FRAME (IMPORTANT)
        player.move();

        // 3. COLLISIONS
        if (player.x < 0 || player.x > 800 || player.y < 0 || player.y > 600) {
            player.alive = false;
        }

        if (player.trail.contains(player.x, player.y)) {
            player.alive = false;
        }
    }
}