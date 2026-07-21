package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class NewspaperZombie extends StandardZombie {
    private boolean isEnraged;
    public static final int waveCost = 700;

    public NewspaperZombie(double health, double speed, int attackPower, int row, double startX, double initialArmor, double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, initialArmor, x, y, xSpeed, ySpeed, ZombieType.NEWSPAPER_ZOMBIE);
        this.isEnraged = false;
    }

    @Override
    public double takeArmorDamage(double amount) {
        if (hasArmor()) {
            this.armorHealth -= amount;
            if (this.armorHealth <= 0) {
                double overflow = -this.armorHealth;
                removeArmor();
                if (!isEnraged) {
                    enterEnrageMode();
                }
                return overflow;
            }
            return 0;
        }
        return amount;
    }

    @Override
    public void enterEnrageMode() {
        this.isEnraged = true;
        setXSpeed(0.22);
        setAttackPower(200);
    }

    public boolean isEnraged() { return isEnraged; }
}
