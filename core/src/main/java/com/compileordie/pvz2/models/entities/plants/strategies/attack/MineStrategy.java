package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

import java.util.List;

public class MineStrategy implements AttackStrategy {

    private final double explosionRadiusTiles;
    private double armingTimer = 0;
    private final double armingTimeRequired = 15.0; // 15 seconds to arm (adjust to ticks as needed)
    private boolean isArmed = false;

    public MineStrategy(double explosionRadiusTiles) {
        this.explosionRadiusTiles = explosionRadiusTiles;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        // Step 1: Handle Arming
        if (!isArmed) {
            armingTimer += (tickDelta / 60.0); // Assuming 60 ticks per second
            if (armingTimer >= armingTimeRequired) {
                isArmed = true;
            }
            return; // Cannot detonate while arming
        }

        // Step 2: Collision Detection
        List<Zombie> zombies = board.getAllZombies();
        boolean triggered = false;

        for (Zombie zombie : zombies) {
            if (zombie.isDead()) continue;

            // Trigger radius is very tight (must step directly on it)
            double distance = Math.hypot(zombie.getX() - plant.getX(), zombie.getY() - plant.getY());
            if (distance <= Constants.Game.TILE_SIZE * 0.4) {
                triggered = true;
                break;
            }
        }

        // Step 3: Detonation
        if (triggered) {
            double explosionRadius = explosionRadiusTiles * Constants.Game.TILE_SIZE;
            for (Zombie zombie : zombies) {
                if (zombie.isDead()) continue;

                double distance = Math.hypot(zombie.getX() - plant.getX(), zombie.getY() - plant.getY());
                if (distance <= explosionRadius) {
                    zombie.takeDamage(plant.getBaseDamage(), DamageType.EXPLOSIVE);
                }
            }
            plant.setCurrentHp(0); // Destroys the mine after explosion
        }
    }
}
