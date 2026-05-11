package com.example.models;

import java.util.ArrayList;
import java.util.List;

// Stores every position a player has passed through.
// Used for both rendering the light trail and collision detection.
public class Trail {

    public List<Point> points = new ArrayList<>();

    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
    }

    // Returns true if the given position is within 'tolerance' pixels of any trail point.
    // Tolerance should match playerSpeed so fast-moving players don't skip through trails.
    public boolean contains(double x, double y, double tolerance) {
        for (Point p : points) {
            if (Math.abs(p.x - x) < tolerance && Math.abs(p.y - y) < tolerance) return true;
        }
        return false;
    }
}
