package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class AreaDamageEffect implements PlantFoodEffectStrategy {
    private final int damageAmount;

    public AreaDamageEffect(int damageAmount) {
        this.damageAmount = damageAmount;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {

        if (plant.getName().equals("Bowling Bulb")) {
            // 1. INSTANT RELOAD!
            plant.reloadAllBulbs();

            // 2. Fire 3 Plasma Bulbs
            try {
                for (int i = 0; i < 3; i++) {
                    // Stagger the spawn X coordinate so they don't overlap perfectly
                    Projectile plasma = plant.template.getProjectileType()
                        .getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                        .newInstance(plant.getX() + (i * 0.5 * Constants.Game.TILE_SIZE), plant.getY(), 6.0, 600, 5);

                    plasma.setSourcePlantType(PlantType.BOWLING_BULB);
                    board.getActiveProjectiles().add(plasma);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Standard static explosion for plants like Cherry Bomb
            double radius = 3.0 * Constants.Game.TILE_SIZE;
            for (Zombie zombie : board.getAllZombies()) {
                if (zombie.isDead()) continue;

                double distance = Math.hypot(zombie.getX() - plant.getX(), zombie.getY() - plant.getY());
                if (distance <= radius) {
                    zombie.takeDamage(damageAmount, DamageType.EXPLOSIVE, PlantType.getByName(plant.getName()));
                }
            }
        }

        plant.resetFeed();
    }
}
