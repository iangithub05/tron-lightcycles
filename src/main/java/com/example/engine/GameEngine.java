package com.example.engine;

import com.example.Main;
import com.example.models.Direction;
import com.example.models.Player;
import com.example.models.Point;
import com.example.services.GameSession;
import com.example.utils.InputManager;
import com.example.utils.KeyBindings;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GameEngine {

    private static final Color[] PLAYER_COLORS = {
        Color.CYAN,
        Color.LIMEGREEN,
        Color.ORANGERED,
        Color.MEDIUMPURPLE
    };

    private final Main app;
    private final GameSession session;

    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer loop;

    private long lastTick = 0;
    private static final long TICK_NS = 16_000_000;

    public GameEngine(Main app, GameSession session) {
        this.app = app;
        this.session = session;
    }

    public Pane buildCanvas() {
        canvas = new Canvas(session.rules.gridWidth, session.rules.gridHeight);
        gc = canvas.getGraphicsContext2D();
        return new Pane(canvas);
    }

    public void start() {
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastTick < TICK_NS) return;
                lastTick = now;

                readInput();

                boolean running = session.tick();
                render();

                if (!running) {
                    stop();
                    Player human = session.game.players.get(0);
                    Player winner = session.game.getWinner();
                    String result;
                    if (!human.alive) {
                        result = "YOU LOSE";
                    } else if (winner == human) {
                        result = "YOU WIN";
                    } else {
                        result = "DRAW";
                    }
                    app.showGameOver(result, session);
                }
            }
        };
        loop.start();
    }

    public void stop() {
        if (loop != null) loop.stop();
    }

    private void readInput() {
        Player p = session.game.players.get(0);
        if (InputManager.isDown(KeyBindings.UP)) p.setDirection(Direction.UP);
        if (InputManager.isDown(KeyBindings.DOWN)) p.setDirection(Direction.DOWN);
        if (InputManager.isDown(KeyBindings.LEFT)) p.setDirection(Direction.LEFT);
        if (InputManager.isDown(KeyBindings.RIGHT)) p.setDirection(Direction.RIGHT);
    }

    private void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, session.rules.gridWidth, session.rules.gridHeight);

        gc.setStroke(Color.web("#1a1a2e"));
        gc.setLineWidth(1);
        gc.strokeRect(0, 0, session.rules.gridWidth, session.rules.gridHeight);

        for (int i = 0; i < session.game.players.size(); i++) {
            Player p = session.game.players.get(i);
            Color color = PLAYER_COLORS[i % PLAYER_COLORS.length];

            gc.setFill(color.deriveColor(0, 1, 0.6, 0.8));
            for (Point pt : p.trail.points) {
                gc.fillRect(pt.x - 1, pt.y - 1, 4, 4);
            }

            if (p.alive) {
                gc.setFill(color);
                gc.fillOval(p.x - 3, p.y - 3, 8, 8);

                gc.setStroke(color.deriveColor(0, 1, 1.4, 0.3));
                gc.setLineWidth(2);
                gc.strokeOval(p.x - 5, p.y - 5, 12, 12);
            }
        }
    }
}
