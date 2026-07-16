package com.compileordie.pvz2.models.plants.strategies.attack;

import com.compileordie.pvz2.models.plants.variants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;

public interface AttackStrategy {
    void attack(Plant plant, GameBoard board, int tickDelta);
}
