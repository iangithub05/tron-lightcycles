package com.example.engine;

import com.example.models.Player;
import com.example.models.Point;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

import java.util.List;

/**
 * Shared lightweight renderer for Tron-style gameplay.
 *
 * Layer 1: trailCanvas  - persistent background + permanent trails.
 * Layer 2: actorCanvas  - cleared every frame, only moving heads/overlays.
 *
 * This avoids redrawing every old trail point every frame. Both singleplayer
 * and multiplayer can use the same rendering pattern.
 */
public class GameRenderer {

    private static final Color[] PLAYER_COLORS = {
        Color.CYAN,
        Color.LIMEGREEN,
        Color.ORANGERED,
        Color.MEDIUMPURPLE
    };

    private final int width;
    private final int height;
    private final Canvas trailCanvas;
    private final Canvas actorCanvas;
    private final GraphicsContext trailGc;
    private final GraphicsContext actorGc;
    private final StackPane view;
    private int[] lastTrailCount = new int[0];

    public GameRenderer(int width, int height) {
        this.width = width;
        this.height = height;

        trailCanvas = new Canvas(width, height);
        actorCanvas = new Canvas(width, height);
        trailGc = trailCanvas.getGraphicsContext2D();
        actorGc = actorCanvas.getGraphicsContext2D();

        trailGc.setImageSmoothing(false);
        actorGc.setImageSmoothing(false);

        view = new StackPane(trailCanvas, actorCanvas);
        view.setAlignment(Pos.CENTER);
        view.setPrefSize(width, height);
        view.setMinSize(width, height);
        view.setMaxSize(width, height);
        view.setStyle("-fx-background-color: black;");

        clearAll();
    }

    public Pane getView() {
        return view;
    }

    public GraphicsContext getActorGc() {
        return actorGc;
    }

    public GraphicsContext getTrailGc() {
        return trailGc;
    }

    public void resetTrailCounters(int playerCount) {
        lastTrailCount = new int[Math.max(0, playerCount)];
    }

    public void clearAll() {
        trailGc.setFill(Color.BLACK);
        trailGc.fillRect(0, 0, width, height);
        clearActors();
        for (int i = 0; i < lastTrailCount.length; i++) lastTrailCount[i] = 0;
    }

    public void clearActors() {
        actorGc.clearRect(0, 0, width, height);
    }

    public void renderPlayers(List<Player> players) {
        ensureTrailCounterSize(players.size());
        drawNewTrailSegments(players);
        clearActors();
        drawHeads(players);
    }

    private void ensureTrailCounterSize(int playerCount) {
        if (lastTrailCount.length == playerCount) return;
        resetTrailCounters(playerCount);
        clearAll();
    }

    private void drawNewTrailSegments(List<Player> players) {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            List<Point> pts = p.trail.points;
            int from = Math.min(lastTrailCount[i], pts.size());
            if (pts.size() <= from) continue;

            Color color = colorFor(i);
            trailGc.setStroke(color.deriveColor(0, 1, 0.78, 0.92));
            trailGc.setLineWidth(5);
            trailGc.setLineCap(StrokeLineCap.ROUND);

            if (from == 0 && pts.size() == 1) {
                Point only = pts.get(0);
                trailGc.setFill(color.deriveColor(0, 1, 0.78, 0.92));
                trailGc.fillOval(only.x - 2.5, only.y - 2.5, 5, 5);
            }

            int start = Math.max(1, from);
            for (int j = start; j < pts.size(); j++) {
                Point a = pts.get(j - 1);
                Point b = pts.get(j);
                if (distanceSquared(a.x, a.y, b.x, b.y) < 90_000) {
                    trailGc.strokeLine(a.x, a.y, b.x, b.y);
                }
            }
            lastTrailCount[i] = pts.size();
        }
    }

    private void drawHeads(List<Player> players) {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (!p.alive) continue;
            drawHead(p.x, p.y, colorFor(i));
        }
    }

    public void drawHead(double x, double y, Color color) {
        actorGc.setFill(color);
        actorGc.fillOval(x - 4, y - 4, 10, 10);
        actorGc.setStroke(color.deriveColor(0, 1, 1.35, 0.34));
        actorGc.setLineWidth(2);
        actorGc.strokeOval(x - 6, y - 6, 14, 14);
    }

    public static Color colorFor(int slot) {
        return PLAYER_COLORS[Math.floorMod(slot, PLAYER_COLORS.length)];
    }

    private static double distanceSquared(double ax, double ay, double bx, double by) {
        double dx = ax - bx;
        double dy = ay - by;
        return dx * dx + dy * dy;
    }
}
