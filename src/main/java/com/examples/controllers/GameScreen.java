package com.example.controllers;

import com.example.Main;
import com.example.models.Player;
import com.example.models.Point;
import com.example.services.GameSession;
import com.example.utils.ConfigManager;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Task 3 — UI / rendering only.
 *
 * Drives the AnimationTimer, delegates all logic to GameSession,
 * and paints what the session exposes. No game logic lives here.
 */
public class GameScreen {

    private final Main        app;
    private final GameSession session;

    private Canvas          canvas;
    private GraphicsContext gc;
    private AnimationTimer  loop;

    /** Start a brand-new game session. */
    public GameScreen(Main app) {
        this.app     = app;
        this.session = new GameSession();
    }

    /** Reuse an existing session (for Retry — session.restart() resets it). */
    public GameScreen(Main app, GameSession existingSession) {
        this.app     = app;
        this.session = existingSession;
    }

    public Pane getView() {
        canvas = new Canvas(ConfigManager.gridWidth, ConfigManager.gridHeight);
        gc     = canvas.getGraphicsContext2D();

        session.start();

        Pane root = new Pane(canvas);

        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean alive = session.tick();

                render();

                if (!alive) {
                    stop();
                    app.showGameOver("You Crashed!", session);
                }
            }
        };

        loop.start();
        return root;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RENDERING — reads game state only, never mutates it
    // ─────────────────────────────────────────────────────────────────────────

    private void render() {
        // Background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, ConfigManager.gridWidth, ConfigManager.gridHeight);

        // Grid border
        gc.setStroke(Color.web("#1a1a2e"));
        gc.strokeRect(0, 0, ConfigManager.gridWidth, ConfigManager.gridHeight);

        // Each player: trail + head
        for (int i = 0; i < session.game.players.size(); i++) {
            Player p = session.game.players.get(i);
            Color color = ConfigManager.PLAYER_COLORS[i];

            // Trail
            gc.setFill(color.deriveColor(0, 1, 0.65, 0.85));
            for (Point pt : p.trail.points) {
                gc.fillRect(pt.x - 1, pt.y - 1, 4, 4);
            }

            // Head (brighter, slightly larger)
            if (p.alive) {
                gc.setFill(color);
                gc.fillOval(p.x - 3, p.y - 3, 8, 8);

                // Glow ring
                gc.setStroke(color.deriveColor(0, 1, 1.5, 0.3));
                gc.setLineWidth(2);
                gc.strokeOval(p.x - 5, p.y - 5, 12, 12);
            }
        }
    }
}
