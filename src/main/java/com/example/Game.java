package com.example;

import com.example.input.InputManager;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {

    // ── Constants ──────────────────────────────────────────────────────────
    public static final int WIDTH  = 800;
    public static final int HEIGHT = 600;

    public static final int    TARGET_SCORE    = 5000;
    public static final int    TARGET_PICKUPS  = 20;
    public static final int    TARGET_LAPS     = 3;
    public static final double TARGET_FILL_PCT = 0.30;
    public static final long   SURVIVE_NANOS   = 60_000_000_000L; // 60 s

    // ── State ──────────────────────────────────────────────────────────────
    public Player    player;
    public GameMode  mode;
    public boolean   won = false;

    // Time survival
    private long startNanos = -1;
    public  long elapsedNanos = 0;

    // Pickups mode
    public List<Pickup> pickups = new ArrayList<>();
    public int pickupsCollected = 0;

    // Laps mode
    public List<Checkpoint> checkpoints = new ArrayList<>();
    public int nextCheckpoint = 0;
    public int lapsCompleted  = 0;

    // ── Constructor ────────────────────────────────────────────────────────
    public Game(GameMode mode) {
        this.mode = mode;
        player = new Player(WIDTH / 2.0, HEIGHT / 2.0);

        if (mode == GameMode.PICKUPS) {
            spawnPickups();
        }
        if (mode == GameMode.LAPS) {
            setupCheckpoints();
        }
    }

    // ── Update (called every frame) ────────────────────────────────────────
    public void update(long now) {
        if (!player.alive || won) return;

        // Start timer on first frame
        if (startNanos < 0) startNanos = now;
        elapsedNanos = now - startNanos;

        // --- Input ---
        if (InputManager.isDown(KeyCode.UP)    || InputManager.isDown(KeyCode.W)) player.setDirection(Direction.UP);
        if (InputManager.isDown(KeyCode.DOWN)  || InputManager.isDown(KeyCode.S)) player.setDirection(Direction.DOWN);
        if (InputManager.isDown(KeyCode.LEFT)  || InputManager.isDown(KeyCode.A)) player.setDirection(Direction.LEFT);
        if (InputManager.isDown(KeyCode.RIGHT) || InputManager.isDown(KeyCode.D)) player.setDirection(Direction.RIGHT);

        // --- Move ---
        player.move();

        // --- Wall collision ---
        if (player.x < 0 || player.x > WIDTH || player.y < 0 || player.y > HEIGHT) {
            player.alive = false;
            return;
        }

        // --- Self collision (skip the most recent ~30 points so we don't instant-die) ---
        List<Point> pts = player.trail.points;
        int safeZone = 30;
        for (int i = 0; i < pts.size() - safeZone; i++) {
            Point p = pts.get(i);
            if (Math.abs(p.x - player.x) < 2 && Math.abs(p.y - player.y) < 2) {
                player.alive = false;
                return;
            }
        }

        // --- Win condition checks ---
        switch (mode) {
            case TIME_SURVIVAL  -> checkTimeSurvival();
            case SCORE_DISTANCE -> checkScore();
            case PICKUPS        -> checkPickups();
            case LAPS           -> checkLaps();
            case FILL_BOARD     -> checkFillBoard();
        }
    }

    // ── Win Checks ─────────────────────────────────────────────────────────

    private void checkTimeSurvival() {
        if (elapsedNanos >= SURVIVE_NANOS) {
            won = true;
        }
    }

    private void checkScore() {
        if (player.trail.points.size() >= TARGET_SCORE) {
            won = true;
        }
    }

    private void checkPickups() {
        for (Pickup pk : pickups) {
            if (!pk.collected && pk.isCollectedBy(player.x, player.y)) {
                pk.collected = true;
                pickupsCollected++;
            }
        }
        if (pickupsCollected >= TARGET_PICKUPS) {
            won = true;
        }
    }

    private void checkLaps() {
        Checkpoint cp = checkpoints.get(nextCheckpoint);
        if (cp.contains(player.x, player.y)) {
            nextCheckpoint++;
            if (nextCheckpoint >= checkpoints.size()) {
                nextCheckpoint = 0;
                lapsCompleted++;
                if (lapsCompleted >= TARGET_LAPS) {
                    won = true;
                }
            }
        }
    }

    private void checkFillBoard() {
        double totalCells = (WIDTH / 2.0) * (HEIGHT / 2.0);
        double filled     = player.trail.points.size();
        if (filled / totalCells >= TARGET_FILL_PCT) {
            won = true;
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void spawnPickups() {
        Random rng = new Random();
        int margin = 40;
        while (pickups.size() < TARGET_PICKUPS) {
            double px = margin + rng.nextDouble() * (WIDTH  - margin * 2);
            double py = margin + rng.nextDouble() * (HEIGHT - margin * 2);
            // keep away from player start
            if (Math.abs(px - WIDTH / 2.0) > 60 || Math.abs(py - HEIGHT / 2.0) > 60) {
                pickups.add(new Pickup(px, py));
            }
        }
    }

    private void setupCheckpoints() {
        int w = 60, h = 60;
        // Four corners, visited in order: TL → TR → BR → BL
        checkpoints.add(new Checkpoint(0,  30,          30,          w, h));
        checkpoints.add(new Checkpoint(1,  WIDTH - 90,  30,          w, h));
        checkpoints.add(new Checkpoint(2,  WIDTH - 90,  HEIGHT - 90, w, h));
        checkpoints.add(new Checkpoint(3,  30,          HEIGHT - 90, w, h));
    }

    // ── Helpers for HUD ────────────────────────────────────────────────────

    /** Seconds remaining for TIME_SURVIVAL, or 0 if done. */
    public double secondsRemaining() {
        long remaining = SURVIVE_NANOS - elapsedNanos;
        return Math.max(0, remaining / 1_000_000_000.0);
    }

    /** Seconds elapsed (general purpose). */
    public double secondsElapsed() {
        return elapsedNanos / 1_000_000_000.0;
    }

    /** Fill percentage 0-100 for FILL_BOARD. */
    public double fillPercent() {
        double totalCells = (WIDTH / 2.0) * (HEIGHT / 2.0);
        return Math.min(100.0, player.trail.points.size() / totalCells * 100.0);
    }
}
