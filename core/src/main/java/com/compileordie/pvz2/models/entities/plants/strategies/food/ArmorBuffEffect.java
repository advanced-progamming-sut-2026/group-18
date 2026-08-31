package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class ArmorBuffEffect implements PlantFoodEffectStrategy {
    private final int bonusArmorHp;

    public ArmorBuffEffect(int bonusArmorHp) {
        this.bonusArmorHp = bonusArmorHp;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        // Fully heal and apply armor
        plant.setCurrentHp(plant.getBaseHp() + bonusArmorHp);

        // Spawn the shiny overlay visual exclusively for Sun Bean
        if (plant.getName().equals("Sun Bean")) {
            com.compileordie.pvz2.models.entities.plants.strategies.food.AreaDamageEffect.spawnVisualHit(
                board, plant.getX(), plant.getY(), PlantType.SUN_BEAN, true
            );
        }
    }
}
