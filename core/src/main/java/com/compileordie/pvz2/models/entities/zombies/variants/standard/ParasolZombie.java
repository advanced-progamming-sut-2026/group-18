package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class ParasolZombie extends StandardZombie {
    public static final int waveCost = 200;
    public ParasolZombie(double health, double speed, int attackPower, int row, double startX, double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, 0, x, y, xSpeed, ySpeed, ZombieType.PARASOL_ZOMBIE);
    }

    public boolean isRepelled(DamageType type) {
        return type == DamageType.LOBBER;
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead() || isRepelled(damageType)) return;
        super.takeDamage(amount, damageType);
    }
}
