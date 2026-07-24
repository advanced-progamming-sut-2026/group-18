package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class ImpDragon extends StandardZombie {
    public static final int WAVE_COST = 150;

    public ImpDragon(double health, double speed, int attackPower, int row, double startX,
                     double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, 0, x, y, xSpeed, ySpeed, ZombieType.IMP_DRAGON);
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        if (damageType == DamageType.FIRE) {
            return;
        }
        super.takeDamage(amount, damageType);
    }
}
