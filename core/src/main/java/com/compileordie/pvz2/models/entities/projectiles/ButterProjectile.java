package com.compileordie.pvz2.models.entities.projectiles;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class ButterProjectile extends LobbedProjectile {
    public ButterProjectile(double x, double y, double speed, int damage, double splashRadius) {
        // We can create a specific STUN type or just rely on LOBBER and apply effects separately
        super(x, y, speed, damage, splashRadius);
    }
}
