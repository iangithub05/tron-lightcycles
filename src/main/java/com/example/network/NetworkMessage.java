package com.example.network;

import com.example.models.GameSnapshot;
import com.example.models.PlayerSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NetworkMessage {

    private static final String TRAIL_POINT_SEPARATOR = "~";

    public static final String HELLO = "HELLO";
    public static final String WELCOME = "WELCOME";
    public static final String LOBBY_STATE = "LOBBY_STATE";
    public static final String SETTINGS = "SETTINGS";
    public static final String CHAT = "CHAT";
    public static final String START = "START";
    public static final String STATE = "STATE";
    public static final String ROUND_OVER = "ROUND_OVER";
    public static final String MATCH_OVER = "MATCH_OVER";
    public static final String INPUT = "INPUT";
    public static final String DISCONNECT = "DISCONNECT";
    public static final String PING = "PING";
    public static final String PONG = "PONG";
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

    public static String encodeSnapshot(GameSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < snapshot.players.size(); i++) {
            if (i > 0) sb.append(';');
            sb.append(encodePlayerSnapshot(snapshot.players.get(i)));
        }
        return sb.toString();
    }

    public static List<PlayerSnapshot> decodeSnapshots(String encoded) {
        List<PlayerSnapshot> snapshots = new ArrayList<>();
        if (encoded == null || encoded.isBlank()) return snapshots;
        for (String enc : encoded.split(";")) {
            if (!enc.isEmpty()) snapshots.add(decodePlayerSnapshot(enc));
        }
        return snapshots;
    }

    public static String encodePlayerSnapshot(PlayerSnapshot ps) {
        StringBuilder sb = new StringBuilder();
        sb.append(ps.slot).append(':')
          .append(ps.x).append(':')
          .append(ps.y).append(':')
          .append(ps.alive ? '1' : '0').append(':')
          .append(ps.score).append(':')
          .append(ps.name == null ? "" : ps.name.replace(":", "").replace(";", "")).append(':');

        if (ps.trailPoints != null) {
            for (int i = 0; i < ps.trailPoints.size(); i++) {
                if (i > 0) sb.append(TRAIL_POINT_SEPARATOR);
                double[] pt = ps.trailPoints.get(i);
                // Coordinates are sent as rounded pixels instead of long double strings.
                // This shrinks packets heavily and reduces parsing work on clients.
                sb.append(Math.round(pt[0])).append(',').append(Math.round(pt[1]));
            }
        }
        return sb.toString();
    }

    public static PlayerSnapshot decodePlayerSnapshot(String enc) {
        String[] main = enc.split(":", 7);
        PlayerSnapshot ps = new PlayerSnapshot();
        ps.slot = Integer.parseInt(main[0]);
        ps.x = Double.parseDouble(main[1]);
        ps.y = Double.parseDouble(main[2]);
        ps.alive = "1".equals(main[3]);
        ps.score = main.length > 4 && !main[4].isEmpty() ? Integer.parseInt(main[4]) : 0;
        ps.name = main.length > 5 ? main[5] : "P" + (ps.slot + 1);
        ps.trailPoints = new ArrayList<>();

        if (main.length > 6 && !main[6].isEmpty()) {
            for (String pair : main[6].split(TRAIL_POINT_SEPARATOR)) {
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
