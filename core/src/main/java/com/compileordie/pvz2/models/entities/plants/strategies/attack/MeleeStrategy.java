package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class MeleeStrategy implements AttackStrategy {

    private final boolean isAoE;
    private final boolean isInstantKill;

    // REMOVED rangeTiles from the constructor!
    public MeleeStrategy(boolean isAoE, boolean isInstantKill) {
        this.isAoE = isAoE;
        this.isInstantKill = isInstantKill;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isArmed()) return;

        int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
        double plantX = plant.getX();

        double attackRadiusPixels = plant.getRangeTiles() * Constants.Game.TILE_HEIGHT;

        boolean attacked = false;

        // --- KIWIBEAST DYNAMIC DAMAGE CALCULATION ---
        int currentDamage = plant.getBaseDamage();
        if (plant.getName().equals("Kiwibeast")) {
            int stage = plant.getGrowthStage(); // Piggybacks on Sun-shroom's math!

            // Stage 1 adds 0. Stage 2 adds +15. Stage 3 adds +30. Stage 4 adds +45!
            currentDamage += (stage - 1) * 15;
        }

        // --- PHAT BEET LOGIC (Radial 3x3 AoE) ---
        if (isAoE) {
            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;

                double dist = Math.hypot(z.getX() - plant.getX(), z.getY() - plant.getY());
                if (dist <= attackRadiusPixels) {
                    z.takeDamage(currentDamage, DamageType.NORMAL, PlantType.getByName(plant.getName()));
                    attacked = true;
                }
            }
        }
        // --- BONK CHOY & WASABI WHIP LOGIC (Linear, Front/Back Priority) ---
        else if (!isInstantKill) {

            Zombie bestFront = null;
            double closestFront = Double.MAX_VALUE;

            Zombie bestBack = null;
            double closestBack = Double.MAX_VALUE;

            for (Zombie z : board.getAllZombies()) {
                if (z.isDead() || z.getCurrentRow() != plantRow) continue;

                double dist = z.getX() - plantX;

                if (Math.abs(dist) <= attackRadiusPixels) {
                    if (dist >= 0 && dist < closestFront) {
                        closestFront = dist;
                        bestFront = z;
                    } else if (dist < 0 && Math.abs(dist) < closestBack) {
                        closestBack = Math.abs(dist);
                        bestBack = z;
                    }
                }
            }

            Zombie target = (bestFront != null) ? bestFront : bestBack;

            if (target != null) {
                target.takeDamage(currentDamage, DamageType.NORMAL, PlantType.getByName(plant.getName()));
                attacked = true;
            }
        }
        if (!attacked) {
            plant.holdAction = true;
        }
    }
}
