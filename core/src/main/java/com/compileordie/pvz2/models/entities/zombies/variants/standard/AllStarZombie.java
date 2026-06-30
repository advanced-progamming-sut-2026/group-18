package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class AllStarZombie extends StandardZombie {
    private boolean isCharging;
    private final double chargeSpeedScale;

    public AllStarZombie(int health, double speed, int attackPower, int row, double startX,
                         int initialArmor, double chargeSpeedScale, double x, double y,
                         int xSpeed, int ySpeed) {
        super(health, speed, attackPower, row, startX, initialArmor, x, y, xSpeed, ySpeed);
        this.chargeSpeedScale = chargeSpeedScale;
        startCharge();
    }

    public void startCharge() {
        this.isCharging = true;
        recalculateSpeed();
    }

    public void stopCharge() {
        this.isCharging = false;
        recalculateSpeed();
    }

    /**
     * این متد مستقیماً توسط سرویس بازی (مثلاً CombatService)
     * در لحظه برخورد فیزیکی این زامبی با یک گیاه صدا زده می‌شود.
     */
    public void onPlantCollision() {
            if (isCharging) {
                // اگر در حال تکل زدن باشد، گیاه اول را فوراً نابود می‌کند و تکلش تمام می‌شود
                destroyPlantInstantly();
                stopCharge();
            } else {
                // اگر شارژ تمام شده باشد، رفتار عادی زامبی (مثلاً جویدن) فعال می‌شود
                // این کار معمولاً با فرستادن سیگنال به سرویس برای تغییر وضعیت زامبی به isEating انجام می‌شود.
                startEating();
            }

    }

    @Override
    public void recalculateSpeed() {
        super.recalculateSpeed();
        if (isCharging) {
            this.currentSpeed *= this.chargeSpeedScale;
        }
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;
        int newAmount = amount;
        if (hasArmor()) {
            newAmount = takeArmorDamage(amount);
            if (!hasArmor()) {
                stopCharge();
            }
        }
        this.health -= newAmount;
        if (this.health < 0) this.health = 0;
    }


    // ** مرتبط با سرویس خاص **
    private void destroyPlantInstantly() {
        // لایه سرویس گیاه برخورد کرده را فوراً از نقشه حذف می‌کند
    }


    public boolean isCharging() {
        return this.isCharging;
    }
}
