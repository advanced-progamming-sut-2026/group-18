package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class ModifierPassiveStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.getName().equals("Torchwood")) return;

        // Define Torchwood's hitbox
        double leftBound = plant.getX() - 10;
        double rightBound = plant.getX() + Constants.Game.TILE_WIDTH;

        for (Projectile p : board.getActiveProjectiles()) {

            // Check if the projectile is in the same row and overlapping Torchwood
            // (Assuming your projectiles share the same Y coordinate as the plant)
            if (p.getY() == plant.getY() && p.getX() >= leftBound && p.getX() <= rightBound) {

                // CRITICAL: We only ignite it ONCE!
                if (!p.isIgnited()) {
                    p.setIgnited(true);

                    // If you have a DamageType Enum for projectiles, set it here!
                    p.setType(DamageType.FIRE);

                    // Check if we are normal fire or BLUE fire!
                    if (plant.isBlueFlame()) {
                        p.setDamage(p.getDamage() * 3); // Blue flame = 3x damage!
                    } else {
                        p.setDamage(p.getDamage() * 2); // Normal flame = 2x damage!
                    }
                }
            }
        }
    }
}
