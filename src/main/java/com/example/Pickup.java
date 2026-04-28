package com.example;

public class Pickup {
    public double x, y;
    public boolean collected = false;
    public static final double RADIUS = 8;

    public Pickup(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean isCollectedBy(double px, double py) {
        double dx = px - x;
        double dy = py - y;
        return Math.sqrt(dx * dx + dy * dy) < RADIUS + 4;
    }
}
