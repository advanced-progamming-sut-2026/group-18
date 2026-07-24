package com.compileordie.pvz2.models.entities.projectiles;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class FireProjectile extends Projectile {
    public FireProjectile(double x, double y, double speed, int damage) {
        super(x, y, speed, damage, DamageType.FIRE);
    }
}
