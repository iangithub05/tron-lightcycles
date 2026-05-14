package com.example.models;

public class Player {

    public double x;
    public double y;
    public Direction direction;
    public double speed;
    public boolean alive;
    public Trail trail;

    public Player(double x, double y, Direction direction, double speed) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.speed = speed;
        this.alive = true;
        this.trail = new Trail();
    }

    public void move() {
        trail.addPoint(x, y);
        switch (direction) {
            case UP -> y -= speed;
            case DOWN -> y += speed;
            case LEFT -> x -= speed;
            case RIGHT -> x += speed;
        }
    }

    public void setDirection(Direction next) {
        boolean reverse =
            (direction == Direction.UP && next == Direction.DOWN) ||
            (direction == Direction.DOWN && next == Direction.UP) ||
            (direction == Direction.LEFT && next == Direction.RIGHT) ||
            (direction == Direction.RIGHT && next == Direction.LEFT);
        if (!reverse) direction = next;
    }

    public void reset(double startX, double startY, Direction startDir) {
        x = startX;
        y = startY;
        direction = startDir;
        alive = true;
        trail.clear();
    }
}
