package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class AllStarZombie extends StandardZombie {
    private boolean isCharging;
    private final double chargeSpeedScale;

    public AllStarZombie(int health, double speed, int attackPower, int row, double startX,
                         int initialArmor, double chargeSpeedScale, double x, double y,
                         double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, initialArmor, x, y, xSpeed, ySpeed, ZombieType.ALL_STAR);
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
     * متد برخورد طبق ساختار سرویس‌محور بازنویسی شد.
     * این متد صرفاً وضعیت خود زامبی را مدیریت می‌کند.
     * سرویس بازی (CombatService) به صورت بیرونی متوجه شارژ زامبی شده و گیاه را نابود می‌کند.
     */
    public void onPlantCollision() {
        if (isCharging) {
            stopCharge(); // تکل تمام می‌شود
        } else {
            startEating(); // متوقف شدن برای جویدن عادی
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
        int oldArmor = this.armorHealth;

        super.takeDamage(amount, damageType);

        // داک بازی: اگر کلاه ورزشی فوتبالیست حین دویدن بشکند، دویدنش متوقف می‌شود
        if (oldArmor > 0 && !hasArmor()) {
            stopCharge();
        }
    }

    public boolean isCharging() { return this.isCharging; }
}
