package com.example.network;

import java.util.List;

public class NetworkMessage {

    public static class PlayerSnapshot {
        public double x;
        public double y;
        public boolean alive;
        public List<double[]> trailPoints;
    }
}
