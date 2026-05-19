package com.example.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trail {

    public List<Point> points = new ArrayList<>();

    private final Map<Long, Integer> gridCounts = new HashMap<>();
    private final List<Long> pointKeys = new ArrayList<>();
    private static final int CELL = 4;

    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
        long k = key((int)(x / CELL), (int)(y / CELL));
        pointKeys.add(k);
        gridCounts.put(k, gridCounts.getOrDefault(k, 0) + 1);
    }

    public void clear() {
        points.clear();
        pointKeys.clear();
        gridCounts.clear();
    }

    public boolean contains(double x, double y, double tolerance) {
        return containsExcludingTail(x, y, tolerance, 0);
    }

    public boolean containsExcludingTail(double x, double y, double tolerance, int skipTail) {
        int limit = points.size() - skipTail;
        if (limit <= 0) return false;

        int cx = (int)(x / CELL);
        int cy = (int)(y / CELL);
        int radius = Math.max(1, (int)Math.ceil(tolerance / CELL));

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                long k = key(cx + dx, cy + dy);
                if (countExcludingRecent(k, skipTail) > 0) return true;
            }
        }
        return false;
    }

    private int countExcludingRecent(long k, int skipTail) {
        int count = gridCounts.getOrDefault(k, 0);
        if (count == 0 || skipTail <= 0) return count;

        int from = Math.max(0, pointKeys.size() - skipTail);
        for (int i = from; i < pointKeys.size(); i++) {
            if (pointKeys.get(i) == k) count--;
        }
        return count;
    }

    private static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }
}
