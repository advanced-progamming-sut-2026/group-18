package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class AttractStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isAlive() || !plant.isArmed()) return;

        int plantRow = (int) Math.round((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);

        // A perfect 1.5 tile gravity field (exactly like Phat Beet)
        double pullRadiusPx = 1.5 * Constants.Game.TILE_WIDTH;

        for (Zombie z : board.getAllZombies()) {
            if (z.isDead()) continue;
            if (Math.abs(z.getCurrentRow() - plantRow) == 1) {
                double distPx = Math.abs(z.getX() - plant.getX());
                if (distPx <= pullRadiusPx) {
                    z.setY(plant.getY());
                     z.setCurrentRow(plantRow);
                }
            }
        }
    }
}
