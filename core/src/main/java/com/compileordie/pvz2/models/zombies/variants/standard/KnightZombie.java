package com.compileordie.pvz2.models.zombies.variants.standard;

import com.compileordie.pvz2.models.zombies.types.DamageType;

public class KnightZombie extends StandardZombie {
    private int helmetArmorHealth;
    private int shoulderArmorHealth;

    public KnightZombie(int health, double speed, int attackPower, int row, double startX, int helmet, int shoulder) {
        super(health, speed, attackPower, row, startX, helmet+shoulder); // initial = helmet + shoulder
        this.helmetArmorHealth = helmet; // 1100
        this.shoulderArmorHealth = shoulder;  // 500
    }

    public int damageHelmet(int amount) {
        this.helmetArmorHealth -= amount;
        takeArmorDamage(amount);
        if (this.helmetArmorHealth <= 0) {
            return (-helmetArmorHealth);
        }
        return 0;
    }

    public int damageShoulderArmor(int amount) {
        this.shoulderArmorHealth -= amount;
        takeArmorDamage(amount);
        if (this.shoulderArmorHealth <= 0) {
            removeArmor();
            return (-shoulderArmorHealth);
        }
        return 0;

    }

    public boolean isHelmetBroken() {
        return this.helmetArmorHealth <= 0;
    }

    public boolean isShoulderArmorBroken() {
        return this.shoulderArmorHealth <= 0;
    }

    @Override
    public void takeDamage(int amount, DamageType damageType){
        if (isDead()) return;
        int newAmount = amount;
        if (!isHelmetBroken()) {
            newAmount = damageHelmet(amount);
        }
        if (!isShoulderArmorBroken()){
            newAmount = damageShoulderArmor(newAmount);
        }
        this.health -= newAmount;
        if (this.health < 0) this.health = 0;
        return;
    }
}
