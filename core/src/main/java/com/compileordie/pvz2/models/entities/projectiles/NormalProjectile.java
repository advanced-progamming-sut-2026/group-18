package com.compileordie.pvz2.models.entities.projectiles;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class NormalProjectile extends Projectile {
    public NormalProjectile(double x, double y, double speed, int damage) {
        super(x, y, speed, damage, DamageType.NORMAL);
    }
}
