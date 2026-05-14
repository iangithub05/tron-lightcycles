package com.example.models;

import java.util.ArrayList;
import java.util.List;

public class Trail {

    public List<Point> points = new ArrayList<>();

    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
    }

    public boolean contains(double x, double y, double tolerance) {
        for (Point p : points) {
            if (Math.abs(p.x - x) < tolerance && Math.abs(p.y - y) < tolerance) return true;
        }
        return false;
    }

    public void clear() {
        points.clear();
    }
}
