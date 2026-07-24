package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class ProjectileEnhanceEffect implements PlantFoodEffectStrategy {
    private final int damageMultiplier;

    public ProjectileEnhanceEffect(int damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        plant.setBaseDamage(plant.getBaseDamage() * damageMultiplier);
        plant.setCurrentHp(plant.getBaseHp() * 2);
    }
}
