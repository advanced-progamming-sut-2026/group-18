package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.SquashProjectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class SquashStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (plant.isHidden() || plant.isExhausted() || !plant.isArmed()) return;

        int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
        Zombie closestTarget = null;
        double closestDist = Double.MAX_VALUE;

        // Scan 2.0 tiles front and back
        for (Zombie z : board.getAllZombies()) {
            if (z.isDead() || z.getCurrentRow() != plantRow) continue;

            double dist = Math.abs(z.getX() - plant.getX());
            if (dist <= 2.0 * Constants.Game.TILE_WIDTH && dist < closestDist) {
                closestDist = dist;
                closestTarget = z;
            }
        }

        if (closestTarget != null) {
            // --- THE FIX: We completely deleted the 10-tick "turn" delay! ---
            // Now, backward jumps trigger instantly, EXACTLY like forward jumps!
            plant.isWindingUp = false;
            plant.setHidden(true);
            int totalCrushes = 1 + plant.getExtraCrushes();

            SquashProjectile jumpOut = new SquashProjectile(plant, plant.getX(), plant.getY(), closestTarget.getX(), closestTarget.getY(), false, totalCrushes, false);
            board.getActiveProjectiles().add(jumpOut);
        }
    }
}
