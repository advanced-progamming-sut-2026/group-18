package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class ImpZombie extends StandardZombie {

    // فیلدهای جدید برای پشتیبانی از منطق پرواز/پرتاب سهمی‌شکل توسط غول (Gargantuar)
    private boolean isAirborne;           // آیا در حال حاضر معلق در آسمان و پرواز است؟
    private double launchSpeedX;          // سرعت افقی پرتاب
    private double launchSpeedY;          // سرعت عمودی اولیه پرتاب
    private double originalGroundY;       // مختصات Y سطح زمین (برای تشخیص فرود دقیق روی زمین)

    public ImpZombie(int health, double speed, int attackPower, int row, double startX,
                     double x, double y, double xSpeed, double ySpeed) {
        // پاس دادن پارامترها به StandardZombie (مقدار زره اولیه برای امپ معمولی 0 است)
        super(health, speed, attackPower, row, startX, 0, x, y, xSpeed, ySpeed, ZombieType.IMP);

        // وضعیت اولیه: ایمپ هنگام ورود به بازی عادی راه می‌رود و معلق در هوا نیست
        this.isAirborne = false;
        this.launchSpeedX = 0.0;
        this.launchSpeedY = 0.0;
        this.originalGroundY = y; // ذخیره مختصات خط زمین برای فرود آمدن در همان لاین
    }

    /**
     * متدی برای آماده‌سازی و پرتاب کردن ایمپ در هوا (توسط زامبی غول‌پیکر)
     */
    public void launch(double speedX, double speedY) {
        this.isAirborne = true;
        this.launchSpeedX = speedX;
        this.launchSpeedY = speedY;
        this.originalGroundY = getY(); // ذخیره ارتفاع پایه لاین فعلی
    }

    // --- Getter ها و Setter های مورد نیاز برای ImpTrajectoryPhysics ---

    public boolean isCurrentlyAirborne() {
        return isAirborne;
    }

    public void setAirborneState(boolean airborne) {
        this.isAirborne = airborne;
    }

    public double getLaunchSpeedX() {
        return launchSpeedX;
    }

    public void setLaunchSpeedX(double launchSpeedX) {
        this.launchSpeedX = launchSpeedX;
    }

    public double getLaunchSpeedY() {
        return launchSpeedY;
    }

    public void setLaunchSpeedY(double launchSpeedY) {
        this.launchSpeedY = launchSpeedY;
    }

    public double getOriginalGroundY() {
        return originalGroundY;
    }

    public void setOriginalGroundY(double originalGroundY) {
        this.originalGroundY = originalGroundY;
    }
}
