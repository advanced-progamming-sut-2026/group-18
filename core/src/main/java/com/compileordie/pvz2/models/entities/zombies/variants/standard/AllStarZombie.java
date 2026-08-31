package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class AllStarZombie extends StandardZombie {
    public static final int WAVE_COST = 1000;
    private final double chargeSpeedScale;
    private boolean isCharging;

    // --- برای انیمیشن «لحظه‌ی برخورد تکل» ---
    // قبلا stopCharge() همون تیکی که برخورد (با گیاه یا زامبی دیگه) اتفاق
    // می‌افتاد فورا isCharging رو false می‌کرد؛ چون View مستقیم بر اساس
    // isCharging() انیمیشن "tackle"/"run" رو انتخاب می‌کرد، همون لحظه که ضربه
    // می‌خورد (یعنی همون تیکی که کل دمیج - حتی دمیج زیاد - وارد می‌شد) بلافاصله
    // برمی‌گشت به walk عادی و انیمیشن ضربه اصلا فرصت پخش کامل پیدا نمی‌کرد.
    // این تایمر دقیقا هم‌الگو با HunterZombie.throwAnimRemaining عمل می‌کنه: از
    // لحظه‌ی برخورد به مدت TACKLE_IMPACT_DURATION ثانیه، View مجبوره انیمیشن
    // ضربه رو کامل (از صفر) پخش کنه، فارغ از این‌که isCharging دیگه false شده.
    public static final double TACKLE_IMPACT_DURATION = 1.0; // 🚩 با طول واقعی کلیپ ضربه هماهنگ کن
    private double tackleImpactRemaining = 0;

    public AllStarZombie(double health, double speed, int attackPower, int row, double startX
        , double chargeSpeedScale, double x, double y,
                         double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, 0, x, y, xSpeed, ySpeed, ZombieType.ALL_STAR);
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
        // 💥 دقیقا لحظه‌ی برخورد/پایان تکل - از اینجا انیمیشن ضربه شروع می‌شه.
        tackleImpactRemaining = TACKLE_IMPACT_DURATION;
    }

    @Override
    public void tick() {
        super.tick();
        if (tackleImpactRemaining > 0) {
            tackleImpactRemaining -= Constants.Game.TIME_COEFFICIENT;
            if (tackleImpactRemaining < 0) tackleImpactRemaining = 0;
        }
    }

    /** آیا الان باید انیمیشن کاملِ لحظه‌ی برخورد پخش بشه (به‌جای walk/run عادی)؟ */
    public boolean isTackleImpacting() {
        return tackleImpactRemaining > 0;
    }

    /** چند ثانیه از لحظه‌ی برخورد گذشته (۰ = همین الان ضربه خورده). */
    public double getTackleImpactElapsed() {
        return TACKLE_IMPACT_DURATION - tackleImpactRemaining;
    }

    // در آینده موقع بر خورد آل استار موقع محاسبه دمیج کافیست نوشته شود : damage + onPlantCollision()
    public double onPlantCollision() {
        if (isCharging) {
            stopCharge(); // تکل تمام می‌شود
            return 99999.0; // در واقع میزان دمیج برای اولین برخورد با گیاه
        }
        return 0;
    }

    // در آینده موقع بر خورد آل استار موقع محاسبه دمیج کافیست نوشته شود : damage + onPlantCollision()
    public double onZombieCollision() {
        if (isCharging) {
            return 99999.0;
        }
        return 0;
    }

    public void recalculateSpeed() {
        if (isCharging) {
            setXSpeed(getXSpeed() * chargeSpeedScale);
        } else {
            setXSpeed(getXSpeed() / chargeSpeedScale);
        }
    }

    public boolean isCharging() {
        return this.isCharging;
    }
}
