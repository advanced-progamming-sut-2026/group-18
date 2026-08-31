package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class AttractStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isArmed()) return;

        int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
        double pullRadius = plant.getRangeTiles() * Constants.Game.TILE_HEIGHT;

        for (Zombie z : board.getAllZombies()) {
            if (z.isDead()) continue;

            // Only pull zombies that are in adjacent lanes
            if (!z.occupiesRow(plantRow)) {

                double dist = Math.hypot(z.getX() - plant.getX(), z.getY() - plant.getY());

                // If they step into the 3x3 gravity field, pull them in!
                if (dist <= pullRadius) {
                    double targetY = plantRow * Constants.Game.TILE_HEIGHT + (Constants.Game.TILE_HEIGHT / 2.0);
                    z.setY(targetY);
                }
            }
        }
    }
}
