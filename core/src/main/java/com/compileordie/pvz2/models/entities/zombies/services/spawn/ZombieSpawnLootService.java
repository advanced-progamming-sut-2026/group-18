package com.compileordie.pvz2.models.entities.zombies.services.spawn;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext;

public interface ZombieSpawnLootService {

    /**
     * خالی کردن صف زامبی‌های جدید (SpawnQueue) به درون لیست اصلی زامبی‌های فعال
     * برای جلوگیری از خطای همزمانی جاوا (ConcurrentModificationException) وسط فریم
     */
    void flushSpawnQueue(ZombieTickContext context);

    /**
     * تولید و افزودن زامبی‌های مینیون به صف اسپاون بر اساس تریگرهای توصیف شده در داک:
     * - زامبی غول (Gargantuar): پرتاب ایمپ (Imp) به اواسط زمین به محض کم شدن خونش از یک حد مشخص.
     * - زامبی دبه‌ای (Barrel) یا توپخانه: رها کردن ۳ زامبی ایمپ پس از انفجار/نابودی دبه.
     * - زامبی مرغ‌دار (Chicken Wrangler): رها کردن گله‌ای از جوجه‌های بسیار سریع به محض آسیب دیدن زره/طناب.
     */
    void spawnSubMinionsOnTrigger(Zombie parentZombie, ZombieTickContext context);

    /**
     * مدیریت پاک‌سازی زامبی پس از مرگ، رها کردن سکه/پرچم روی زمین
     * و مهم‌تر از همه: پس دادن ۱۰۰٪ خورشیدهای دزدیده شده توسط زامبی رع به زمین بازی به محض مرگش.
     */
    void handleZombieDeathAndLoot(ZombieTickContext context);
}
