package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class TangleKelpStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isArmed()) return;

        int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
        int plantCol = (int) (plant.getX() / Constants.Game.TILE_WIDTH);

        // Base targets = 1. Plus upgrades!
        int maxTargets = 1 + plant.getExtraTargets();
        int targetsHit = 0;

        for (Zombie z : board.getAllZombies()) {
            if (z.isDead()) continue;

            if (z.occupiesRow(plantRow)) {
                int zCol = (int) (z.getX() / Constants.Game.TILE_WIDTH);
                if (zCol == plantCol) {

                    // Instantly kills normal zombies, severely damages Gargantuars!
                    z.takeDamage(800, DamageType.NORMAL, PlantType.getByName(plant.getName()));
                    targetsHit++;

                    if (targetsHit >= maxTargets) {
                        break; // Stop pulling if we hit our capacity!
                    }
                }
            }
        }

        // If we grabbed at least one zombie, the Kelp destroys itself!
        if (targetsHit > 0) {
            plant.die();
        }
    }
}
