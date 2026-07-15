package com.compileordie.pvz2.models.entities.zombies.services.movement;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext;

public interface ZombieMovementService {

    /**
     * پردازش حرکت تمام زامبی‌های زنده بر اساس وضعیت جاری، سرعت و جهت
     */
    void moveAll(ZombieTickContext context);

    /**
     * مخصوص زامبی غواص (Snorkel)؛ سوئیچ بین حالت زیر آب و روی آب هنگام رسیدن به کاشی آب
     */
    void handleSnorkelDivingLogic(Zombie snorkelZombie, ZombieTickContext context);

    /**
     * مخصوص زامبی‌های پرنده (DodoRider)؛ مدیریت وضعیت پرواز و عبور از روی گیاهان زمینی
     */
    void handleFlyerOverObstacles(Zombie flyerZombie, ZombieTickContext context);

    /**
     * مخصوص زامبی معدن‌چی (Prospector)؛ پیاده‌روی معکوس پس از پرتاب شدن به انتهای سطر
     */
    void handleMinerReverseWalk(Zombie minerZombie, ZombieTickContext context);

    /**
     * مخصوص زامبی پیانیست؛ جابه‌جایی نرم زامبی‌های هم‌سطر بین لِین‌ها
     */
    void handleLaneSwitchTransition(Zombie zombie, int targetRow, ZombieTickContext context);

    /**
     * اعمال فیزیک حرکت پرتابی سهمی‌شکل برای ایمپ (Imp) پرتاب شده توسط غول
     */
    void handleThrownImpTrajectory(Zombie impZombie, ZombieTickContext context);

    /**
     * اعمال نیروی عقب‌رانی (Knockback) روی زامبی‌ها به سمت راست زمین
     */
    void applyKnockbackForce(Zombie zombie, double distance, ZombieTickContext context);

    /**
     * بررسی عبور زامبی‌ها از مرز چپ زمین و مدیریت باخت بازی
     */
    void checkLawnMowersAndBreach(ZombieTickContext context);
}
