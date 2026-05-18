package com.example.network;

import java.util.List;

public final class NetworkMessage {

    public static final String HELLO       = "HELLO";
    public static final String WELCOME     = "WELCOME";
    public static final String LOBBY_STATE = "LOBBY_STATE";
    public static final String CHAT        = "CHAT";
    public static final String SETTINGS    = "SETTINGS";
    public static final String START       = "START";
    public static final String INPUT       = "INPUT";
    public static final String STATE       = "STATE";
    public static final String ROUND_OVER  = "ROUND_OVER";
    public static final String MATCH_OVER  = "MATCH_OVER";
    public static final String DISCONNECT  = "DISCONNECT";
    public static final String PING        = "PING";
    public static final String PONG        = "PONG";

    public static final String DISCOVER_REQ  = "TRON_DISCOVER_REQ";
    public static final String DISCOVER_RESP = "TRON_DISCOVER_RESP";

    public static String make(String type, String... parts) {
        if (parts.length == 0) return type;
        return type + "|" + String.join("|", parts);
    }

    public static String[] parse(String raw) {
        return raw.split("\\|", -1);
    }


    public static class PlayerSnapshot {
        public int    slot;
        public double x, y;
        public boolean alive;
        public List<double[]> trailPoints;

        public String encode() {
            StringBuilder sb = new StringBuilder();
            sb.append(slot).append(':').append((int) x).append(':').append((int) y)
              .append(':').append(alive ? '1' : '0').append(':');
            if (trailPoints != null) {
                for (int i = 0; i < trailPoints.size(); i++) {
                    if (i > 0) sb.append('~');
                    double[] p = trailPoints.get(i);
                    sb.append((int) p[0]).append(',').append((int) p[1]);
                }
            }
            return sb.toString();
        }

        public static PlayerSnapshot decode(String s) {
            PlayerSnapshot ps = new PlayerSnapshot();
            String[] parts = s.split(":", -1);
            ps.slot  = Integer.parseInt(parts[0]);
            ps.x     = Double.parseDouble(parts[1]);
            ps.y     = Double.parseDouble(parts[2]);
            ps.alive = "1".equals(parts[3]);
            ps.trailPoints = new java.util.ArrayList<>();
            if (parts.length > 4 && !parts[4].isEmpty()) {
                for (String pt : parts[4].split("~")) {
                    String[] xy = pt.split(",");
                    ps.trailPoints.add(new double[]{
                        Double.parseDouble(xy[0]), Double.parseDouble(xy[1])
                    });
                }
            }
            return ps;
        }
    }
}
