package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class KnightZombie extends StandardZombie {
    private double helmetArmorHealth;
    private double shoulderArmorHealth;
    public static final int waveCost = 550;

    public KnightZombie(double health, double speed, int attackPower, int row, double startX, double helmet, double shoulder, double x, double y, double xSpeed, double ySpeed) {
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

    public boolean isHelmetBroken() { return this.helmetArmorHealth <= 0; }
    public boolean isShoulderArmorBroken() { return this.shoulderArmorHealth <= 0; }

    public void mushroomAbsorption() {
        this.helmetArmorHealth = 0;

        // شک دارم اینا رو هم جذب می کنه یا ن
//        this.shoulderArmorHealth = 0;
//        setArmorHealth(0);


        // اینجا باید گیاه به محض رویت زامبی در نزدیکی اش این متد را فراخوانی کند
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        super.takeDamage(amount, damageType);
    }
}
