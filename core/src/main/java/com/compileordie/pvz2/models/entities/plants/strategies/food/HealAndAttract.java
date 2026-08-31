package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class HealAndAttract implements PlantFoodEffectStrategy {

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        // 1. Heal itself completely to maximum base HP
        plant.setCurrentHp(plant.getBaseHp());

        int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);

        // 2. Global Magnet: Yank EVERY zombie on the board into this lane!
        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;

            // If they are in any other lane, forcefully snap them into the Sweet Potato's lane
            if (!zombie.occupiesRow(plantRow)) {
                zombie.setY(plant.getY());
                zombie.setCurrentRow(plantRow);
            }
        }

        // (PlantFoodClipManager handles the 2.0s visual timer before returning to idle)
    }
}
