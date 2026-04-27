package com.example.services;

import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.Player;

/** Base interface for anything that decides a player's next direction. */
public interface Controller {
    Direction getNextDirection(Player player, Game game);
}
