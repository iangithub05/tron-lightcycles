package com.example.services;

import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.Player;
import com.example.models.Point;

import java.util.Arrays;
import java.util.Random;

/**
 * Singleplayer Medium AI: actively tries to intercept and take down the human.
 * Lower survival concern than Hard — it will sacrifice space to chase the player.
 * Uses 12-step lookahead to predict where the human is heading.
 */
public class SPMediumAI implements AIController {

    private static final int CELL_SIZE = 8;
    private static final int COLS = 1280 / CELL_SIZE;
    private static final int ROWS = 720 / CELL_SIZE;
    private static final int CELLS = COLS * ROWS;
    private static final int MAX_FLOOD = 1500;
    private static final int LOOK_AHEAD = 12;

    private static final double SPACE_WEIGHT     = 0.4;
    private static final double INTERCEPT_WEIGHT = 70.0;
    private static final double MOMENTUM         = 1.12;
    private static final double NOISE            = 0.05;

    private final boolean[] occupancy = new boolean[CELLS];
    private final int[] visitGen = new int[CELLS];
    private final int[] bfsQueue = new int[CELLS];
    private int genStamp = 0;
    private int bfsTail = 0;

    private final Random rng = new Random();

    @Override
    public Direction computeDirection(Player player, Game game) {
        buildOccupancy(game, player);

        Player human = game.players.get(0);
        double speed = game.rules.playerSpeed;

        // Predict where the human will be in LOOK_AHEAD steps
        double targetX = clampD(human.x + dx(human.direction) * speed * LOOK_AHEAD,
                                0, game.rules.gridWidth);
        double targetY = clampD(human.y + dy(human.direction) * speed * LOOK_AHEAD,
                                0, game.rules.gridHeight);

        Direction best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Direction d : Direction.values()) {
            if (isReverse(player.direction, d)) continue;

            double nx = player.x + dx(d) * speed;
            double ny = player.y + dy(d) * speed;
            if (isDeadly(nx, ny, game)) continue;

            int cellX = clamp(toCellX(player.x) + (int) dx(d), 0, COLS - 1);
            int cellY = clamp(toCellY(player.y) + (int) dy(d), 0, ROWS - 1);
            double space = floodFill(cellX, cellY);

            double distBefore = dist(player.x, player.y, targetX, targetY);
            double distAfter  = dist(nx, ny, targetX, targetY);

            // Low space concern, high human-intercept drive
            double score = space * SPACE_WEIGHT
                         + (distBefore - distAfter) * INTERCEPT_WEIGHT;

            if (d == player.direction) score *= MOMENTUM;
            score += (rng.nextDouble() * 2.0 - 1.0) * Math.abs(score) * NOISE;

            if (score > bestScore) {
                bestScore = score;
                best = d;
            }
        }

        return best != null ? best : player.direction;
    }

    private void buildOccupancy(Game game, Player self) {
        Arrays.fill(occupancy, false);
        for (Player p : game.players) {
            for (Point pt : p.trail.points) {
                int idx = flatIdx(toCellX(pt.x), toCellY(pt.y));
                if (idx >= 0) occupancy[idx] = true;
            }
            if (p != self && p.alive) {
                int idx = flatIdx(toCellX(p.x), toCellY(p.y));
                if (idx >= 0) occupancy[idx] = true;
            }
        }
    }

    private int floodFill(int sx, int sy) {
        int startIdx = flatIdx(sx, sy);
        if (startIdx < 0 || occupancy[startIdx]) return 0;

        genStamp++;
        visitGen[startIdx] = genStamp;
        bfsQueue[0] = startIdx;
        bfsTail = 1;

        int head = 0, count = 0;
        while (head < bfsTail && count < MAX_FLOOD) {
            int idx = bfsQueue[head++];
            count++;
            int cx = idx / ROWS;
            int cy = idx % ROWS;
            expand(cx - 1, cy);
            expand(cx + 1, cy);
            expand(cx, cy - 1);
            expand(cx, cy + 1);
        }
        return count;
    }

    private void expand(int cx, int cy) {
        if (cx < 0 || cx >= COLS || cy < 0 || cy >= ROWS) return;
        int idx = cx * ROWS + cy;
        if (!occupancy[idx] && visitGen[idx] != genStamp) {
            visitGen[idx] = genStamp;
            bfsQueue[bfsTail++] = idx;
        }
    }

    private boolean isDeadly(double nx, double ny, Game game) {
        if (nx < 0 || nx > game.rules.gridWidth || ny < 0 || ny > game.rules.gridHeight) return true;
        for (Player p : game.players)
            if (p.trail.contains(nx, ny, game.rules.collisionTolerance)) return true;
        return false;
    }

    private boolean isReverse(Direction a, Direction b) {
        return (a == Direction.UP    && b == Direction.DOWN)
            || (a == Direction.DOWN  && b == Direction.UP)
            || (a == Direction.LEFT  && b == Direction.RIGHT)
            || (a == Direction.RIGHT && b == Direction.LEFT);
    }

    private int flatIdx(int cx, int cy) {
        if (cx < 0 || cx >= COLS || cy < 0 || cy >= ROWS) return -1;
        return cx * ROWS + cy;
    }

    private int toCellX(double x) { return Math.max(0, Math.min(COLS - 1, (int)(x / CELL_SIZE))); }
    private int toCellY(double y) { return Math.max(0, Math.min(ROWS - 1, (int)(y / CELL_SIZE))); }
    private int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private double clampD(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }
    private double dx(Direction d) { return d == Direction.RIGHT ? 1.0 : d == Direction.LEFT ? -1.0 : 0.0; }
    private double dy(Direction d) { return d == Direction.DOWN  ? 1.0 : d == Direction.UP   ? -1.0 : 0.0; }
    private double dist(double ax, double ay, double bx, double by) { return Math.hypot(ax - bx, ay - by); }
}
