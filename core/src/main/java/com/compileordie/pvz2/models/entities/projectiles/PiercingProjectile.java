package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class PiercingProjectile extends Projectile {
    private int pierceRemaining;

    // Default Cactus pierces 3. Fume-shroom can pass 999.
    public PiercingProjectile(double x, double y, double speed, int damage, int maxPierces) {
        super(x, y, speed, damage, DamageType.NORMAL);
        this.pierceRemaining = maxPierces;
    }

    @Override
    public void destroy() {
        pierceRemaining--;
        if (pierceRemaining <= 0) {
            this.isDead = true;
        }
        // If pierceRemaining > 0, the combat engine THINKS it destroyed it, but it keeps flying!
    }
}
