package com.example.services;

import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.Player;

import java.util.Random;

// Dumbest AI: picks a completely random direction each tick.
// setDirection() in Player prevents reversing, so it won't instantly suicide.
public class RandomAIController implements AIController {

    private final Random random = new Random();

    @Override
    public Direction getNextDirection(Player player, Game game) {
        Direction[] all = Direction.values();
        return all[random.nextInt(all.length)];
    }
}
