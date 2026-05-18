package com.example.network;

import com.example.models.Direction;

import java.util.ArrayList;
import java.util.List;

public class NetworkMessage {

    public static final String HELLO        = "HELLO";
    public static final String WELCOME      = "WELCOME";
    public static final String LOBBY_STATE  = "LOBBY_STATE";
    public static final String SETTINGS     = "SETTINGS";
    public static final String CHAT         = "CHAT";
    public static final String START        = "START";
    public static final String STATE        = "STATE";
    public static final String ROUND_OVER   = "ROUND_OVER";
    public static final String MATCH_OVER   = "MATCH_OVER";
    public static final String INPUT        = "INPUT";
    public static final String DISCONNECT   = "DISCONNECT";
    public static final String PING         = "PING";
    public static final String PONG         = "PONG";
    public static final String DISCOVER_REQ = "TRON_DISCOVER";
    public static final String DISCOVER_RESP = "TRON_HERE";

    public static String make(String type, String... args) {
        if (args == null || args.length == 0) return type;
        return type + "|" + String.join("|", args);
    }

    public static String[] parse(String raw) {
        if (raw == null || raw.isBlank()) return new String[]{""};
        return raw.split("\\|", -1);
    }

    public static class PlayerSnapshot {

        public int     slot;
        public double  x, y;
        public boolean alive;
        public List<double[]> trailPoints = new ArrayList<>();

        public PlayerSnapshot() {}

        public PlayerSnapshot(int slot, double x, double y,
                              boolean alive, List<double[]> trailPoints) {
            this.slot        = slot;
            this.x           = x;
            this.y           = y;
            this.alive       = alive;
            this.trailPoints = trailPoints;
        }

        public String encode() {
            StringBuilder sb = new StringBuilder();
            sb.append(slot).append(':')
              .append(x).append(':')
              .append(y).append(':')
              .append(alive ? '1' : '0').append(':');
            if (trailPoints != null) {
                for (int i = 0; i < trailPoints.size(); i++) {
                    if (i > 0) sb.append('|');
                    double[] pt = trailPoints.get(i);
                    sb.append(pt[0]).append(',').append(pt[1]);
                }
            }
            return sb.toString();
        }

        public static PlayerSnapshot decode(String enc) {
            String[] main = enc.split(":", 5);
            PlayerSnapshot ps = new PlayerSnapshot();
            ps.slot  = Integer.parseInt(main[0]);
            ps.x     = Double.parseDouble(main[1]);
            ps.y     = Double.parseDouble(main[2]);
            ps.alive = "1".equals(main[3]);
            ps.trailPoints = new ArrayList<>();
            if (main.length > 4 && !main[4].isEmpty()) {
                for (String pair : main[4].split("\\|")) {
                    String[] xy = pair.split(",", 2);
                    if (xy.length == 2) {
                        try {
                            ps.trailPoints.add(new double[]{
                                Double.parseDouble(xy[0]),
                                Double.parseDouble(xy[1])
                            });
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            return ps;
        }
    }
}