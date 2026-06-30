package com.compileordie.pvz2.models.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.variants.Plant;

public interface AttackStrategy {
    void attack(Plant plant, GameBoard board, int tickDelta);
}
