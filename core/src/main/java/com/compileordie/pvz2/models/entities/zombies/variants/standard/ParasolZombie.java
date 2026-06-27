package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class ParasolZombie extends StandardZombie {

    public ParasolZombie(int health, double speed, int attackPower, int row, double startX, int initialArmor, double x, double y, int xSpeed, int ySpeed) {
        super(health, speed, attackPower, row, startX, initialArmor, x, y, xSpeed, ySpeed);
    }

    public boolean isReplaied(DamageType type) {
        if (type==DamageType.LOBBER) return true;
        return false;
    }

    @Override
    public void takeDamage(int amount, DamageType damageType){
        if (isDead() || isReplaied(damageType)) return;
        int newAmount = amount;
        if (hasArmor()) {
            newAmount = takeArmorDamage(amount);
        }
        this.health -= newAmount;
        if (this.health < 0) this.health = 0;
        return;
    }

}
