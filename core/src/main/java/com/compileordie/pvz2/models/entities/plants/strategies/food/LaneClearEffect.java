package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class LaneClearEffect implements PlantFoodEffectStrategy {
    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {

        int plantRow = (int) (plant.getY() / com.compileordie.pvz2.config.Constants.Game.TILE_SIZE);

        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;

            if (zombie.getCurrentRow() == plantRow) {
                // Passed the PlantType so Citron gets the credit for the lane wipe!
                zombie.takeDamage(2000, DamageType.NORMAL, PlantType.getByName(plant.getName()));
            }
        }

        plant.resetFeed();
    }
}
