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
        // داک: شعاع دید برای شروع توانایی برابر با 4 خانه است (abilityRange = 4)
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);
        this.isStealing = false;
        this.stealTimer = 0;
        this.perSecondTimer = 0;
        this.totalStolenSuns = 0;
        setDelta(delta);
    }

    @Override
    public void useAbility() {
        // لایه سرویس با دیدن گیاه در شعاع 4 خانه‌ای این متد را صدا می‌زند
        if (!isStealing) {
            this.isStealing = true;
            this.stealTimer = 0;
            this.perSecondTimer = 0;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        // مدیریت فرآیند دزدیدن خورشید و شلیک لیزر با زمان‌بندی واقعی دلتا
        if (isStealing) {
            this.stealTimer += this.delta;
            this.perSecondTimer += this.delta;

            // داک: هر ثانیه 25 خورشید میدزدد
            if (this.perSecondTimer >= 1.0) {
                stealSunFromPlayerBalance(25);
                this.totalStolenSuns += 25;
                this.perSecondTimer -= 1.0;
            }

            // داک: برای 5 ثانیه این کار را ادامه میدهد و بعد آن لیزر شلیک میکند
            if (this.stealTimer >= 5.0) {
                triggerLaserBlast();
                this.isStealing = false;
                this.stealTimer = 0;
                this.perSecondTimer = 0;
                resetCooldown();
            }
        }
    }

    @Override
    public void handleDeath() {
        // داک: پس از کشته شدن، نیمی از خورشید های دزدیده شده را می اندازد
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

    // ** مرتبط با سرویس خاص **
    private void stealSunFromPlayerBalance(int amount) {
        // لایه سرویس مقدار خورشید را مستقیم از موجودی خورشیدهای بازیکن کسر می‌کند
    }

    // ** مرتبط با سرویس خاص **
    private void triggerLaserBlast() {
        // لایه سرویس بر اساس سطر زامبی، تمام گیاهان موجود در 4 خانه روبرویی را نابود می‌کند
    }

    // ** مرتبط با سرویس خاص **
    private void spawnDroppedSuns(int amount) {
        // لایه منیجر این تعداد خورشید را به عنوان لوت حاصل از مرگ زامبی روی زمین بازی تولید می‌کند
    }

    public boolean isStealing() {
        return this.isStealing;
    }

    public int getTotalStolenSuns() {
        return this.totalStolenSuns;
    }
}
