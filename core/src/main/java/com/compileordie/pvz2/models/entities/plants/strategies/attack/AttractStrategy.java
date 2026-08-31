package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class AttractStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isAlive() || !plant.isArmed()) return;

        // --- FIX 1: Use Math.floor() so the grid calculation NEVER drifts! ---
        int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);

        // --- FIX 2: Your forward-facing directional radar! ---
        double pullRangeMax = plant.getX() + (1.5 * Constants.Game.TILE_WIDTH); // 1.5 tiles ahead
        double pullRangeMin = plant.getX() - (Constants.Game.TILE_WIDTH * 0.25); // Tiny leeway behind

        for (Zombie z : board.getAllZombies()) {
            if (z.isDead()) continue;

            // 1. Must be in the lane directly above or below
            if (Math.abs(z.getCurrentRow() - plantRow) == 1) {

                // 2. Must NOT have passed the Sweet Potato, but must be within 1.5 tiles!
                if (z.getX() >= pullRangeMin && z.getX() <= pullRangeMax) {

                    // Sucks them into this lane!
                    z.setY(plant.getY());
                    z.setCurrentRow(plantRow);
                }
            }
        }
    }
}
