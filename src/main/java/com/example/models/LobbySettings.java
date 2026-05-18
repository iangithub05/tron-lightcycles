package com.example.models;

public class LobbySettings {

    public int maxPlayers = 2;
    public int gamesToWin = 3;
    public int timeLimitSecs = 120;
    public Difficulty difficulty = Difficulty.MEDIUM;
    public int gridWidth  = 1280;
    public int gridHeight = 720;

    public String encode() {
        return maxPlayers + "," + gamesToWin + "," + timeLimitSecs + ","
                + difficulty.name() + "," + gridWidth + "," + gridHeight;
    }

    public static LobbySettings decode(String s) {
        String[] p = s.split(",");
        LobbySettings ls = new LobbySettings();
        ls.maxPlayers    = Integer.parseInt(p[0]);
        ls.gamesToWin    = Integer.parseInt(p[1]);
        ls.timeLimitSecs = Integer.parseInt(p[2]);
        ls.difficulty    = Difficulty.valueOf(p[3]);
        ls.gridWidth     = Integer.parseInt(p[4]);
        ls.gridHeight    = Integer.parseInt(p[5]);
        return ls;
    }

    public GameRules toGameRules() {
        double speed = switch (difficulty) {
            case EASY   -> 1.5;
            case MEDIUM -> 2.5;
            case HARD   -> 4.0;
        };
        GameRules r = new GameRules(gridWidth, gridHeight, speed);
        return r;
    }
}
