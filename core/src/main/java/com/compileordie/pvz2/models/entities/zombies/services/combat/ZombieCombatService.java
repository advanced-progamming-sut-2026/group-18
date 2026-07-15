package com.compileordie.pvz2.models.entities.zombies.services.combat;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext;

/**
 * اینترفیس مدیریت مبارزه، دمیج زامبی‌ها به گیاهان، دریافت آسیب و تعاملات پرتابه‌ها.
 * کاملاً هماهنگ با متد tick چرخه اصلی بازی.
 */
public interface ZombieCombatService {

    /**
     * مرحله ۵ چرخه بازی:
     * مکانیک ژانگولر: بررسی و معکوس کردن مسیر تیرها قبل از محاسبات برخورد تیر به زامبی
     */
    void reflectProjectiles(Zombie zombie, ZombieTickContext context);

    /**
     * مرحله ۸ چرخه بازی:
     * پردازش درگیری‌های نزدیک (جویدن گیاهان، له کردن غول‌ها/پیانیست، آسیب دیدن زامبی از تیرها)
     *  نکته مهم داک: اگر زامبی هیپنوتیزم باشد، در این مرحله به زامبی‌های دیگر حمله می‌کند!
     */
    void processCombat(ZombieTickContext context);
}
