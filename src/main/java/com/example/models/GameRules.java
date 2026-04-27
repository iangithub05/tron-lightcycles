package com.example.models;

// All tunable numbers that define how the game physically works.
// Change these here — everything else reads from this object.
public class GameRules {
    public int    gridWidth          = 800;
    public int    gridHeight         = 600;
    public double playerSpeed        = 2.0;

    // How close (in pixels) a player must be to a trail point to count as a hit.
    // Keep this equal to playerSpeed — if it's lower, fast players can skip through trails.
    public double collisionTolerance = 2.0;
}
