package com.compileordie.pvz2.models.entities.projectiles;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class LobbedProjectile extends Projectile {
    private final double splashRadius;

    public LobbedProjectile(double x, double y, double speed, int damage, double splashRadius) {
        super(x, y, speed, damage, DamageType.LOBBER);
        this.splashRadius = splashRadius;
    }

    public double getSplashRadius() { return splashRadius; }
}
