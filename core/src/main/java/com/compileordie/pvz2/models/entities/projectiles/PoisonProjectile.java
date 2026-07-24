package com.compileordie.pvz2.models.entities.projectiles;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class PoisonProjectile extends Projectile {
    public PoisonProjectile(double x, double y, double speed, int damage) {
        super(x, y, speed, damage, DamageType.POISON);
    }
}
