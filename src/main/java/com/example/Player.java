package com.example;

public class Player {

    public double x, y;
    public Direction direction;
    public double speed = 2;
    public boolean alive = true;
    public Trail trail;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.direction = Direction.RIGHT;
        this.trail = new Trail();
    }

    public void move() {
        trail.addPoint(x, y);

        switch (direction) {
            case UP    -> y -= speed;
            case DOWN  -> y += speed;
            case LEFT  -> x -= speed;
            case RIGHT -> x += speed;
        }
    }

    public void setDirection(Direction newDir) {
        // Prevent 180-degree reversal
        if ((direction == Direction.UP    && newDir == Direction.DOWN)  ||
            (direction == Direction.DOWN  && newDir == Direction.UP)    ||
            (direction == Direction.LEFT  && newDir == Direction.RIGHT) ||
            (direction == Direction.RIGHT && newDir == Direction.LEFT)) {
            return;
        }
        direction = newDir;
    }
}
