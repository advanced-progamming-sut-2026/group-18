package com.compileordie.pvz2.models.entities.zombies.services.manager;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.services.movement.ZombieMovementService;
import com.compileordie.pvz2.models.entities.zombies.services.combat.ZombieCombatService;
import com.compileordie.pvz2.models.entities.zombies.services.effects.ZombieStatusEffectService;
import com.compileordie.pvz2.models.entities.zombies.services.ability.ZombieAbilityService;
import com.compileordie.pvz2.models.entities.zombies.services.spawn.ZombieSpawnLootService;

public class ZombieManager {

    private final ZombieMovementService movementService;
    private final ZombieCombatService combatService;
    private final ZombieStatusEffectService effectService;
    private final ZombieAbilityService abilityService;
    private final ZombieSpawnLootService spawnLootService;

    public ZombieManager(ZombieMovementService movementService, ZombieCombatService combatService,
                         ZombieStatusEffectService effectService, ZombieAbilityService abilityService,
                         ZombieSpawnLootService spawnLootService) {
        this.movementService = movementService;
        this.combatService = combatService;
        this.effectService = effectService;
        this.abilityService = abilityService;
        this.spawnLootService = spawnLootService;
    }

    /**
     * تنها نقطه اتصال کنترلر اصلی بازی با سیستم زامبی‌ها
     * این متد در هر فریم تمام رفتارهای زامبی‌ها را ارکستر می‌کند.
     */
    public void tick(com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext context) {

        // ۱. تزریق مینیون‌های صف معلق (مثل جوجه‌ها یا ایمپ‌ها) به لیست اصلی در ابتدای فریم
        spawnLootService.flushSpawnQueue(context);

        // ۲. به‌روزرسانی تایمر افکت‌ها (یخ‌زدگی، سم، گیجی، هیپنوتیزم) و اعمال تغییر سرعت‌ها
        effectService.updateActiveEffects(context);

        // ۳. بررسی مکانیک خشم (Rage) برای زامبی روزنامه و فرعون در صورت شکستن زره
        for (Zombie zombie : context.getActiveZombies()) {
            effectService.handleRageMechanic(zombie);
        }

        // ۴. اجرای اسپل‌ها و توانایی‌ها (دزدیدن خورشید رع، جادوگر، اختاپوس، شکارچی)
        abilityService.executeActiveAbilities(context);

        // ۵. مکانیک ژانگولر: بررسی و معکوس کردن مسیر تیرها قبل از محاسبات برخورد تیر به زامبی
        for (Zombie zombie : context.getActiveZombies()) {
            // فراخوانی متد بازتاب با امضای (zombie, context)
            combatService.reflectProjectiles(zombie, context);
        }

        // ۶. جابه‌جایی فیزیکی (راه رفتن، غواصی زیر آب، پرواز دودو، حرکت عقب‌عقب معدن‌چی)
        movementService.moveAll(context);

        // ۷. بررسی عبور از خط پایان، فعال‌سازی چمن‌زن‌ها یا تریگر کردن حالت باخت (GameOver)
        movementService.checkLawnMowersAndBreach(context);

        // ۸. پردازش درگیری‌های نزدیک (جویدن گیاهان، له کردن غول‌ها/پیانیست، آسیب دیدن زامبی از تیرها)
        // ⚠️ نکته مهم داک: اگر زامبی هیپنوتیزم باشد، در این مرحله به زامبی‌های دیگر حمله می‌کند!
        combatService.processCombat(context);

        // ۹. مدیریت مرگ زامبی‌ها، پاک‌سازی حافظه و رهاسازی لوت‌ها (مانند پس دادن خورشیدهای رع)
        spawnLootService.handleZombieDeathAndLoot(context);
    }
}
