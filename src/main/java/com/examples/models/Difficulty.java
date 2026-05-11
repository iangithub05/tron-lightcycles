package com.example.models;

/**
 * AI difficulty levels.
 *
 * AI_TICK_INTERVAL_FRAMES controls how often (in game frames) each AI
 * controller is allowed to make a new decision.
 *
 * Lower  = AI decides more often  (harder / smoother)
 * Higher = AI decides less often  (easier / sluggish)
 * Set to 1 to make AI decide every single frame.
 */
public enum Difficulty {

    EASY(10),    // AI ticks every 10 frames
    MEDIUM(5),   // AI ticks every 5 frames
    HARD(1);     // AI ticks every frame (maximum reaction)

    /** Frames between each AI direction decision. Change this per-enum to tune. */
    public final int AI_TICK_INTERVAL_FRAMES;

    Difficulty(int interval) {
        this.AI_TICK_INTERVAL_FRAMES = interval;
    }
}
