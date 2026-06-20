package com.compileordie.pvz2.models.zombies.variants.standard;

import com.compileordie.pvz2.models.zombies.types.DamageType;

public class AllStarZombie extends StandardZombie {
    private boolean isCharging;
    private double chargeSpeedScale;
    private final int damage;

    public AllStarZombie(int health, double speed, int attackPower, int row, double startX, int initialArmor, int chargeSpeedScale) {
        super(health, speed, attackPower, row, startX, initialArmor);
        this.isCharging = false;
        this.chargeSpeedScale = chargeSpeedScale;
        this.damage = attackPower;
//        this.chargeSpeedScale = 3.0; ضریب سرعت تکل
        startCharge();
    }

    public void startCharge() {
        this.attackPower = 9999999;
        this.isCharging = true;
        this.currentSpeed = this.movementSpeed * chargeSpeedScale;
    }

    public void stopCharge() {
        this.attackPower = damage;
        this.isCharging = false;
        this.currentSpeed = this.movementSpeed;
    }

    @Override
    public void recalculateSpeed() {
        // ابتدا محاسبه سرعت بر اساس افکت‌های سرما و کندی در کلاس والد
        super.recalculateSpeed();

        // اگر در حال شارژ بود، ضریب سرعت تکل نیز روی سرعت فعلی ضرب می‌شود
        if (isCharging) {
            this.currentSpeed *= this.chargeSpeedScale;
        }
    }

    @Override
    public void takeDamage(int amount, DamageType damageType){
        if (isDead()) return;
        int newAmount = amount;
        if (hasArmor()) {
            newAmount = takeArmorDamage(amount);
            if (!hasArmor()) stopCharge();
        }
        this.health -= newAmount;
        if (this.health < 0) this.health = 0;
        return;
    }
}
