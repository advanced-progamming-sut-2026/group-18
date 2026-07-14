package com.compileordie.pvz2.models.entities.zombies.services.manager;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.projectiles.Projectile;
import com.compileordie.pvz2.models.map.GameMap;
import com.compileordie.pvz2.models.economy.Sun; // خورشیدهای رها شده روی زمین
import com.compileordie.pvz2.models.economy.Coin; // سکه‌های رها شده روی زمین
import com.compileordie.pvz2.models.entities.LawnMower; // ماشین‌های چمن‌زنی

import java.util.List;
import java.util.Queue;

/**
 * اگر فردا مکانیزم جدیدی به بازی اضافه شود، فقط فیلدش به اینجا اضافه می‌شود
 * و امضای متد tick در ZombieManager و کنترلر اصلی دست‌نخورده باقی می‌ماند.
 */
public interface ZombieTickContext {
    double getDelta();                           // زمان سپری شده در این تیک
    GameMap getGameMap();                        // نقشه بازی (برای تشخیص کاشی آب، سطرها و...)
    List<Zombie> getActiveZombies();             // لیست زامبی‌های زنده در زمین
    Queue<Zombie> getSpawnQueue();               // ⚠️ صف تردایمن برای جوجه‌ها و ایمپ‌های پرتابی وسط فریم
    List<Plant> getActivePlants();               // لیست گیاهان زنده زمین
    List<Projectile> getActiveProjectiles();     // لیست تیرها (برای ریفلکت ژانگولر و آسیب‌ها)
    List<Sun> getLooseSunsOnGround();            // خورشیدهای روی زمین (برای جذب زامبی رع)
    List<Coin> getLooseCoinsOnGround();          // سکه‌های زمین (برای سیستم لوت)
    List<LawnMower> getLawnMowers();             // ماشین‌های چمن‌زنی سطرها

    void triggerGameOver();                      // متد اعلام باخت به سشن اصلی در صورت عبور زامبی
}
