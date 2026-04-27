package com.example.services;

import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.GameMode;
import com.example.models.GameRules;
import com.example.models.Player;
import com.example.utils.ConfigManager;

import java.util.ArrayList;
import java.util.List;

// The brain of the game — decides who controls what each frame.
// It doesn't draw anything or handle raw input; it only orchestrates.
public class GameSession {

    // Readable from GameScreen for rendering, but only written here
    public Game    game;
    public Player  humanPlayer;
    public boolean running = false;

    // One controller per player (index 0 = human, 1-3 = AIs)
    private final List<Controller>   controllers   = new ArrayList<>();
    private final List<AIController> aiControllers = new ArrayList<>();

    // Tracks how many frames have passed since each AI last made a decision
    private final int[] aiFrameCounters = new int[3];

    // Captured once at session creation — changes mid-game won't affect this session
    private final GameMode mode;

    public GameSession() {
        this.mode = ConfigManager.gameMode;
    }

    // --- Public API ----------------------------------------------------------

    // Sets up a fresh game and starts the session.
    // Safe to call multiple times (Retry reuses the same GameSession object).
    public void start() {
        controllers.clear();
        aiControllers.clear();
        for (int i = 0; i < aiFrameCounters.length; i++) aiFrameCounters[i] = 0;

        buildGame();
        running = true;
    }

    // Alias for start() — makes call sites at the Retry button easier to read.
    public void restart() {
        start();
    }

    // Called every frame by the AnimationTimer in GameScreen.
    // Returns true while the game is still going, false when the human dies.
    public boolean tick() {
        if (!running) return false;

        // Human input — PlayerController reads the keyboard state
        Direction humanDir = controllers.get(0).getNextDirection(humanPlayer, game);
        if (humanDir != null) humanPlayer.setDirection(humanDir);

        // AI decisions — each AI has its own tick interval (see ConfigManager.AI_TICK_INTERVALS)
        for (int i = 0; i < aiControllers.size(); i++) {
            aiFrameCounters[i]++;

            int interval = ConfigManager.AI_TICK_INTERVALS[i]; // how often this AI decides
            if (aiFrameCounters[i] < interval) continue;

            aiFrameCounters[i] = 0;

            Player aiPlayer = game.players.get(i + 1); // +1 because index 0 is the human
            if (!aiPlayer.alive) continue;

            Direction aiDir = aiControllers.get(i).getNextDirection(aiPlayer, game);
            if (aiDir != null) aiPlayer.setDirection(aiDir);
        }

        // Physics step — moves every player and checks collisions
        game.update();

        // Game over when the human hits something
        if (!humanPlayer.alive) {
            running = false;
            return false;
        }

        return true;
    }

    // --- Setup helpers -------------------------------------------------------

    private void buildGame() {
        game = new Game(buildRules());

        // Human player always occupies slot 0
        humanPlayer = spawnPlayer(0);
        game.addPlayer(humanPlayer);
        controllers.add(new PlayerController());

        if (mode == GameMode.VS_AI) {
            // Slot 1 — AvoidDeath AI (green), fastest reaction, smartest
            addAIPlayer(1, new AvoidDeathAIController());

            // Slot 2 — Pathfinding AI (orange), BFS-based, medium reaction
            addAIPlayer(2, new PathfindingAIController());

            // Slot 3 — Random AI (purple), slowest reaction, dumbest
            addAIPlayer(3, new RandomAIController());
        }
        // VS_SELF mode: only the human is added, no AI
    }

    // Creates a player at the configured starting position and direction for the given slot.
    private Player spawnPlayer(int slot) {
        double[]  pos = ConfigManager.START_POSITIONS[slot];
        Direction dir = ConfigManager.START_DIRECTIONS[slot];
        Player p = new Player(pos[0], pos[1]);
        p.direction = dir;
        return p;
    }

    private void addAIPlayer(int slot, AIController ctrl) {
        game.addPlayer(spawnPlayer(slot));
        aiControllers.add(ctrl);
    }

    private GameRules buildRules() {
        GameRules rules = new GameRules();
        rules.gridWidth          = ConfigManager.gridWidth;
        rules.gridHeight         = ConfigManager.gridHeight;
        rules.playerSpeed        = ConfigManager.playerSpeed;
        rules.collisionTolerance = ConfigManager.collisionTolerance;
        return rules;
    }
}
