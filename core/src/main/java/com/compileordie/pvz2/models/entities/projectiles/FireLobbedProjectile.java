package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class FireLobbedProjectile extends LobbedProjectile {

    public FireLobbedProjectile(double x, double y, double targetX, double speed, int damage, int aoeDamage, double splashRadius) {
        super(x, y, targetX, speed, damage, aoeDamage, splashRadius);

        // Tag as FIRE so it instantly melts ice blocks and un-freezes zombies!
        this.enumType = ProjectileType.FIRE;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        double distanceTraveled = Math.abs(this.x - this.startX);
        double prevP = distanceTraveled / this.totalDistance;

        super.tick(board, delta);

        double newP = Math.abs(this.x - this.startX) / this.totalDistance;

        // --- THE FIRE EXPLOSION INTERCEPT ---
        if (prevP < 1.0 && newP >= 1.0) {

            double radiusPixels = this.splashRadius * Constants.Game.TILE_SIZE;

            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;

                double dist = Math.hypot(z.getX() - this.targetX, z.getY() - this.y);

                if (dist <= radiusPixels) {
                    // FIRE cleanses CHILLED and FROZEN status effects!
                    z.removeStatusEffect(EffectType.CHILLED);
                    z.removeStatusEffect(EffectType.FROZEN);
                }
            }
        }
    }
}
