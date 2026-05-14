package com.example.models;

public class GameRules {
    public int gridWidth = 1280;
    public int gridHeight = 720;
    public double playerSpeed = 2.0;
    public double collisionTolerance = 2.0;
    public Difficulty difficulty = Difficulty.MEDIUM;

    public GameRules() {
    }

    public GameRules(int gridWidth, int gridHeight, double playerSpeed) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.playerSpeed = playerSpeed;
        this.collisionTolerance = playerSpeed;
    }
}
