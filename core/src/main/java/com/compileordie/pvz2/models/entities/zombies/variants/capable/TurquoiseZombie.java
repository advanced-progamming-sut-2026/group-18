package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class TurquoiseZombie extends CapableZombie {
    private boolean isStealing;
    private double stealTimer;
    private double perSecondTimer;
    private int totalStolenSuns;

    public TurquoiseZombie(int health, double speed, int attackPower, int row, double startX,
                           double abilityCooldown, int abilityRange, double delta, double x, double y,
                           int xSpeed, int ySpeed) {
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);
        this.isStealing = false;
        this.stealTimer = 0;
        this.perSecondTimer = 0;
        this.totalStolenSuns = 0;
    }

    @Override
    public void useAbility() {
        // سرویس ابیلیتی با دیدن گیاه در شعاع دید این متد را یک‌بار صدا می‌زند
        if (!isStealing) {
            this.isStealing = true;
            this.stealTimer = 0;
            this.perSecondTimer = 0;
            resetCooldown(); // فیکس: بلافاصله کول‌داون قفل می‌شود تا متد در تیک‌های بعدی مجدد هرز نرود
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        // منطق دزدیدن در لایه تیک به صورت ایزوله اجرا می‌شود
        if (this.isStealing) {
            this.stealTimer += this.delta;
            this.perSecondTimer += this.delta;

            if (this.perSecondTimer >= 1.0) {
                stealSunFromPlayerBalance(1);
                this.totalStolenSuns += 1;
                this.perSecondTimer -= 1.0;
            }

            if (this.stealTimer >= 5.0) {
                triggerLaserBlast();
                this.isStealing = false;
                // کول‌داون قبلاً قفل شده و در حال کم شدن است، نیازی به دستکاری مجدد نیست
            }
        }
    }

    private void stealSunFromPlayerBalance(int amount) {}
    private void triggerLaserBlast() {}
    private void spawnDroppedSuns(int amount) {}

    @Override
    public void handleDeath() {
        int sunsToDrop = this.totalStolenSuns / 2;
        spawnDroppedSuns(sunsToDrop);
        super.handleDeath();
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }
}
