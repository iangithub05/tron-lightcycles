package com.example;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

public class GameScreen {

    private final Main app;
    private Game game;

    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer loop;

    public GameScreen(Main app) {
        this.app = app;
    }

    public Pane getView() {
        Pane root = new Pane();

        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();

        game = new Game();

        root.getChildren().add(canvas);

        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                game.update();

                if (!game.player.alive) {
                    stop();
                    app.showGameOver("You Crashed!");
                }

                render();
            }
        };

        loop.start();

        return root;
    }

    private void render() {
        gc.clearRect(0, 0, 800, 600);

        for (Point p : game.player.trail.points) {
            gc.fillRect(p.x, p.y, 2, 2);
        }

        gc.fillOval(game.player.x, game.player.y, 5, 5);
    }
}