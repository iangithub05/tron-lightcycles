package com.example.services;

import com.example.models.Difficulty;
import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.GameMode;
import com.example.models.GameRules;
import com.example.models.Player;

/**
 * Singleplayer-specific session: 1 human (index 0) + 3 AI players (indices 1-3).
 *
 * Extends GameSession so it can be passed unchanged to GameEngine / GameScreen.
 * Multiplayer code never touches this class.
 *
 * The parent constructor creates a minimal 2-player game for VS_AI mode; we
 * immediately overwrite it in rebuildGame() to get the full 4-player layout.
 * This avoids changing any field visibility in GameSession.
 */
public class SinglePlayerSession extends GameSession {

    // Corners of the 1280x720 arena, safely inset from the wall
    private static final double[][] START_POS = {
        { 80,  80}, // human  – top-left,   heading RIGHT
        {1200,  80},  // AI #1  – top-right,  heading DOWN
        {1200, 640},  // AI #2  – bottom-right, heading LEFT
        { 80,  640},  // AI #3  – bottom-left,  heading UP
    };

    private static final Direction[] START_DIR = {
        Direction.RIGHT,
        Direction.DOWN,
        Direction.LEFT,
        Direction.UP,
    };

    private final Difficulty difficulty;
    private AIController[] aiControllers;

    // AI path scoring/flood-fill is the heaviest singleplayer cost.
    // Recalculate AI decisions every few ticks and keep moving in the previous
    // chosen direction between recalculations. This keeps motion smooth while
    // cutting most AI CPU work.
    private int aiDecisionTick = 0;
    private static final int AI_DECISION_INTERVAL = 3;

    public SinglePlayerSession(Difficulty difficulty) {
        // super() calls buildGame() → creates 2-player game for VS_AI.
        // We immediately replace that with the real 4-player layout below.
        super(makeRules(), GameMode.VS_AI);
        this.difficulty = difficulty;
        rebuildGame();
        initAI();
    }

    /** Fast singleplayer speed; tolerance is 0.75× speed to avoid false self-hits. */
    private static GameRules makeRules() {
        GameRules r = new GameRules();
        r.playerSpeed = 5.5;
        r.collisionTolerance = 4.0;
        // Skip only 3 recent points — the nearest unchecked is 4×5.5=22px away,
        // well beyond tolerance, so no false self-hits while tight loops now kill.
        r.safeSkipCount = 3;
        return r;
    }

    private void rebuildGame() {
        game = new Game(rules);
        for (int i = 0; i < 4; i++) {
            double sx = Math.min(START_POS[i][0], rules.gridWidth  - 80);
            double sy = Math.min(START_POS[i][1], rules.gridHeight - 80);
            game.addPlayer(new Player(sx, sy, START_DIR[i], rules.playerSpeed));
        }
    }

    private void initAI() {
        aiControllers = switch (difficulty) {
            case EASY   -> new AIController[]{ new SPEasyAI(), new SPEasyAI(), new SPEasyAI()   };
            case MEDIUM -> new AIController[]{ new SPMediumAI(), new SPMediumAI(), new SPMediumAI() };
            case HARD   -> new AIController[]{ new SPHardAI(), new SPHardAI(), new SPHardAI()   };
        };
    }

    /**
     * Each tick: AI picks directions, physics runs, then we check end conditions.
     * In singleplayer the game ends as soon as the human (index 0) dies — we do
     * NOT wait for only one player to remain alive (that would let the loop keep
     * running with 3 AIs after the human is already dead).
     */
    @Override
    public boolean tick() {
        boolean recomputeAI = (aiDecisionTick++ % AI_DECISION_INTERVAL) == 0;

        if (recomputeAI) {
            for (int i = 0; i < 3; i++) {
                Player ai = game.players.get(i + 1);
                if (ai.alive) {
                    Direction chosen = aiControllers[i].computeDirection(ai, game);
                    ai.setDirection(chosen);
                }
            }
        }

        super.tick();  // runs game.update() — ignore its return value
        Player human = game.players.get(0);
        // Keep running only while the human is alive AND the round isn't over
        return human.alive && !game.isOver();
    }

    /** Full reset: rebuild the arena and keep the same AI controller instances. */
    @Override
    public void restart() {
        aiDecisionTick = 0;
        rebuildGame();
        // AI controllers are stateless between rounds; no need to recreate them
    }
}
