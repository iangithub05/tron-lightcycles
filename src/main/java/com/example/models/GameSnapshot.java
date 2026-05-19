package com.example.models;

import java.util.ArrayList;
import java.util.List;

public class GameSnapshot {

    public enum Phase {
        WAITING,
        PLAYING,
        ROUND_OVER,
        MATCH_OVER
    }

    public Phase phase = Phase.WAITING;
    public int roundWinnerSlot = -1;
    public int matchWinnerSlot = -1;
    public int countdownSeconds = 0;
    public int gamesToWin = 1;
    public List<PlayerSnapshot> players = new ArrayList<>();

    public GameSnapshot() {}

    public GameSnapshot copyWithoutTrail() {
        GameSnapshot copy = new GameSnapshot();
        copy.phase = phase;
        copy.roundWinnerSlot = roundWinnerSlot;
        copy.matchWinnerSlot = matchWinnerSlot;
        copy.countdownSeconds = countdownSeconds;
        copy.gamesToWin = gamesToWin;
        for (PlayerSnapshot p : players) {
            copy.players.add(p.copyWithoutTrail());
        }
        return copy;
    }
}
