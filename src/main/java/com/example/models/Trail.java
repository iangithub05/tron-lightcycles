package com.example.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Trail {

    public List<Point> points = new ArrayList<>();
    private final Set<Long> grid = new HashSet<>();
    private static final int CELL = 4;

    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
        grid.add(key((int)(x / CELL), (int)(y / CELL)));
    }

    public void clear() {
        points.clear();
        grid.clear();
    }

    public boolean contains(double x, double y, double tolerance) {
        return containsExcludingTail(x, y, tolerance, 0);
    }

    public boolean containsExcludingTail(double x, double y, double tolerance, int skipTail) {
        int limit = points.size() - skipTail;
        if (limit <= 0) return false;

        int cx = (int)(x / CELL);
        int cy = (int)(y / CELL);
        boolean nearAny = false;
        outer:
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (grid.contains(key(cx + dx, cy + dy))) { nearAny = true; break outer; }
            }
        }
        if (!nearAny) return false;

        double tSq = tolerance * tolerance;
        for (int i = 0; i < limit; i++) {
            Point p = points.get(i);
            double ddx = p.x - x;
            double ddy = p.y - y;
            if (ddx * ddx + ddy * ddy <= tSq) return true;
        }
        return false;
    }

    private static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }
}