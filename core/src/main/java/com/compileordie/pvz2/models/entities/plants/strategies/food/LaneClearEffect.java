package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class LaneClearEffect implements PlantFoodEffectStrategy {
    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        int plantRow = (int) (plant.getY() / Constants.Game.TILE_SIZE);
        double maxX = board.totalCols * Constants.Game.TILE_SIZE;

        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;

            if (zombie.getCurrentRow() == plantRow) {

                // --- FUME-SHROOM LOGIC ---
                if (plant.getName().equals("Fume-shroom")) {
                    // Only hit zombies in front of the Fume-shroom
                    if (zombie.getX() >= plant.getX()) {
                        zombie.takeDamage(1500, DamageType.NORMAL, PlantType.FUME_SHROOM);

                        // Heavy Knockback (3 Tiles)
                        if (!zombie.isDead()) {
                            double newX = Math.min(zombie.getX() + (3 * Constants.Game.TILE_SIZE), maxX);
                            zombie.setX(newX);
                        }
                    }
                }
                // --- CITRON LOGIC (And Fallback) ---
                else {
                    zombie.takeDamage(2000, DamageType.NORMAL, PlantType.getByName(plant.getName()));
                }
            }
        }

        plant.resetFeed();
    }
}
