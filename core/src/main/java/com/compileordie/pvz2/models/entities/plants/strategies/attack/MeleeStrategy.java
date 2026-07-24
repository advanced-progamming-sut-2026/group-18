package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

import java.util.List;

public class MeleeStrategy implements AttackStrategy {

    private final double rangeTiles; // E.g., 1.5 for a 3x3 AoE, 1.0 for straight ahead
    private final boolean isAoE;     // True for Phat Beet, False for Bonk Choy
    private final boolean isInstaKill; // True for Chomper

    public MeleeStrategy(double rangeTiles, boolean isAoE, boolean isInstaKill) {
        this.rangeTiles = rangeTiles;
        this.isAoE = isAoE;
        this.isInstaKill = isInstaKill;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        double attackRadius = rangeTiles * Constants.Game.TILE_SIZE;
        List<Zombie> zombies = board.getAllZombies();
        boolean hasAttacked = false;

        for (Zombie zombie : zombies) {
            if (zombie.isDead()) continue;

            double distance = Math.hypot(zombie.getX() - plant.getX(), zombie.getY() - plant.getY());

            if (distance <= attackRadius) {
                if (isInstaKill) {
                    zombie.takeDamage(9999, DamageType.NORMAL); // Or zombie.die()
                    hasAttacked = true;
                } else {
                    zombie.takeDamage(plant.getBaseDamage(), DamageType.NORMAL);
                    hasAttacked = true;
                }

                // If it's a single-target brawler (Bonk Choy), stop after hitting the first zombie
                if (!isAoE) {
                    break;
                }
            }
        }

        // If it's a Chomper (InstaKill), we simulate the "chewing" cooldown by forcing the action timer negative
        if (hasAttacked && isInstaKill) {
            // Wait 40 seconds before attacking again
            plant.setActionIntervalTicks(40.0); // Adjust according to your tick delta math
        }
    }
}
