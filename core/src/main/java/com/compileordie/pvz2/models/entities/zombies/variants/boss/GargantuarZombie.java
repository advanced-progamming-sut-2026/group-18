package com.compileordie.pvz2.models.entities.zombies.variants.boss;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class GargantuarZombie extends Zombie {

    // فیلدهایی که قبلاً قرار بود در BossZombie باشند، الان مستقیم همینجا هستند:
    private int phase;
    private boolean hasTriggeredSpecialAction;

    // فیلدهای اختصاصی گارگانچوار (زامبی غول‌پیکر):
    private boolean impThrown;
    private final double healthThresholdToThrowImp;
    private final int impTargetColumn;

    public GargantuarZombie(int health, double speed, int attackPower, int row, double startX,
                            double x, double y, int xSpeed, int ySpeed) {
        // ارث‌بری مستقیم از کلاس اصلی Zombie بدون هیچ کلاس واسطی
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed);

        this.phase = 1; // شروع بازی در فاز ۱
        this.hasTriggeredSpecialAction = false;
        this.impThrown = false;

        // داک: هر وقت زامبی غول پیکر به نصف جان خودش برسد
        this.healthThresholdToThrowImp = health / 2.0;

        // داک: امپ را به ستون سوم (از چپ) می‌اندازد
        this.impTargetColumn = 3;
    }

    /**
     * مدیریت فازهای زامبی غول‌پیکر (مثلاً بعد از پرتاب امپ وارد فاز ۲ می‌شود)
     */
    public void changePhase() {
        this.phase++;
    }

    /**
     * فعال‌کننده اکشن ویژه که طبق داک همان پرتاب کردن Imp است
     */
    public void triggerSpecialAction() {
        if (!this.impThrown) {
            throwImp();
        }
    }

    /**
     * داک: imp را از پشت خود پرتاب می کند
     */
    public void throwImp() {
        this.impThrown = true;
        this.hasTriggeredSpecialAction = true;
        changePhase(); // تغییر فاز زامبی به فاز بعدی بعد از پرتاب
    }

    /**
     * داک: برخورد غول‌پیکر با گیاهان منجر به له شدن آن‌ها می‌شود.
     * @return true به سرویس بازی اعلام می‌کند که گیاه هدف باید فوراً حذف (له) شود.
     */
    public boolean smashPlant() {
        // غول‌پیکر اصلاً متوقف نمی‌شود تا گیاه را بجود (isEating تغییر نمی‌کند)، بلکه مستقیم آن را له می‌کند.
        return true;
    }

    /**
     * پیاده‌سازی متد انتزاعی takeDamage کلاس Zombie
     * سناریوی آسیب دیدن و چک کردنِ مانیتورینگ نصف شدن خون زامبی غول‌پیکر
     */
    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        this.health -= amount;
        if (this.health < 0) {
            this.health = 0;
        }

        // بررسی سناریوی رسیدن به نصف جان یا کمتر، برای پرتاب آنی و خودکار Imp
        if (!this.impThrown && this.health <= this.healthThresholdToThrowImp) {
            triggerSpecialAction();
        }
    }

    // گترها برای استفاده در لایه سرویس بازی جهت اسپان کردن Imp در ستون ۳ سطر فعلی
    public int getPhase() { return phase; }
    public boolean isHasTriggeredSpecialAction() { return hasTriggeredSpecialAction; }
    public boolean isImpThrown() { return impThrown; }
    public int getImpTargetColumn() { return impTargetColumn; }
}
