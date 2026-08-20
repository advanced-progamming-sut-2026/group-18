package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class TombraiserZombie extends SummonerZombie {
    public static final int WAVE_COST = 300;
    private boolean spawnTomb = false;

    // --- برای انیمیشن "power" موقع ساختن قبر (۳ ثانیه، سپس خودکار به حالت عادی) ---
    public static final double POWER_ANIM_DURATION = 3.0;
    private double powerAnimRemaining = 0;
    // 👈 طبق درخواست: اول انیمیشن power باید کامل پخش بشه، بعد قبر واقعا ساخته
    // بشه - نه هم‌زمان. این فلگ لحظه‌ی summon() رو یادداشت می‌کنه؛ spawnTomb
    // (که ZombieManager واقعا می‌خونه) فقط وقتی powerAnimRemaining به صفر برسه
    // true می‌شه، نه همون لحظه‌ی شروع انیمیشن.
    private boolean pendingSpawnAfterAnim = false;

    public TombraiserZombie(double health, double speed, int attackPower, int row, double startX,
                            double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.TOMBRAISER, 10.0);
    }

    @Override
    public void move(int ticks) {
        // 🪦 حین پخش انیمیشن power، زامبی قبرساز باید کاملا بایسته تا انیمیشن
        // کامل دیده بشه، درست مثل را/هانتر/اختاپوس.
        if (isPoweringUp()) return;
        super.move(ticks);
    }

    @Override
    public void tick() {
        super.tick(); // این summonTimer رو هندل می‌کنه و در لحظه‌ی مناسب summon() رو صدا می‌زنه
        if (powerAnimRemaining > 0) {
            powerAnimRemaining -= Constants.Game.TIME_COEFFICIENT;
            if (powerAnimRemaining <= 0) {
                powerAnimRemaining = 0;
                // انیمیشن power تازه همین لحظه تموم شد -> حالا واقعا قبر بساز
                if (pendingSpawnAfterAnim) {
                    pendingSpawnAfterAnim = false;
                    spawnTomb = true;
                }
            }
        }
    }

    @Override
    public void summon() {
        // 👈 دیگه اینجا spawnTomb مستقیم true نمی‌شه؛ فقط انیمیشن power شروع
        // می‌شه و «درخواست ساخت قبر» یادداشت می‌شه - قبر واقعی بعد از تمومِ ۳
        // ثانیه‌ی انیمیشن (تو tick() بالا) ساخته می‌شه.
        pendingSpawnAfterAnim = true;
        powerAnimRemaining = POWER_ANIM_DURATION;
    }

    public boolean shouldWeSpawnTomb() {
        return spawnTomb;
    }

    public void stopSpawnTomb() {
        this.spawnTomb = false;
    }

    /** آیا الان باید انیمیشن "power" پخش بشه (به‌جای walk عادی)؟ */
    public boolean isPoweringUp() {
        return powerAnimRemaining > 0;
    }

    /** چند ثانیه از شروع انیمیشن power گذشته (۰ = همین الان شروع شده). */
    public double getPowerAnimElapsed() {
        return POWER_ANIM_DURATION - powerAnimRemaining;
    }
}
