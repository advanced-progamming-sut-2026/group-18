package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class NewspaperZombie extends StandardZombie {
    public static final int WAVE_COST = 700;
    private boolean isEnraged;

    public NewspaperZombie(double health,
                           double speed,
                           int attackPower,
                           int row,
                           double startX,
                           double initialArmor,
                           double x,
                           double y,
                           double xSpeed,
                           double ySpeed) {
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
        // 👈 setXSpeed ساده کافی نیست (تیک بعدی Zombie.tick() دوباره سرعت رو از
        // replacedSpeed بازیابی می‌کرد و enrage عملا هیچ‌وقت دیده نمی‌شد). با
        // setPermanentXSpeed هم سرعت فعلی و هم مرجع داخلی که هر فریم ازش
        // بازخوانی می‌شه با هم عوض می‌شن.
        setPermanentXSpeed(0.5);
        setAttackPower(300);
    }

    public boolean isEnraged() {
        return isEnraged;
    }
}
