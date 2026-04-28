package com.example;

import com.example.input.InputManager;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class GameScreen {

    private static final int W = Game.WIDTH;
    private static final int H = Game.HEIGHT;

    private final Main     app;
    private final GameMode mode;
    private Game           game;
    private Canvas         canvas;
    private GraphicsContext gc;
    private AnimationTimer loop;

    // Neon colours
    private static final Color COL_BG         = Color.web("#0a0a1a");
    private static final Color COL_GRID        = Color.web("#1a1a3a");
    private static final Color COL_TRAIL       = Color.web("#00f0ff");
    private static final Color COL_PLAYER      = Color.web("#ffffff");
    private static final Color COL_PICKUP      = Color.web("#ffcc00");
    private static final Color COL_PICKUP_GLOW = Color.web("#ffcc0044");
    private static final Color COL_CP_NEXT     = Color.web("#00ff8844");
    private static final Color COL_CP_DONE     = Color.web("#ffffff22");
    private static final Color COL_HUD         = Color.web("#00f0ff");
    private static final Color COL_HUD_WARN    = Color.web("#ff4444");

    public GameScreen(Main app, GameMode mode) {
        this.app  = app;
        this.mode = mode;
    }

    public Pane getView() {
        Pane root = new Pane();
        canvas = new Canvas(W, H);
        gc     = canvas.getGraphicsContext2D();
        game   = new Game(mode);

        root.getChildren().add(canvas);

        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                game.update(now);

                render();

                if (game.won) {
                    stop();
                    InputManager.clear();
                    app.showGameOver(true, mode, buildWinMessage());
                } else if (!game.player.alive) {
                    stop();
                    InputManager.clear();
                    app.showGameOver(false, mode, buildLoseMessage());
                }
            }
        };

        loop.start();
        return root;
    }

    // ── Rendering ──────────────────────────────────────────────────────────

    private void render() {
        // Background
        gc.setFill(COL_BG);
        gc.fillRect(0, 0, W, H);

        drawGrid();

        // Mode-specific decorations
        if (mode == GameMode.LAPS)    drawCheckpoints();
        if (mode == GameMode.PICKUPS) drawPickups();

        // Trail
        gc.setFill(COL_TRAIL);
        for (Point p : game.player.trail.points) {
            gc.fillRect(p.x - 1, p.y - 1, 3, 3);
        }

        // Player head (bright dot with glow)
        gc.setFill(Color.web("#00f0ff88"));
        gc.fillOval(game.player.x - 6, game.player.y - 6, 12, 12);
        gc.setFill(COL_PLAYER);
        gc.fillOval(game.player.x - 3, game.player.y - 3, 6, 6);

        drawHUD();
    }

    private void drawGrid() {
        gc.setStroke(COL_GRID);
        gc.setLineWidth(0.5);
        for (int x = 0; x <= W; x += 40) {
            gc.strokeLine(x, 0, x, H);
        }
        for (int y = 0; y <= H; y += 40) {
            gc.strokeLine(0, y, W, y);
        }
    }

    private void drawCheckpoints() {
        for (Checkpoint cp : game.checkpoints) {
            boolean isNext = cp.index == game.nextCheckpoint;
            gc.setFill(isNext ? COL_CP_NEXT : COL_CP_DONE);
            gc.fillRoundRect(cp.x, cp.y, cp.width, cp.height, 8, 8);
            gc.setStroke(isNext ? Color.web("#00ff88") : Color.web("#ffffff44"));
            gc.setLineWidth(isNext ? 2 : 1);
            gc.strokeRoundRect(cp.x, cp.y, cp.width, cp.height, 8, 8);

            gc.setFill(isNext ? Color.web("#00ff88") : Color.web("#ffffff88"));
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(cp.index + 1),
                        cp.x + cp.width / 2,
                        cp.y + cp.height / 2 + 5);
        }
    }

    private void drawPickups() {
        for (Pickup pk : game.pickups) {
            if (pk.collected) continue;
            // Glow
            gc.setFill(COL_PICKUP_GLOW);
            gc.fillOval(pk.x - Pickup.RADIUS * 2, pk.y - Pickup.RADIUS * 2,
                        Pickup.RADIUS * 4, Pickup.RADIUS * 4);
            // Core
            gc.setFill(COL_PICKUP);
            gc.fillOval(pk.x - Pickup.RADIUS, pk.y - Pickup.RADIUS,
                        Pickup.RADIUS * 2, Pickup.RADIUS * 2);
        }
    }

    private void drawHUD() {
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.LEFT);

        switch (mode) {
            case TIME_SURVIVAL -> {
                double rem = game.secondsRemaining();
                gc.setFill(rem < 10 ? COL_HUD_WARN : COL_HUD);
                gc.fillText(String.format("TIME: %.1fs", rem), 10, 20);
            }
            case SCORE_DISTANCE -> {
                int score = game.player.trail.points.size();
                gc.setFill(COL_HUD);
                gc.fillText(String.format("TRAIL: %d / %d", score, Game.TARGET_SCORE), 10, 20);
                // Progress bar
                double pct = Math.min(1.0, score / (double) Game.TARGET_SCORE);
                gc.setFill(Color.web("#ffffff22"));
                gc.fillRect(10, 28, 200, 6);
                gc.setFill(COL_TRAIL);
                gc.fillRect(10, 28, 200 * pct, 6);
            }
            case PICKUPS -> {
                gc.setFill(COL_HUD);
                gc.fillText(String.format("PICKUPS: %d / %d",
                        game.pickupsCollected, Game.TARGET_PICKUPS), 10, 20);
            }
            case LAPS -> {
                gc.setFill(COL_HUD);
                gc.fillText(String.format("LAPS: %d / %d  |  NEXT CP: %d",
                        game.lapsCompleted, Game.TARGET_LAPS,
                        game.nextCheckpoint + 1), 10, 20);
            }
            case FILL_BOARD -> {
                gc.setFill(COL_HUD);
                gc.fillText(String.format("FILL: %.1f%% / %.0f%%",
                        game.fillPercent(), Game.TARGET_FILL_PCT * 100), 10, 20);
                double pct = Math.min(1.0, game.fillPercent() / (Game.TARGET_FILL_PCT * 100));
                gc.setFill(Color.web("#ffffff22"));
                gc.fillRect(10, 28, 200, 6);
                gc.setFill(COL_TRAIL);
                gc.fillRect(10, 28, 200 * pct, 6);
            }
        }

        // Mode label top-right
        gc.setFill(Color.web("#ffffff55"));
        gc.setFont(Font.font("Monospace", 11));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(modeLabel(), W - 10, 20);

        // Controls reminder bottom-left
        gc.setFill(Color.web("#ffffff33"));
        gc.setFont(Font.font("Monospace", 10));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("WASD / Arrow Keys", 10, H - 10);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private String modeLabel() {
        return switch (mode) {
            case TIME_SURVIVAL  -> "MODE: TIME SURVIVAL";
            case SCORE_DISTANCE -> "MODE: SCORE CHASE";
            case PICKUPS        -> "MODE: PICKUP RUSH";
            case LAPS           -> "MODE: LAP RACE";
            case FILL_BOARD     -> "MODE: FILL THE BOARD";
        };
    }

    private String buildWinMessage() {
        return switch (mode) {
            case TIME_SURVIVAL  -> String.format("You survived 60 seconds!\nTime: %.1fs", game.secondsElapsed());
            case SCORE_DISTANCE -> String.format("Trail target reached!\nFinal trail: %d pts", game.player.trail.points.size());
            case PICKUPS        -> String.format("All %d pickups collected!\nTime: %.1fs", Game.TARGET_PICKUPS, game.secondsElapsed());
            case LAPS           -> String.format("%d laps completed!\nTime: %.1fs", Game.TARGET_LAPS, game.secondsElapsed());
            case FILL_BOARD     -> String.format("Board filled to %.1f%%!\nTime: %.1fs", game.fillPercent(), game.secondsElapsed());
        };
    }

    private String buildLoseMessage() {
        return switch (mode) {
            case TIME_SURVIVAL  -> String.format("You crashed!\nSurvived %.1fs / 60s", game.secondsElapsed());
            case SCORE_DISTANCE -> String.format("You crashed!\nTrail: %d / %d pts", game.player.trail.points.size(), Game.TARGET_SCORE);
            case PICKUPS        -> String.format("You crashed!\nPickups: %d / %d", game.pickupsCollected, Game.TARGET_PICKUPS);
            case LAPS           -> String.format("You crashed!\nLaps: %d / %d", game.lapsCompleted, Game.TARGET_LAPS);
            case FILL_BOARD     -> String.format("You crashed!\nFilled: %.1f%% / %.0f%%", game.fillPercent(), Game.TARGET_FILL_PCT * 100);
        };
    }
}
