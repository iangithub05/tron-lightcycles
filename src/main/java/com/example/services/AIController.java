package com.example.services;

import com.example.models.Direction;
import com.example.models.Game;
import com.example.models.Player;

public interface AIController {
    Direction computeDirection(Player player, Game game);
}
