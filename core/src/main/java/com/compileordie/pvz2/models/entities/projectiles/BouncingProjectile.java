package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class BouncingProjectile extends Projectile {
    private int bouncesRemaining;

    public BouncingProjectile(double x, double y, double speed, int damage, int bouncesRemaining) {
        super(x, y, speed, damage, DamageType.NORMAL);
        this.bouncesRemaining = bouncesRemaining;
    }

    @Override
    public void destroy() {
        if (bouncesRemaining > 0) {
            bouncesRemaining--;
            // Deflect up or down a lane randomly
            double deflectSpeed = (Math.random() > 0.5 ? 1.0 : -1.0) * (Constants.Game.TILE_SIZE * 0.5);
            this.ySpeed = deflectSpeed;
            // Keeps isDead = false so it continues moving
        } else {
            this.isDead = true;
        }
    }
}
