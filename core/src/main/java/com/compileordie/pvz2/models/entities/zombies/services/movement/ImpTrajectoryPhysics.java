package com.compileordie.pvz2.models.entities.zombies.services.movement;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.ImpZombie;

/**
 * محاسبات ریاضی و سینماتیک حرکت پرواز سهمی‌شکل زامبی ایمپ در فضا.
 */
public class ImpTrajectoryPhysics {

    /**
     * به‌روزرسانی گام‌به‌گام موقعیت افقی و عمودی ایمپ معلق در فضا.
     */
    public static void updateTrajectory(ImpZombie imp, double dt) {
        // ۱. بررسی دفاعی: اگر ایمپ روی زمین است، نیازی به فیزیک پروازی ندارد
        if (!imp.isCurrentlyAirborne()) {
            return;
        }

        // ۲. محاسبات حرکت افقی (محور X - حرکت با سرعت ثابت به سمت چپ صفحه)
        double currentX = imp.getX();
        double nextX = currentX - (imp.getLaunchSpeedX() * dt);

        // ۳. محاسبات حرکت عمودی (محور Y - شتاب جاذبه زمین)
        double gravity = Constants.Imp.LAUNCH_GRAVITY;
        double currentSpeedY = imp.getLaunchSpeedY();
        double nextSpeedY = currentSpeedY - (gravity * dt);

        // فرمول مکان-زمان حرکت شتاب‌دار: y = y0 + v0*dt - 1/2*g*dt^2
        double currentY = imp.getY();
        double nextY = currentY + (currentSpeedY * dt) - (0.5 * gravity * dt * dt);

        // ۴. اعمال موقعیت‌ها و سرعت‌های جدید
        imp.setX(nextX);
        imp.setY(nextY);
        imp.setLaunchSpeedY(nextSpeedY);

        // ۵. مدیریت فرود ایمپ روی چمنزار
        double groundLevel = imp.getOriginalGroundY();
        if (nextY <= groundLevel + Constants.Imp.LANDING_TOLERANCE) {

            // تثبیت ارتفاع روی زمین جهت جلوگیری از افتادن زیر نقشه
            imp.setY(groundLevel);

            // متوقف کردن بردار سرعت‌های پرتابی
            imp.setLaunchSpeedY(0.0);
            imp.setLaunchSpeedX(0.0);

            // خروج از وضعیت پرواز؛ از این فریم به بعد ایمپ مانند زامبی عادی قدم می‌زند
            imp.setAirborneState(false);
        }
    }
}
