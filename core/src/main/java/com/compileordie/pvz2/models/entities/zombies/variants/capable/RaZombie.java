package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class RaZombie extends CapableZombie {
    private int stolenSunCount;

    public RaZombie(int health, double speed, int attackPower, int row, double startX, double abilityCooldown, int abilityRange, double delta, double x, double y, int xSpeed, int ySpeed) {
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);
        // پیشفرض پیشنهادی : هر 4 ثانیه یکبار خورشید به سمت خودش بکشد : abilityCooldown

        this.stolenSunCount = 0;
        setDelta(delta);
    }

    // ** مرتبط با سرویس خاص **
    @Override
    public void useAbility() {
        // لایه سرویس بازی خورشیدهای روی زمین را برمی‌دارد و متد stealSun را صدا می‌زند
        resetCooldown();
    }

    public void stealSun(int amount) {
        this.stolenSunCount += amount;
    }

    public int getStolenSuns() {
        return this.stolenSunCount;
    }

    // **مرتبط با سرویس خاص**
    @Override
    public void handleDeath() {
        super.handleDeath();
        // در لایه Manager، مقدار خورشیدهای خروجی این متد مستقیم به بالانس بازیکن اضافه می‌شود
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }
}
