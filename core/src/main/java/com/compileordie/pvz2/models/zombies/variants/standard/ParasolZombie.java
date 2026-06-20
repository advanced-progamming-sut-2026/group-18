package com.compileordie.pvz2.models.zombies.variants.standard;

import com.compileordie.pvz2.models.zombies.types.DamageType;

public class ParasolZombie extends StandardZombie {

    public ParasolZombie(int health, double speed, int attackPower, int row, double startX, int initialArmor) {
        super(health, speed, attackPower, row, startX, initialArmor);
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
