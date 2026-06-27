package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;

public class ProspectorZombie extends MobilityZombie {

    private boolean dynamiteActive;
    private double dynamiteTimer;
    private final double timeToExplode; // طبق داک ۱۰ ثانیه (کاملاً پارامتریک از سازنده)
    private boolean isReversedDirection;
    private final double lastColumnX; // مختصات انتهای سطر (کنار خانه بازیکن) برای پرتاب شدن

    public ProspectorZombie(int health, double speed, int attackPower, int row, double startX,
                            int initialArmor, double delta, double x, double y, int xSpeed, int ySpeed,
                            double underwaterSpeedModifier, double timeToExplode, double lastColumnX) {
        super(health, speed, attackPower, row, startX, initialArmor, delta, x, y, xSpeed, ySpeed, underwaterSpeedModifier);
        setDelta(delta);

        this.dynamiteActive = true;
        this.dynamiteTimer = 0.0;
        this.timeToExplode = timeToExplode;
        this.isReversedDirection = false;
        this.lastColumnX = lastColumnX;
    }

    @Override
    public void tick() {
        // ۱. بررسی افکت‌های یخ‌زدگی برای خاموش کردن دینامیت (قبل از آپدیت تیک والد)
        // داک: در صورتی که تیری یخی به آنها بخورد، دینامیتشان خاموش می‌شود
        if (this.dynamiteActive && (hasEffect(EffectType.FREEZE) || hasEffect(EffectType.CHILLED))) {
            this.dynamiteActive = false;
        }

        // ۲. آپدیت افکت‌ها و رفتارهای کلاس والد
        super.tick();
        if (isDead()) return;

        // ۳. مدیریت زمان‌بندی انفجار دینامیت با دلتا
        if (this.dynamiteActive) {
            this.dynamiteTimer += this.delta;
            if (this.dynamiteTimer >= this.timeToExplode) {
                explodeAndTeleport();
            }
        }
    }

    /**
     * منطق انفجار دینامیت و پرتاب شدن به انتهای سطر
     */
    private void explodeAndTeleport() {
        this.dynamiteActive = false;
        this.isReversedDirection = true;

        // داک: دینامیت به انتهای سطر (کنار خانه) پرتاب می‌شد
        this.positionX = this.lastColumnX;

        // تغییر وضعیت حرکت برای شروع مجدد جابه‌جایی رو به جلو (به سمت راست)
        changeMovementState(MovementState.WALKING);
    }

    /**
     * بازنویسی متد حرکت کلاس ریشه Zombie
     * داک: خلاف جهت باقی زامبی‌ها شروع به حرکت و خوردن گیاه‌ها می‌کند
     */
    @Override
    public void move() {
        if (this.isReversedDirection) {
            // حرکت به سمت راست (خلاف جهت بقیه زامبی‌ها)
            this.positionX += this.currentSpeed;
        } else {
            // حرکت عادی به سمت چپ قبل از انفجار دینامیت
            this.positionX -= this.currentSpeed;
        }
    }

    /**
     * مدیریت رویداد برخورد فیزیکی با گیاه از طریق سرویس بازی
     */
    public void onPlantCollision() {
        startEating(); // فعال کردن ترمز فیزیکی در کلاس ریشه Zombie (isEating = true)
        changeMovementState(MovementState.EATING);
    }

    /**
     * رویداد پاک شدن مسیر بعد از خوردن گیاه جلو زامبی
     */
    public void onPathCleared() {
        if (this.movementState == MovementState.EATING) {
            stopEating(); // آزاد کردن ترمز فیزیکی کلاس ریشه Zombie (isEating = false)
            changeMovementState(MovementState.WALKING);
        }
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    // گتر برای لایه سرویس جهت تشخیص جهت حرکت معکوس زامبی اکتشاف‌گر
    public boolean isReversedDirection() {
        return this.isReversedDirection;
    }
}
