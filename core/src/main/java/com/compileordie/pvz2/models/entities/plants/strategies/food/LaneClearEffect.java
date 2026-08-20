package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

import java.util.Random;

public class LaneClearEffect implements PlantFoodEffectStrategy {

    private final Random random = new Random();

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        int plantRow = (int) (plant.getY() / Constants.Game.TILE_SIZE);
        double maxX = board.totalCols * Constants.Game.TILE_SIZE;

        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;

            if (zombie.getCurrentRow() == plantRow) {

                // --- GARLIC LOGIC (Lane Shifting, NO Damage) ---
                if (plant.getName().equals("Garlic")) {
                    int targetRow = plantRow;

                    // Determine safe adjacent lane
                    if (plantRow == 0) {
                        targetRow = 1; // Top lane can only go down
                    } else if (plantRow == board.totalRows - 1) {
                        targetRow = plantRow - 1; // Bottom lane can only go up
                    } else {
                        // Middle lanes randomly choose up or down!
                        targetRow = random.nextBoolean() ? plantRow - 1 : plantRow + 1;
                    }

                    // Force the zombie's Y coordinate to the new lane!
                    double newY = targetRow * Constants.Game.TILE_SIZE + (Constants.Game.TILE_SIZE / 2.0);
                    zombie.setY(newY);
                }
                // --- FUME-SHROOM LOGIC ---
                else if (plant.getName().equals("Fume-shroom")) {
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
