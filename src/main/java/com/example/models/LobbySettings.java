package com.example.models;

public class LobbySettings {

    public int maxPlayers = 2;
    public int gamesToWin = 3;
    public int timeLimitSecs = 120;
    public double speed = 2.5;
    public int gridWidth = 1280;
    public int gridHeight = 720;

    public String encode() {
        return maxPlayers + "," + gamesToWin + "," + timeLimitSecs + ","
                + speed + "," + gridWidth + "," + gridHeight;
    }

    public static LobbySettings decode(String s) {
        String[] p = s.split(",");
        LobbySettings ls = new LobbySettings();
        ls.maxPlayers = Integer.parseInt(p[0]);
        ls.gamesToWin = Integer.parseInt(p[1]);
        ls.timeLimitSecs = Integer.parseInt(p[2]);
        ls.speed = Double.parseDouble(p[3]);
        ls.gridWidth = Integer.parseInt(p[4]);
        ls.gridHeight = Integer.parseInt(p[5]);
        return ls;
    }

    public GameRules toGameRules() {
        return new GameRules(gridWidth, gridHeight, speed);
    }
}