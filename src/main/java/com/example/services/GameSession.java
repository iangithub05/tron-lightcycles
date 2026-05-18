package com.example.services;

import com.example.models.Difficulty;
import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.GameMode;
import com.example.models.GameRules;
import com.example.models.Player;

public class GameSession {

    public final GameRules rules;
    public final GameMode mode;
    public Game game;

    private static final double[][] START_POSITIONS = {
 {80, 80},
 {1200, 80},
 {1200, 640},
 {80, 640}
    };

    private static final Direction[] START_DIRECTIONS = {
        Direction.RIGHT,
        Direction.DOWN,
        Direction.LEFT,
        Direction.UP
    };

    public GameSession(GameRules rules, GameMode mode) {
        this.rules = rules;
        this.mode = mode;
        buildGame();
    }

    public GameSession() {
        this.rules = new GameRules();
        this.mode = GameMode.VS_AI;
        buildGame();
    }

    private void buildGame() {
        game = new Game(rules);
        int count = mode == GameMode.VS_AI ? 2 : 1;
        for (int i = 0; i < count; i++) {
            double sx = Math.min(START_POSITIONS[i][0], rules.gridWidth - 80);
            double sy = Math.min(START_POSITIONS[i][1], rules.gridHeight - 80);
            game.addPlayer(new Player(sx, sy, START_DIRECTIONS[i], rules.playerSpeed));
        }
    }

    public void restart() {
        buildGame();
    }

    public boolean tick() {
        game.update();
        return !game.isOver();
    }
}
