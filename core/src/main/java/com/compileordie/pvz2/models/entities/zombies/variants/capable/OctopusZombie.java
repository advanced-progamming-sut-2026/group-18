package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class OctopusZombie extends CapableZombie {
    public static final int WAVE_COST = 900;
    public static final double ABILITY_RANGE = Constants.Game.TILE_WIDTH * 2.2;

    // --- برای انیمیشن "toss" موقع پرتاب اختاپوس (۳ ثانیه، سپس خودکار به حالت عادی) ---
    public static final double TOSS_ANIM_DURATION = 3.0;
    private double tossAnimRemaining = 0;

    public OctopusZombie(double health, double speed, int attackPower, int row, double startX,
                         double x, double y,
                         double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.OCTOPUS_ZOMBIE);
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;
        if (tossAnimRemaining > 0) {
            tossAnimRemaining -= Constants.Game.TIME_COEFFICIENT;
            if (tossAnimRemaining < 0) tossAnimRemaining = 0;
        }
    }

    // 🐙 طبق درخواست: حین انیمیشن "toss" (پرتاب اختاپوس)، زامبی باید کاملا
    // بایسته و راه نره تا انیمیشن کامل پخش بشه، بعد دوباره راه بیفته - دقیقا
    // مثل move() هانتر/را. قبلا این override اصلا وجود نداشت، پس اختاپوس هیچ‌وقت
    // موقع toss متوقف نمی‌شد.
    @Override
    public void move(int ticks) {
        if (isTossing()) return;
        super.move(ticks);
    }

    /**
     * منطق واقعیه: دقیقا لحظه‌ای صدا زده می‌شه که اختاپوس واقعا پرتاب می‌کنه
     * (نگاه کن به ZombieManager.processSpecialCombatAbilities، جایی که
     * p.applyOctopus(...) صدا زده می‌شه).
     */
    public void startTossAnimation() {
        tossAnimRemaining = TOSS_ANIM_DURATION;
    }

    /** آیا الان باید انیمیشن "toss" پخش بشه (به‌جای walk عادی)؟ */
    public boolean isTossing() {
        return tossAnimRemaining > 0;
    }

    /** چند ثانیه از شروع انیمیشن toss گذشته (۰ = همین الان شروع شده). */
    public double getTossAnimElapsed() {
        return TOSS_ANIM_DURATION - tossAnimRemaining;
    }

}
