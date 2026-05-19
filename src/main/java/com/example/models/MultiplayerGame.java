package com.example.models;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerGame {

    private static final int SELF_COLLISION_SAFE_SKIP = 20;

    private final LobbySettings settings;
    private final GameRules rules;
    private final List<String> playerNames;
    private final int[] matchScores;
    private final int[] trailSentCount;

    private final List<Player> players = new ArrayList<>();
    private GameSnapshot.Phase phase = GameSnapshot.Phase.WAITING;
    private int roundWinnerSlot = -1;
    private int matchWinnerSlot = -1;

    public MultiplayerGame(LobbySettings settings, List<String> playerNames) {
        this.settings = settings;
        this.rules = settings.toGameRules();
        this.playerNames = new ArrayList<>(playerNames);
        this.matchScores = new int[Math.max(settings.maxPlayers, playerNames.size())];
        this.trailSentCount = new int[Math.max(settings.maxPlayers, playerNames.size())];
        resetRound(playerNames.size());
    }

    public synchronized void resetRound(int playerCount) {
        players.clear();
        roundWinnerSlot = -1;
        phase = GameSnapshot.Phase.PLAYING;

        double[][] startPos = {
            {80, settings.gridHeight / 2.0},
            {settings.gridWidth - 80, settings.gridHeight / 2.0},
            {settings.gridWidth / 2.0, 80},
            {settings.gridWidth / 2.0, settings.gridHeight - 80}
        };
        Direction[] startDir = {
            Direction.RIGHT,
            Direction.LEFT,
            Direction.DOWN,
            Direction.UP
        };

        for (int i = 0; i < playerCount; i++) {
            int posIndex = i % startPos.length;
            int dirIndex = i % startDir.length;
            players.add(new Player(startPos[posIndex][0], startPos[posIndex][1],
                    startDir[dirIndex], rules.playerSpeed));
            trailSentCount[i] = 0;
        }
    }

    public synchronized void applyDirection(int slot, Direction direction) {
        if (direction == null || phase != GameSnapshot.Phase.PLAYING) return;
        if (slot >= 0 && slot < players.size()) players.get(slot).setDirection(direction);
    }

    public synchronized void update() {
        if (phase != GameSnapshot.Phase.PLAYING) return;

        for (Player player : players) {
            if (player.alive) player.move();
        }

        for (Player player : players) {
            if (player.alive) checkCollisions(player);
        }

        int aliveCount = 0;
        int aliveSlot = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).alive) {
                aliveCount++;
                aliveSlot = i;
            }
        }

        if (aliveCount <= 1) {
            roundWinnerSlot = aliveSlot;
            phase = GameSnapshot.Phase.ROUND_OVER;
            if (roundWinnerSlot >= 0) {
                matchScores[roundWinnerSlot]++;
                if (matchScores[roundWinnerSlot] >= settings.gamesToWin) {
                    matchWinnerSlot = roundWinnerSlot;
                    phase = GameSnapshot.Phase.MATCH_OVER;
                }
            }
        }
    }

    private void checkCollisions(Player player) {
        if (player.x < 0 || player.x > rules.gridWidth ||
            player.y < 0 || player.y > rules.gridHeight) {
            player.alive = false;
            return;
        }

        for (Player other : players) {
            int skipTail = other == player ? SELF_COLLISION_SAFE_SKIP : 0;
            if (other.trail.containsExcludingTail(player.x, player.y,
                    rules.collisionTolerance, skipTail)) {
                player.alive = false;
                return;
            }
        }
    }

    public synchronized boolean isRoundOver() {
        return phase == GameSnapshot.Phase.ROUND_OVER || phase == GameSnapshot.Phase.MATCH_OVER;
    }

    public synchronized boolean isMatchOver() {
        return phase == GameSnapshot.Phase.MATCH_OVER;
    }

    public synchronized int getRoundWinnerSlot() {
        return roundWinnerSlot;
    }

    public synchronized int getMatchWinnerSlot() {
        return matchWinnerSlot;
    }

    public synchronized int[] getScores() {
        return matchScores.clone();
    }

    public synchronized int getPlayerCount() {
        return players.size();
    }

    public synchronized GameSnapshot snapshot(boolean trailDeltaOnly) {
        GameSnapshot snapshot = new GameSnapshot();
        snapshot.phase = phase;
        snapshot.roundWinnerSlot = roundWinnerSlot;
        snapshot.matchWinnerSlot = matchWinnerSlot;
        snapshot.gamesToWin = settings.gamesToWin;

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            List<double[]> trail = new ArrayList<>();
            int from = trailDeltaOnly ? Math.min(trailSentCount[i], p.trail.points.size()) : 0;
            for (int j = from; j < p.trail.points.size(); j++) {
                Point pt = p.trail.points.get(j);
                trail.add(new double[]{pt.x, pt.y});
            }
            if (trailDeltaOnly) trailSentCount[i] = p.trail.points.size();

            String name = i < playerNames.size() ? playerNames.get(i) : "P" + (i + 1);
            snapshot.players.add(new PlayerSnapshot(i, p.x, p.y, p.alive,
                    i < matchScores.length ? matchScores[i] : 0, name, trail));
        }
        return snapshot;
    }
    public synchronized GameSnapshot snapshotUsingTrailCounters(int[] counters) {
        GameSnapshot snapshot = new GameSnapshot();
        snapshot.phase = phase;
        snapshot.roundWinnerSlot = roundWinnerSlot;
        snapshot.matchWinnerSlot = matchWinnerSlot;
        snapshot.gamesToWin = settings.gamesToWin;

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            List<double[]> trail = new ArrayList<>();
            int from = 0;
            if (counters != null && i < counters.length) {
                from = Math.min(counters[i], p.trail.points.size());
            }
            for (int j = from; j < p.trail.points.size(); j++) {
                Point pt = p.trail.points.get(j);
                trail.add(new double[]{pt.x, pt.y});
            }
            if (counters != null && i < counters.length) counters[i] = p.trail.points.size();

            String name = i < playerNames.size() ? playerNames.get(i) : "P" + (i + 1);
            snapshot.players.add(new PlayerSnapshot(i, p.x, p.y, p.alive,
                    i < matchScores.length ? matchScores[i] : 0, name, trail));
        }
        return snapshot;
    }

}
