package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class IceLobbedProjectile extends LobbedProjectile {

    public IceLobbedProjectile(double x, double y, double targetX, double speed, int damage, int aoeDamage, double splashRadius) {
        super(x, y, targetX, speed, damage, aoeDamage, splashRadius);

        // Tag as ICE so fire/ice interactions trigger correctly!
        this.enumType = ProjectileType.ICE;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        // Track previous P to know exactly when the explosion happens
        double distanceTraveled = Math.abs(this.x - this.startX);
        double prevP = distanceTraveled / this.totalDistance;

        // Run the parent physics engine (which handles movement and damage)
        super.tick(board, delta);

        double newP = Math.abs(this.x - this.startX) / this.totalDistance;

        // --- THE ICE EXPLOSION INTERCEPT ---
        // If the parent tick() just pushed us past 1.0, the explosion just happened!
        if (prevP < 1.0 && newP >= 1.0) {

            double radiusPixels = this.splashRadius * Constants.Game.TILE_SIZE;

            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;

                double dist = Math.hypot(z.getX() - this.targetX, z.getY() - this.y);

                if (dist <= radiusPixels) {
                    // Apply a 5-second CHILLED effect to everything in the blast radius!
                    z.addEffect(new StatusEffect(EffectType.CHILLED, 50));
                }
            }
        }
    }
}
