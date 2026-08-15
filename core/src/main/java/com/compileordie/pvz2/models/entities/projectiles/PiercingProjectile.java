package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class PiercingProjectile extends Projectile {
    private int pierceRemaining;

    // Your existing 5-argument constructor
    public PiercingProjectile(double x, double y, double speed, int damage, int maxPierces) {
        super(x, y, speed, damage, DamageType.NORMAL);
        this.pierceRemaining = maxPierces;
    }

    public PiercingProjectile(double x, double y, double speed, int damage) {
        super(x, y, speed, damage, DamageType.NORMAL);
        this.pierceRemaining = 3; // Default pierce count
    }

    @Override
    public void destroy() {
        pierceRemaining--;
        if (pierceRemaining <= 0) {
            this.isDead = true;
        }
    }
}
