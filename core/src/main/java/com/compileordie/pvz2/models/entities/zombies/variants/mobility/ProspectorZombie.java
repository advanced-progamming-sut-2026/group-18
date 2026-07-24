package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.StatusEffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

/**
 * [زامبی اکتشاف‌گر - ProspectorZombie]
 * وضعیت یکپارچگی: ۱۰۰٪ منطبق بر کلاس MobilityZombie جدید شما و سازنده ۱۲ پارامتری آن.
 * * قوانین داک اعمال شده:
 * ۱. دینامیت پشت زامبی بعد از ۱۰ ثانیه منفجر می‌شود.
 * ۲. پس از انفجار، به انتهای سطر (سمت چپ زمین، نزدیک خانه بازیکن) پرتاب شده و خلاف جهت (راست) حرکت می‌کند.
 * ۳. پرتابه‌ها و افکت‌های یخی دینامیت او را برای همیشه خاموش می‌کنند.
 */
public class ProspectorZombie extends MobilityZombie {
    private boolean dynamiteActive;
    private double dynamiteTimer;
    private final double timeToExplode = 10.0; // داک: انفجار بعد از ۱۰ ثانیه
    private boolean isReversedDirection;
    private final double homeColumnX = 50.0; // مختصات ابتدای سطر سمت چپ (نزدیک خانه بازیکن)

    public ProspectorZombie(int health, double speed, int attackPower, int row, double startX,
                            int initialArmor, double delta, double x, double y, double xSpeed, double ySpeed,
                            double underwaterSpeedModifier) {
        // فراخوانی دقیق سازنده ۱۲ پارامتری کلاس MobilityZombie شما
        super(health, speed, attackPower, row, startX, initialArmor, delta, x, y, xSpeed, ySpeed, underwaterSpeedModifier, ZombieType.PROSPECTOR_ZOMBIE);

        this.dynamiteActive = true;
        this.dynamiteTimer = 0.0;
        this.isReversedDirection = false;
    }

    @Override
    public void tick() {
        // داک: در صورت برخورد تیر یخی یا وجود افکت سرمایی، دینامیت خاموش می‌شود
        if (this.dynamiteActive && (hasEffect(StatusEffectType.FROZEN) || hasEffect(StatusEffectType.CHILLED))) {
            this.dynamiteActive = false;
        }

        // فراخوانی تیک والد (که متد updateMovementState شما را برای تنظیم سرعت بر اساس WALKING/EATING اجرا می‌کند)
        super.tick();
        if (isDead()) return;

        // منطق شمارش معکوس دینامیت با استفاده از فیلد delta کلاس MobilityZombie شما
        if (this.dynamiteActive && !hasEffect(StatusEffectType.FROZEN)) {
            this.dynamiteTimer += this.delta;
            if (this.dynamiteTimer >= this.timeToExplode) {
                triggerExplosionAndFly();
            }
        }
    }

    private void triggerExplosionAndFly() {
        this.dynamiteActive = false;
        this.isReversedDirection = true;

        // تغییر وضعیت به پرواز/پرتاب جهت هماهنگی با لایه گرافیک یا رندر بازی
        changeMovementState(MovementState.FLYING);

        // داک: پرتاب به انتهای سطر (کنار خانه بازیکن)
        setX(this.homeColumnX);

        // بازگشت به وضعیت راه رفتن اما این بار در جهت معکوس
        changeMovementState(MovementState.WALKING);
    }

    /**
     * اورراید کردن حرکت برای پیاده‌سازی جابه‌جایی معکوس (از چپ به راست) پس از انفجار
     */
    @Override
    public void move(int ticks) {
        float dt = ticks * Constants.Game.TIME_COEFFICIENT;
        setXSpeed(this.currentSpeed);

        if (this.isReversedDirection) {
            // داک: خلاف جهت باقی زامبی‌ها حرکت می‌کند (پیشروی به سمت راست با سرعت فعلی currentSpeed)
            setX(getX() + getXSpeed() * dt);
        } else {
            // حرکت عادی تمام زامبی‌ها به سمت چپ زمین قبل از انفجار دینامیت
            setX(getX() - getXSpeed() * dt);
        }
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        // داک: تیر یخی دینامیت را خاموش می‌کند
        if (damageType == DamageType.ICE) {
            this.dynamiteActive = false;
        }

        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    public boolean isDynamiteActive() {
        return this.dynamiteActive;
    }

    public boolean isReversedDirection() {
        return this.isReversedDirection;
    }
}
