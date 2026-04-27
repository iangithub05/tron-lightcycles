package com.example.models;

// A player's position, direction, speed, and trail.
// setDirection() enforces the Tron rule of no reversing.
public class Player {

    public double    x, y;
    public Direction direction;
    public double    speed = 2;
    public boolean   alive = true;
    public Trail     trail;

    public Player(double x, double y) {
        this.x         = x;
        this.y         = y;
        this.direction = Direction.RIGHT;
        this.trail     = new Trail();
    }

    // Records current position in the trail, then moves one step forward.
    public void move() {
        trail.addPoint(x, y);

        switch (direction) {
            case UP    -> y -= speed;
            case DOWN  -> y += speed;
            case LEFT  -> x -= speed;
            case RIGHT -> x += speed;
        }
    }

    // Changes direction, but silently ignores attempts to reverse (Tron rule).
    public void setDirection(Direction newDir) {
        boolean isReverse =
            (direction == Direction.UP    && newDir == Direction.DOWN)  ||
            (direction == Direction.DOWN  && newDir == Direction.UP)    ||
            (direction == Direction.LEFT  && newDir == Direction.RIGHT) ||
            (direction == Direction.RIGHT && newDir == Direction.LEFT);

        if (!isReverse) direction = newDir;
    }
}
