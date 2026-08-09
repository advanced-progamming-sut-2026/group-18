package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class KnightZombie extends StandardZombie {
    public static final int WAVE_COST = 550;
    public double helmetArmorHealth;
    public double shoulderArmorHealth;

    public KnightZombie(double health,
                        double speed,
                        int attackPower,
                        int row,
                        double startX,
                        double helmet,
                        double shoulder,
                        double x,
                        double y,
                        double xSpeed,
                        double ySpeed) {
        super(health, speed, attackPower, row, startX, helmet + shoulder, x, y, xSpeed, ySpeed, ZombieType.KNIGHT);
        this.helmetArmorHealth = helmet;
        this.shoulderArmorHealth = shoulder;
    }

    @Override
    public double takeArmorDamage(double amount) {
        if (!hasArmor()) return amount;
        double remainingDamage = amount;

        if (!isHelmetBroken()) {
            if (remainingDamage <= helmetArmorHealth) {
                helmetArmorHealth -= remainingDamage;
                this.armorHealth -= remainingDamage;
                remainingDamage = 0;
                return 0;
            } else {
                remainingDamage -= helmetArmorHealth;
                this.armorHealth -= helmetArmorHealth;
                helmetArmorHealth = 0;
            }
        }

        if (!isShoulderArmorBroken()) {
            if (remainingDamage <= shoulderArmorHealth) {
                shoulderArmorHealth -= remainingDamage;
                this.armorHealth -= remainingDamage;
                remainingDamage = 0;
                return 0;
            } else {
                remainingDamage -= shoulderArmorHealth;
                this.armorHealth -= shoulderArmorHealth;
                shoulderArmorHealth = 0;
                removeArmor();
            }
        }

        return remainingDamage;
    }

    public boolean isHelmetBroken() {
        return this.helmetArmorHealth <= 0;
    }

    public boolean isShoulderArmorBroken() {
        return this.shoulderArmorHealth <= 0;
    }


    @Override
    public void takeDamage(double amount, DamageType damageType) {
        super.takeDamage(amount, damageType);
    }
}
