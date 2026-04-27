package com.example.services;

import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.Player;
import com.example.utils.InputManager;
import com.example.utils.KeyBindings;

// Reads the current keyboard state and returns the matching direction.
// Returns null when no key is pressed, which tells the player to keep its current direction.
public class PlayerController implements Controller {

    @Override
    public Direction getNextDirection(Player player, Game game) {
        if (InputManager.isDown(KeyBindings.UP))    return Direction.UP;
        if (InputManager.isDown(KeyBindings.DOWN))  return Direction.DOWN;
        if (InputManager.isDown(KeyBindings.LEFT))  return Direction.LEFT;
        if (InputManager.isDown(KeyBindings.RIGHT)) return Direction.RIGHT;
        return null;
    }
}
