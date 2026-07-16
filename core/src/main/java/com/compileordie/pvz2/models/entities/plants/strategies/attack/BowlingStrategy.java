package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.variants.Plant;
import com.compileordie.pvz2.models.entities.plants.specific.BowlingBulbPlant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.projectiles.BowlingProjectile;

public class BowlingStrategy implements AttackStrategy {

    // Using double to match GameEntity's xSpeed requirement
    private static final double PROJECTILE_SPEED = 150.0;

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        // Find the lane this plant is sitting in
        Lane lane = board.getLane(plant.getTileRow());
        boolean targetInSight = false;

        // 1. Is there a living zombie in this lane ahead of the plant?
        if (lane != null && lane.zombies != null) {
            for (Zombie zombie : lane.zombies) {
                if (!zombie.isDead() && zombie.getX() >= plant.getX()) {
                    targetInSight = true;
                    break;
                }
            }
        }

        // 2. If we see a target, pull the trigger!
        if (targetInSight && plant instanceof BowlingBulbPlant) {
            BowlingBulbPlant bowlingPlant = (BowlingBulbPlant) plant;

            // Grab the exact 1, 2, or 3 bulb state from the plant
            int bulbToFire = bowlingPlant.consumeBulb();

            if (bulbToFire > 0) {
                // Spawn the projectile exactly where the plant is
                BowlingProjectile projectile = new BowlingProjectile(
                    plant.getX(),
                    plant.getY(),
                    PROJECTILE_SPEED,
                    0.0, // ySpeed (starts rolling straight)
                    bulbToFire
                );

                board.projectiles.add(projectile);
            }
        }
    }
}
