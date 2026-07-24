package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class StandardZombie extends Zombie {
    protected double armorHealth;

    public StandardZombie(double health, double speed, int attackPower, int row, double startX, double initialArmor, double x, double y, double xSpeed, double ySpeed, ZombieType type) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, type);
        this.armorHealth = initialArmor;
    }

    public double takeArmorDamage(double amount) {
        if (hasArmor()) {
            this.armorHealth -= amount;
            if (this.armorHealth <= 0) {
                double overflow = -this.armorHealth;
                removeArmor();
                return overflow;
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

    public void enterEnrageMode() {
    }

    public double getArmorHealth() {
        return armorHealth;
    }

    public void setArmorHealth(double a) {
        this.armorHealth = a;
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        double newAmount = amount;

        // فیکس: آسیب‌های نادیده‌گیرنده زره مستقیماً به گوشت زامبی می‌خورند
        if (damageType != DamageType.BYPASS_ARMOR && hasArmor()) {
            newAmount = takeArmorDamage(amount);
        }

        this.health -= newAmount;
        if (this.health < 0) this.health = 0;
    }
}
