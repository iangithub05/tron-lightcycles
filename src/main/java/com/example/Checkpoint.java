package com.example;

public class Checkpoint {
    public double x, y, width, height;
    public int index;

    public Checkpoint(int index, double x, double y, double width, double height) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}
