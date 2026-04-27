package com.example;

import java.util.ArrayList;
import java.util.List;

public class Trail {

    public List<Point> points = new ArrayList<>();

    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
    }

    public boolean contains(double x, double y) {
        for (Point p : points) {
            if (Math.abs(p.x - x) < 2 && Math.abs(p.y - y) < 2) {
                return true;
            }
        }
        return false;
    }
}