package com.example.models;

public class GameRules {
    public int    gridWidth = 1280;
    public int    gridHeight = 720;
    public double playerSpeed = 2.0;

    // Must be strictly LESS than playerSpeed so the head never registers
    // as hitting a trail point that is exactly one step behind it.
    // Using 0.75× speed gives clean hits against other players' trails
    // while avoiding false self-collision from the immediately-prior point.
    public double collisionTolerance = 1.5;

    // How many of the most-recent trail points to skip during self-collision.
    // Default (20) preserves multiplayer behaviour. Singleplayer overrides to 3
    // so that tight loops correctly kill the player.
    public int safeSkipCount = 20;

    public Difficulty difficulty = Difficulty.MEDIUM;

    public GameRules() {}

    public GameRules(int gridWidth, int gridHeight, double playerSpeed) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.playerSpeed = playerSpeed;
        this.collisionTolerance = playerSpeed * 0.75;
    }
}