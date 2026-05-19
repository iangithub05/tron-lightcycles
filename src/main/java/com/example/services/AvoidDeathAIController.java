package com.example.services;

import com.example.models.Difficulty;
import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.Player;
import com.example.models.Point;

import java.util.Arrays;
import java.util.Random;

/**
 * Difficulty-based AI that targets the human player (game.players.get(0)).
 *
 * Core scoring per candidate direction:
 *
 *   score = floodFill(selfNextCell) * spaceWeight          ← self-survival
 *         + (distToTarget_before - distToTarget_after)
 *             * interceptWeight                            ← hunt the human
 *         × momentumFactor  (if d == currentDirection)    ← prevent zigzag
 *         + noise  (EASY / Chaotic only)                  ← imperfection
 *
 * The "target" is NOT the closest opponent — it is always the human's
 * PREDICTED position (current + direction * playerSpeed * lookAheadSteps).
 * This makes HARD AI cut ahead of the human instead of chasing from behind.
 *
 * Difficulty controls the survival-vs-hunt trade-off:
 *   EASY   – strongly values open space, barely hunts; survives long but poses little threat
 *   MEDIUM – balanced: solid survival + real hunting pressure; genuinely dangerous
 *   HARD   – aggressive hunter; survival instinct present but hunt always dominates
 *
 * Personality (same index across all difficulties):
 *   0 = Hunter   – highest intercept, willing to sacrifice space to kill
 *   1 = Trapper  – strong survival + moderate intercept; walls the human in
 *   2 = Ambusher – balanced + slight noise; unpredictable approach angle
 */
public class AvoidDeathAIController implements AIController {

    private static final int CELL_SIZE = 8;
    private static final int COLS  = 1280 / CELL_SIZE;   // 160
    private static final int ROWS  =  720 / CELL_SIZE;   //  90
    private static final int CELLS = COLS * ROWS;         // 14 400

    private final double spaceWeight;
    private final double interceptWeight;
    private final double momentumFactor;
    private final double noiseScale;
    private final int    maxFloodCells;
    private final int    lookAheadSteps;     // predict human N steps ahead
    private final double randomMoveProbability; // chance of picking a random safe move (imperfection)

    // Pre-allocated BFS structures — zero per-frame GC pressure
    private final boolean[] occupancy = new boolean[CELLS];
    private final int[]     visitGen  = new int[CELLS];
    private final int[]     bfsQueue  = new int[CELLS];
    private int genStamp = 0;
    private int bfsTail  = 0;

    private final Random rng = new Random();

    public AvoidDeathAIController(Difficulty difficulty, int personality) {

        double baseSpace, baseIntercept, baseMomentum, baseNoise, baseRandom;
        int    baseFlood, baseLookAhead;

        switch (difficulty) {
            case EASY -> {
                // Survival-first: large spaceWeight keeps it alive; low intercept
                // means it barely chases the human. Occasional random safe moves
                // make it imperfect and beatable without causing self-destruction.
                baseSpace = 2.5;   baseIntercept = 8;   baseMomentum = 1.40;
                baseNoise = 0.10;  baseFlood = 4000; baseLookAhead = 0;
                baseRandom = 0.06;
            }
            case HARD -> {
                // MAXIMUM AGGRESSION: intercept=6000 makes every personality
                // overwhelmingly hunt-dominant (Hunter 26x, Ambusher 7.6x,
                // Trapper 2.9x — no passive play anywhere). Momentum=1.20 lets
                // all three pivot quickly when the human dodges; full-arena flood
                // (CELLS) means they always choose the safe pivot, never self-trap.
                // 100-step lookahead (700 px at speed 7) aims them at the human's
                // position almost an entire arena-width ahead — they cross the map
                // diagonally to cut off every escape route.
                baseSpace = 0.5;   baseIntercept = 6000; baseMomentum = 1.20;
                baseNoise = 0.0;   baseFlood = CELLS;    baseLookAhead = 100;
                baseRandom = 0.0;
            }
            default -> {
                // MEDIUM: genuine balance — survives well and actively hunts.
                baseSpace = 1.8;   baseIntercept = 70;  baseMomentum = 1.40;
                baseNoise = 0.0;   baseFlood = 5000; baseLookAhead = 30;
                baseRandom = 0.02;
            }
        }

        switch (personality) {
            case 0 -> {
                // Hunter: primary goal is to reach the human's path — accepts
                // reduced personal space in exchange for maximum intercept pressure.
                spaceWeight           = baseSpace * 0.55;
                interceptWeight       = baseIntercept * 2.2;
                momentumFactor        = baseMomentum;
                noiseScale            = baseNoise;
                randomMoveProbability = baseRandom * 0.85;
            }
            case 1 -> {
                // Trapper: survives long, walls the human into shrinking space.
                // Less direct pursuit but stays alive to close off escape routes.
                spaceWeight           = baseSpace * 1.50;
                interceptWeight       = baseIntercept * 0.75;
                momentumFactor        = baseMomentum * 1.06;
                noiseScale            = Math.max(0, baseNoise - 0.05);
                randomMoveProbability = baseRandom * 0.60;
            }
            default -> {
                // Ambusher: balanced approach with slight unpredictability so the
                // human can't read its angle of attack as easily.
                spaceWeight           = baseSpace;
                interceptWeight       = baseIntercept * 1.30;
                momentumFactor        = baseMomentum * 0.95;
                noiseScale            = baseNoise + 0.06;
                randomMoveProbability = baseRandom * 1.15;
            }
        }

        maxFloodCells  = baseFlood;
        lookAheadSteps = baseLookAhead;
    }

    // -------------------------------------------------------------------------
    // Main decision
    // -------------------------------------------------------------------------

    @Override
    public Direction computeDirection(Player player, Game game) {
        buildOccupancy(game, player);

        // Occasionally pick a random safe move to make the AI imperfect and beatable.
        if (randomMoveProbability > 0 && rng.nextDouble() < randomMoveProbability) {
            Direction[] safe = new Direction[4];
            int safeCount = 0;
            for (Direction d : Direction.values()) {
                if (isReverse(player.direction, d)) continue;
                double nx = player.x + dx(d) * game.rules.playerSpeed;
                double ny = player.y + dy(d) * game.rules.playerSpeed;
                if (!isDeadly(nx, ny, game)) safe[safeCount++] = d;
            }
            if (safeCount > 0) return safe[rng.nextInt(safeCount)];
        }

        // Always target the human (index 0).  If human is dead, fall back to
        // the closest surviving opponent so the AIs keep fighting each other.
        Player human = game.players.get(0);
        Player target = human.alive ? human : closestOpponent(player, game);

        // Predicted interception point: where the target will be in N steps.
        // lookAheadSteps = 0 means react to current position (EASY behaviour).
        double targetX = target.x;
        double targetY = target.y;
        if (lookAheadSteps > 0) {
            double speed = game.rules.playerSpeed;
            targetX = clampD(target.x + dx(target.direction) * speed * lookAheadSteps,
                             0, game.rules.gridWidth);
            targetY = clampD(target.y + dy(target.direction) * speed * lookAheadSteps,
                             0, game.rules.gridHeight);
        }

        Direction best    = null;
        double    bestScore = Double.NEGATIVE_INFINITY;

        for (Direction d : Direction.values()) {
            if (isReverse(player.direction, d)) continue;

            // Immediate collision check (pixel-precise)
            double nx = player.x + dx(d) * game.rules.playerSpeed;
            double ny = player.y + dy(d) * game.rules.playerSpeed;
            if (isDeadly(nx, ny, game)) continue;

            // 1. Self-survival: flood-fill available space one cell ahead
            int cellX = clamp(toCellX(player.x) + (int) dx(d), 0, COLS - 1);
            int cellY = clamp(toCellY(player.y) + (int) dy(d), 0, ROWS - 1);
            double score = floodFill(cellX, cellY) * spaceWeight;

            // 2. Hunt score: reward closing distance to target's predicted position.
            //    Moving closer = positive; moving away = penalty (negative).
            double distBefore = dist(player.x, player.y, targetX, targetY);
            double distAfter  = dist(nx, ny, targetX, targetY);
            score += (distBefore - distAfter) * interceptWeight;

            // 3. Momentum: require a meaningful improvement before changing direction
            if (d == player.direction) score *= momentumFactor;

            // 4. Noise: proportional jitter (EASY / Ambusher only)
            if (noiseScale > 0) {
                score += (rng.nextDouble() * 2.0 - 1.0) * Math.abs(score) * noiseScale;
            }

            if (score > bestScore) {
                bestScore = score;
                best      = d;
            }
        }

        return best != null ? best : player.direction;
    }

    // -------------------------------------------------------------------------
    // Occupancy grid
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // BFS flood-fill (zero per-call allocation via generation stamps)
    // -------------------------------------------------------------------------

    private int floodFill(int sx, int sy) {
        int startIdx = flatIdx(sx, sy);
        if (startIdx < 0 || occupancy[startIdx]) return 0;

        genStamp++;
        visitGen[startIdx] = genStamp;
        bfsQueue[0]        = startIdx;
        bfsTail            = 1;

        int head = 0, count = 0;
        while (head < bfsTail && count < maxFloodCells) {
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
            visitGen[idx]       = genStamp;
            bfsQueue[bfsTail++] = idx;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isDeadly(double nx, double ny, Game game) {
        if (nx < 0 || nx > game.rules.gridWidth
                   || ny < 0 || ny > game.rules.gridHeight) return true;
        for (Player p : game.players)
            if (p.trail.contains(nx, ny, game.rules.collisionTolerance)) return true;
        return false;
    }

    private Player closestOpponent(Player self, Game game) {
        Player closest = null;
        double minDist = Double.MAX_VALUE;
        for (Player p : game.players) {
            if (p == self || !p.alive) continue;
            double d = dist(self.x, self.y, p.x, p.y);
            if (d < minDist) { minDist = d; closest = p; }
        }
        return closest;
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

    private int    clamp(int v, int lo, int hi)       { return Math.max(lo, Math.min(hi, v)); }
    private double clampD(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }

    private double dx(Direction d) { return d == Direction.RIGHT ? 1.0 : d == Direction.LEFT  ? -1.0 : 0.0; }
    private double dy(Direction d) { return d == Direction.DOWN  ? 1.0 : d == Direction.UP    ? -1.0 : 0.0; }

    private double dist(double ax, double ay, double bx, double by) { return Math.hypot(ax - bx, ay - by); }
}
