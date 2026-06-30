package com.compileordie.pvz2.models.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.variants.Plant;

public interface PlantFoodEffectStrategy {
    void activate(Plant plant, GameBoard board, Player player);
}
