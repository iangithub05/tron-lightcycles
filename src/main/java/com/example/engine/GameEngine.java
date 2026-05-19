package com.example.engine;

import com.example.Main;
import com.example.models.Direction;
import com.example.models.Player;
import com.example.services.GameSession;
import com.example.utils.InputManager;
import com.example.utils.KeyBindings;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;

public class GameEngine {

    private final Main app;
    private final GameSession session;

    private GameRenderer renderer;
    private AnimationTimer loop;

    private long lastTick = 0;
    private static final long TICK_NS = 16_666_667; // fixed 60 UPS cap

    public GameEngine(Main app, GameSession session) {
        this.app = app;
        this.session = session;
    }

    public Pane buildCanvas() {
        renderer = new GameRenderer(session.rules.gridWidth, session.rules.gridHeight);
        renderer.resetTrailCounters(session.game.players.size());
        return renderer.getView();
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
        if (InputManager.isDown(KeyBindings.UP))    p.setDirection(Direction.UP);
        if (InputManager.isDown(KeyBindings.DOWN))  p.setDirection(Direction.DOWN);
        if (InputManager.isDown(KeyBindings.LEFT))  p.setDirection(Direction.LEFT);
        if (InputManager.isDown(KeyBindings.RIGHT)) p.setDirection(Direction.RIGHT);
    }

    private void render() {
        renderer.renderPlayers(session.game.players);
    }
}
