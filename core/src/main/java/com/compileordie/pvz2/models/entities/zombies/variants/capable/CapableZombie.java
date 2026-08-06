package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class CapableZombie extends Zombie {

    public CapableZombie(double health, double speed, int base_damage, int row, double startX,
                         double x, double y, double xSpeed, double ySpeed, ZombieType type) {
        super(health, speed, base_damage, row, startX, x, y, xSpeed, ySpeed, type);
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        if (damageType == DamageType.FIRE){
            this.removeFrozen();
        }
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }
}
