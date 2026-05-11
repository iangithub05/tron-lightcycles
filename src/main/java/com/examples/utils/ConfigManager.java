package com.example.utils;

import com.example.models.Difficulty;
import com.example.models.Direction;
import com.example.models.GameMode;
import javafx.scene.paint.Color;

// Single place to change game settings.
// Edit values here to affect all sessions — no need to dig through logic code.
public class ConfigManager {

    // Current game mode and difficulty
    public static GameMode gameMode = GameMode.VS_AI;
    public static Difficulty difficulty = Difficulty.MEDIUM;

    // Grid dimensions in pixels
    public static int gridWidth = 800;
    public static int gridHeight = 600;
    public static double playerSpeed        = 1.0;  // pixels per frame
    public static double collisionTolerance = 1.0;  // must match playerSpeed

    // Player colors — index 0 is always the human player
    public static final Color[] PLAYER_COLORS = {
            Color.CYAN, // P1 — human
            Color.LIMEGREEN, // P2 — AvoidDeath AI (smartest, reacts fastest)
            Color.ORANGERED, // P3 — Pathfinding AI (medium)
            Color.MEDIUMPURPLE // P4 — Random AI (dumbest, reacts slowest)
    };

    // Players start in the four corners, each heading along the wall.
    // This gives everyone the most room at the start and avoids instant head-ons.
    // P1: top-left → heading right
    // P2: top-right → heading down
    // P3: bot-right → heading left
    // P4: bot-left → heading up
    public static final double[][] START_POSITIONS = {
            { 80, 80 }, // P1 top-left
            { 720, 80 }, // P2 top-right
            { 720, 520 }, // P3 bottom-right
            { 80, 520 }, // P4 bottom-left
    };

    // Starting direction for each player (matches START_POSITIONS above)
    public static final Direction[] START_DIRECTIONS = {
            Direction.RIGHT, // P1
            Direction.DOWN, // P2
            Direction.LEFT, // P3
            Direction.UP, // P4
    };

    // How many frames pass between each AI's decision.
    // Lower = more frequent decisions = feels smarter / harder.
    // Set any value to 1 to make that AI decide every single frame.
    // Index 0 → P2 (AvoidDeath), index 1 → P3 (Pathfinding), index 2 → P4 (Random)
    public static final int[] AI_TICK_INTERVALS = {
            3, // P2 — reacts the fastest
            6, // P3 — medium reaction speed
            10, // P4 — reacts the slowest
    };

    private ConfigManager() {
    } // no instances — all fields are static
}
