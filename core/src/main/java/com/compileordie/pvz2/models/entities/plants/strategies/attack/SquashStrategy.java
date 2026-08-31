package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.SquashProjectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class SquashStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        // If it's already jumping, or if it's exhausted as a meat shield, do nothing!
        if (plant.isHidden() || plant.isExhausted() || !plant.isArmed()) return;

        int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
        Zombie closestTarget = null;
        double closestDist = Double.MAX_VALUE;

        // Scan 1.5 tiles front and back
        for (Zombie z : board.getAllZombies()) {
            if (z.isDead() || !z.occupiesRow(plantRow)) continue;

            double dist = Math.abs(z.getX() - plant.getX());
            if (dist <= 1.5 * Constants.Game.TILE_HEIGHT && dist < closestDist) {
                closestDist = dist;
                closestTarget = z;
            }
        }

        if (closestTarget != null) {
            // TRIGGER THE CHAIN!
            plant.setHidden(true); // Hide the actual plant
            int totalCrushes = 1 + plant.getExtraCrushes(); // Base 1, +1 if upgraded

            SquashProjectile jumpOut = new SquashProjectile(plant, plant.getX(), plant.getY(), closestTarget.getX(), closestTarget.getY(), false, totalCrushes, false);
            board.getActiveProjectiles().add(jumpOut);
        }
    }
}
