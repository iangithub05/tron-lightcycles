// package com.example.services;

// import com.example.models.Direction;
// import com.example.models.Game;
// import com.example.models.Player;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Random;

// // Reactive AI — only cares about surviving the next single move.
// // No planning, no lookahead, no opponent awareness.
// //
// // Decision order each tick:
// //   1. Collect all directions that don't cause immediate death
// //   2. If still safe, prefer going straight (keeps movement smooth)
// //   3. Otherwise, randomly pick from the remaining safe options
// //   4. If completely cornered, keep going and crash (no choice left)
// public class AvoidDeathAIController implements AIController {

//     private final Random random = new Random();

//     @Override
//     public Direction getNextDirection(Player player, Game game) {

//         // Find every direction that won't immediately kill the player
//         List<Direction> safeMoves = new ArrayList<>();
//         for (Direction dir : Direction.values()) {
//             if (isOpposite(player.direction, dir))
//                 continue; // reversing is not allowed

//             double nextX = player.x + dx(dir) * player.speed;
//             double nextY = player.y + dy(dir) * player.speed;

//             if (isSafe(nextX, nextY, game))
//                 safeMoves.add(dir);
//         }

//         // Completely cornered — no safe option exists, crash is unavoidable
//         if (safeMoves.isEmpty())
//             return player.direction;

//         // Prefer continuing straight — avoids unnecessary zig-zagging
//         if (safeMoves.contains(player.direction))
//             return player.direction;

//         // Can't go straight, so randomly pick one of the remaining safe directions
//         return safeMoves.get(random.nextInt(safeMoves.size()));
//     }

//     // A position is safe if it's inside the grid and not on any player's trail
//     private boolean isSafe(double x, double y, Game game) {
//         if (x < 0 || x > game.rules.gridWidth || y < 0 || y > game.rules.gridHeight)
//             return false;
//         for (Player p : game.players)
//             if (p.trail.contains(x, y, game.rules.collisionTolerance))
//                 return false;
//         return true;
//     }

//     // True when dir is the exact reverse of current (not allowed in Tron)
//     private boolean isOpposite(Direction current, Direction dir) {
//         return (current == Direction.UP && dir == Direction.DOWN) ||
//                 (current == Direction.DOWN && dir == Direction.UP) ||
//                 (current == Direction.LEFT && dir == Direction.RIGHT) ||
//                 (current == Direction.RIGHT && dir == Direction.LEFT);
//     }

//     private double dx(Direction dir) {
//         return switch (dir) {
//             case LEFT -> -1;
//             case RIGHT -> 1;
//             default -> 0;
//         };
//     }

//     private double dy(Direction dir) {
//         return switch (dir) {
//             case UP -> -1;
//             case DOWN -> 1;
//             default -> 0;
//         };
//     }
// }
package com.example.services;

import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.Player;

import java.util.*;

public class AvoidDeathAIController implements AIController {

    private final Random random = new Random();

    private static final int MAX_DEPTH = 100;

    @Override
    public Direction getNextDirection(Player player, Game game) {

        List<Direction> candidates = new ArrayList<>();

        // Step 1: collect safe moves
        for (Direction dir : Direction.values()) {
            if (isOpposite(player.direction, dir))
                continue;

            int nextX = (int) (player.x + dx(dir));
            int nextY = (int) (player.y + dy(dir));

            if (isSafe(nextX, nextY, game)) {
                candidates.add(dir);
            }
        }

        // No safe moves → accept death
        if (candidates.isEmpty())
            return player.direction;

        Direction bestDir = player.direction;
        int bestScore = Integer.MIN_VALUE;

        // Step 2: evaluate each move
        for (Direction dir : candidates) {

            int nextX = (int) (player.x + dx(dir));
            int nextY = (int) (player.y + dy(dir));

            // 🧠 CORE IDEA: compare territory
            int mySpace = floodFill(nextX, nextY, game);
            int enemySpace = estimateOpponentSpace(game, player);

            int score = mySpace - enemySpace;

            // prefer smoother movement
            if (dir == player.direction)
                score += 5;

            // avoid tight spots
            score += countFreeNeighbors(nextX, nextY, game) * 3;

            // tiny randomness to avoid loops
            score += random.nextInt(3);

            if (score > bestScore) {
                bestScore = score;
                bestDir = dir;
            }
        }

        return bestDir;
    }

    // Flood fill (BFS)
    private int floodFill(int startX, int startY, Game game) {

        int width = (int) game.rules.gridWidth;
        int height = (int) game.rules.gridHeight;

        boolean[][] visited = new boolean[height][width];

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[] { startX, startY });

        int count = 0;

        while (!queue.isEmpty() && count < MAX_DEPTH) {

            int[] node = queue.poll();
            int x = node[0];
            int y = node[1];

            if (x < 0 || x >= width || y < 0 || y >= height)
                continue;
            if (visited[y][x])
                continue;

            if (!isSafe(x, y, game))
                continue;

            visited[y][x] = true;
            count++;

            queue.add(new int[] { x + 1, y });
            queue.add(new int[] { x - 1, y });
            queue.add(new int[] { x, y + 1 });
            queue.add(new int[] { x, y - 1 });
        }

        return count;
    }

    // Estimate opponent available space
    private int estimateOpponentSpace(Game game, Player self) {
        for (Player p : game.players) {
            if (p != self) {
                return floodFill((int) p.x, (int) p.y, game);
            }
        }
        return 0;
    }

    // Measures openness (prevents tunnels)
    private int countFreeNeighbors(int x, int y, Game game) {
        int count = 0;

        if (isSafe(x + 1, y, game))
            count++;
        if (isSafe(x - 1, y, game))
            count++;
        if (isSafe(x, y + 1, game))
            count++;
        if (isSafe(x, y - 1, game))
            count++;

        return count;
    }

    private boolean isSafe(int x, int y, Game game) {
        if (x < 0 || x >= game.rules.gridWidth || y < 0 || y >= game.rules.gridHeight)
            return false;

        for (Player p : game.players) {
            if (p.trail.contains(x, y, game.rules.collisionTolerance))
                return false;
        }

        return true;
    }

    private boolean isOpposite(Direction current, Direction dir) {
        return (current == Direction.UP && dir == Direction.DOWN) ||
                (current == Direction.DOWN && dir == Direction.UP) ||
                (current == Direction.LEFT && dir == Direction.RIGHT) ||
                (current == Direction.RIGHT && dir == Direction.LEFT);
    }

    private int dx(Direction dir) {
        return switch (dir) {
            case LEFT -> -1;
            case RIGHT -> 1;
            default -> 0;
        };
    }

    private int dy(Direction dir) {
        return switch (dir) {
            case UP -> -1;
            case DOWN -> 1;
            default -> 0;
        };
    }
}