package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class IceProjectile extends Projectile {
    public IceProjectile(double x, double y, double speed, int damage) {
        super(x, y, speed, damage, DamageType.ICE);
    }
}
