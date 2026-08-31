package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class DigestStrategy implements AttackStrategy {

    private final double rangeTiles;

    public DigestStrategy(double rangeTiles) {
        this.rangeTiles = rangeTiles;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isArmed()) return;

        // --- FIX: Bulletproof grid math! ---
        int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);

        double eatMin = plant.getX() - (Constants.Game.TILE_WIDTH * 0.25); // Slight leeway behind
        double eatMax = plant.getX() + (rangeTiles * Constants.Game.TILE_WIDTH);

        Zombie target = null;
        double closestFront = Double.MAX_VALUE;

        for (Zombie z : board.getAllZombies()) {
            if (z.isDead() || !z.occupiesRow(plantRow)) continue;

            if (z.getX() >= eatMin && z.getX() <= eatMax) {
                double dist = z.getX() - plant.getX();
                if (dist < closestFront) {
                    closestFront = dist;
                    target = z;
                }
            }
        }

        if (target != null) {
            target.takeDamage(99999, DamageType.NORMAL, PlantType.getByName(plant.getName()));

            // Chomper successfully ate! We DO NOT set holdAction=true.
            // The engine will naturally reset his action timer to 0, starting the 40s digest!
        } else {
            plant.holdAction = true; // Stay ready to bite!
        }
    }
}
