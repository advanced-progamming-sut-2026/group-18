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

        int plantRow = (int) (plant.getY() / Constants.Game.TILE_SIZE);
        double plantX = plant.getX();
        double attackRadiusPixels = rangeTiles * Constants.Game.TILE_SIZE;

        Zombie target = null;
        double closestFront = Double.MAX_VALUE;

        for (Zombie z : board.getAllZombies()) {
            if (z.isDead() || z.getCurrentRow() != plantRow) continue;

            // Positive distance means the zombie is in front (right)
            double dist = z.getX() - plantX;

            // -20 pixel tolerance so it can eat zombies that are currently biting it!
            if (dist >= -20 && dist <= attackRadiusPixels) {
                if (dist < closestFront) {
                    closestFront = dist;
                    target = z; // Grab the closest one!
                }
            }
        }

        if (target != null) {
            target.takeDamage(99999, DamageType.NORMAL, PlantType.getByName(plant.getName()));

            // Note: I DO NOT set holdAction = true here.
            // This allows Plant.java to reset the timer to 0, starting the 40-second digest!
        } else {
            plant.holdAction = true;
        }
    }
}
