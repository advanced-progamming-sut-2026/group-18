package com.compileordie.pvz2.models.zombies.variants.standard;

import com.compileordie.pvz2.models.zombies.types.DamageType;
import com.compileordie.pvz2.models.zombies.variants.Zombie;

//Zs :
//BasicZombie
//ConeHeadZombie
//BucketHeadZombie
//KnightZombie
//BlockheadZombie
//NewspaperZombie
//ParasolZombie
//Imp
//ImpDragon
//AllStarZombie

public abstract class StandardZombie extends Zombie {
    protected int armorHealth;

    public StandardZombie(int health, double speed, int attackPower, int row, double startX, int initialArmor) {
        super(health, speed, attackPower, row, startX);
        this.armorHealth = initialArmor;
    }

    public int takeArmorDamage(int amount) {
        if (hasArmor()) {
            this.armorHealth -= amount;
            if (this.armorHealth <= 0) {
                removeArmor();
                return (-armorHealth);
            }
            return 0;
        }
        return amount;
    }

    public boolean hasArmor() {
        return this.armorHealth > 0;
    }

    public void removeArmor() {
        this.armorHealth = 0;
    }

    public void enterEnrageMode(){};


//    public void takeDamage(int amount, DamageType damageType) {
//        if (isDead()) return;
//        this.health -= amount;
//        if (this.health < 0) this.health = 0;
//    }

    @Override
    public void takeDamge(int amount, DamageType damageType){
        if (isDead()) return;
        int newAmount = amount;
        if (hasArmor()) {
            newAmount = takeArmorDamage(amount);
        }
        this.health -= newAmount;
        if (this.health < 0) this.health = 0;
        return;
    }
}
