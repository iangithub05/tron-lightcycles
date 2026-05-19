package com.example.engine;

import com.example.Main;
import com.example.models.Direction;
import com.example.models.Player;
import com.example.models.Point;
import com.example.services.GameSession;
import com.example.utils.InputManager;
import com.example.utils.KeyBindings;
import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;

import java.util.List;

public class GameEngine {

    private static final Color[] PLAYER_COLORS = {
        Color.CYAN,
        Color.LIMEGREEN,
        Color.ORANGERED,
        Color.MEDIUMPURPLE
    };

    private final Main app;
    private final GameSession session;

    private Canvas trailCanvas;
    private Canvas canvas;
    private GraphicsContext trailGc;
    private GraphicsContext gc;
    private AnimationTimer loop;
    private int[] lastTrailCount;

    private long lastTick = 0;
    private static final long TICK_NS = 16_000_000;

    public GameEngine(Main app, GameSession session) {
        this.app = app;
        this.session = session;
    }

    public Pane buildCanvas() {
        int gw = session.rules.gridWidth;
        int gh = session.rules.gridHeight;

        // Two-canvas approach: persistent trails + dynamic heads (same as multiplayer)
        trailCanvas = new Canvas(gw, gh);
        canvas      = new Canvas(gw, gh);
        trailGc     = trailCanvas.getGraphicsContext2D();
        gc          = canvas.getGraphicsContext2D();

        trailGc.setFill(Color.BLACK);
        trailGc.fillRect(0, 0, gw, gh);

        lastTrailCount = new int[session.game.players.size()];

        // Scale both canvases to fill the actual monitor resolution
        Rectangle2D screen = Screen.getPrimary().getBounds();
        double scaleX = screen.getWidth()  / gw;
        double scaleY = screen.getHeight() / gh;

        Group group = new Group(trailCanvas, canvas);
        group.getTransforms().add(new Scale(scaleX, scaleY, 0, 0));

        Pane pane = new Pane(group);
        pane.setPrefSize(screen.getWidth(), screen.getHeight());
        pane.setStyle("-fx-background-color: black;");
        return pane;
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
                    Player human  = session.game.players.get(0);
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
        int gw = session.rules.gridWidth;
        int gh = session.rules.gridHeight;
        List<Player> players = session.game.players;

        // Incrementally draw only new trail segments as smooth lines
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            List<Point> pts = p.trail.points;
            int from = lastTrailCount[i];
            if (pts.size() > from) {
                Color color = PLAYER_COLORS[i % PLAYER_COLORS.length];
                trailGc.setStroke(color.deriveColor(0, 1, 0.75, 0.9));
                trailGc.setLineWidth(3);
                trailGc.setLineCap(StrokeLineCap.ROUND);
                int start = Math.max(1, from);
                for (int j = start; j < pts.size(); j++) {
                    Point a = pts.get(j - 1);
                    Point b = pts.get(j);
                    trailGc.strokeLine(a.x, a.y, b.x, b.y);
                }
                lastTrailCount[i] = pts.size();
            }
        }

        // Clear dynamic canvas each frame, then draw player heads
        gc.clearRect(0, 0, gw, gh);
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.alive) {
                Color color = PLAYER_COLORS[i % PLAYER_COLORS.length];
                gc.setFill(color);
                gc.fillOval(p.x - 4, p.y - 4, 10, 10);
                gc.setStroke(color.deriveColor(0, 1, 1.4, 0.3));
                gc.setLineWidth(2);
                gc.strokeOval(p.x - 6, p.y - 6, 14, 14);
            }
        }
    }
}
