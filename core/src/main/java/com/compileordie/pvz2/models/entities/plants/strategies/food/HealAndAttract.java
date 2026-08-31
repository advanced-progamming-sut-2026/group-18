package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class HealAndAttract implements PlantFoodEffectStrategy {

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        // 1. Heal itself completely to its maximum first HP!
        plant.setCurrentHp(plant.getBaseHp());

        // 2. Attract all zombies in the 3x3 area
        int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
        double pullRadius = 1.5 * Constants.Game.TILE_HEIGHT;

        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;

            // Only pull zombies that are NOT already in our lane!
            if (!zombie.occupiesRow(plantRow)) {
                double dist = Math.hypot(zombie.getX() - plant.getX(), zombie.getY() - plant.getY());

                if (dist <= pullRadius) {
                    // Yank them forcefully into the Sweet Potato's lane!
                    double targetY = plantRow * Constants.Game.TILE_HEIGHT + (Constants.Game.TILE_HEIGHT / 2.0);
                    zombie.setY(targetY);
                }
            }
        }
        plant.resetFeed();
    }
}
