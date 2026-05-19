package com.example.models;

import java.util.ArrayList;
import java.util.List;

public class PlayerSnapshot {

    public int slot;
    public double x;
    public double y;
    public boolean alive;
    public int score;
    public String name;
    public Direction direction;
    public List<double[]> trailPoints = new ArrayList<>();

    public PlayerSnapshot() {}

    public PlayerSnapshot(int slot, double x, double y, boolean alive,
                          int score, String name, List<double[]> trailPoints) {
        this.slot = slot;
        this.x = x;
        this.y = y;
        this.alive = alive;
        this.score = score;
        this.name = name;
        this.trailPoints = trailPoints != null ? trailPoints : new ArrayList<>();
    }

    public PlayerSnapshot copyWithoutTrail() {
        return new PlayerSnapshot(slot, x, y, alive, score, name, new ArrayList<>());
    }
}
