package com.example.services;

import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.Player;

import java.util.*;

// Smartest AI: uses BFS to measure open space in each direction, then picks
// the one with the most room. More open space = more time before getting trapped.
public class PathfindingAIController implements AIController {

    // Stop the BFS early once we've counted this many cells — keeps it fast enough for real-time use
    private static final int MAX_CELLS = 200;

    @Override
    public Direction getNextDirection(Player player, Game game) {
        Direction best      = player.direction;
        int       bestScore = -1;

        for (Direction dir : Direction.values()) {
            if (isOpposite(player.direction, dir)) continue; // can't reverse

            double nx = player.x + dx(dir) * player.speed;
            double ny = player.y + dy(dir) * player.speed;

            if (!isClear(nx, ny, game)) continue; // skip immediately dangerous moves

            // Score = how many cells are reachable from this next position
            int score = countReachableCells(nx, ny, player.speed, game);
            if (score > bestScore) {
                bestScore = score;
                best      = dir;
            }
        }

        return best;
    }

    // BFS flood fill: starts at (startX, startY) and expands outward,
    // counting every open cell reachable from there (up to MAX_CELLS).
    // Higher count = more open space = better choice.
    private int countReachableCells(double startX, double startY, double speed, Game game) {
        int step = (int) speed;

        Set<String>  visited = new HashSet<>();
        Queue<int[]> queue   = new LinkedList<>();

        int sx = (int) startX, sy = (int) startY;
        queue.add(new int[]{sx, sy});
        visited.add(sx + "," + sy);

        int count = 0;
        while (!queue.isEmpty() && count < MAX_CELLS) {
            int[] cell = queue.poll();
            count++;

            for (Direction dir : Direction.values()) {
                int nx = cell[0] + (int) (dx(dir) * step);
                int ny = cell[1] + (int) (dy(dir) * step);
                String key = nx + "," + ny;

                if (!visited.contains(key) && isClear(nx, ny, game)) {
                    visited.add(key);
                    queue.add(new int[]{nx, ny});
                }
            }
        }

        return count;
    }

    // Returns true if the position is inside the grid and not on any player's trail
    private boolean isClear(double x, double y, Game game) {
        if (x < 0 || x > game.rules.gridWidth || y < 0 || y > game.rules.gridHeight)
            return false;
        for (Player p : game.players)
            if (p.trail.contains(x, y, game.rules.collisionTolerance)) return false;
        return true;
    }

    private boolean isOpposite(Direction current, Direction next) {
        return (current == Direction.UP    && next == Direction.DOWN)  ||
               (current == Direction.DOWN  && next == Direction.UP)    ||
               (current == Direction.LEFT  && next == Direction.RIGHT) ||
               (current == Direction.RIGHT && next == Direction.LEFT);
    }

    private double dx(Direction dir) {
        return switch (dir) { case LEFT -> -1; case RIGHT -> 1; default -> 0; };
    }

    private double dy(Direction dir) {
        return switch (dir) { case UP -> -1; case DOWN -> 1; default -> 0; };
    }
}
