package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;

/**
 * [زامبی مشعل‌دار - ExplorerZombie]
 * خوشه: CapableZombie (زامبی دارای توانمندی فعال)
 * وضعیت سینتکس: کاملاً اصلاح‌شده؛ متد useAbility پیاده‌سازی شد تا از ارور کامپایل جلوگیری شود.
 * * توضیحات داک: با مشعل روشن گیاهان جلو را درجا می‌سوزاند. تیر یخی مشعل را خاموش و تیر آتشین آن را مجدد روشن می‌کند.
 */
public class ExplorerZombie extends CapableZombie {
    private boolean torchLit;
    private final int baseAttackPower;

    public ExplorerZombie(int health, double speed, int attackPower, int row, double startX,
                          double abilityCooldown, int abilityRange, double delta, double x, double y, int xSpeed, int ySpeed) {
        // فراخوانی دقیق سازنده ۱۲ پارامتری CapableZombie موجود در فایل شما
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);
        this.baseAttackPower = attackPower;
        this.torchLit = true;
        this.attackPower = 999999; // داک: سوزاندن درجا گیاهان در صورت روشن بودن مشعل
    }

    /**
     * پیاده‌سازی متد انتزاعی اجباری خوشه CapableZombie
     * این متد توسط ZombieAbilityService در زمان تیک‌های کوول‌داون صدا زده می‌شود.
     */
    @Override
    public void useAbility() {
        if (this.torchLit) {
            // در صورتی که مشعل روشن باشد، قدرت تخریب بالا نگه داشته می‌شود
            this.attackPower = 999999;
        }
        // ریست کردن تایمر کوول‌داون بر اساس متد موجود در کلاس پایه شما
        resetCooldown();
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        // داک: افکت‌های سرمایی محیطی مشعل را خاموش می‌کنند
        if (hasEffect(EffectType.CHILLED) || hasEffect(EffectType.FREEZE)) {
            extinguishTorch();
        }
    }

    public void extinguishTorch() {
        if (this.torchLit) {
            this.torchLit = false;
            this.attackPower = this.baseAttackPower; // بازگشت به قدرت تخریب عادی زامبی برای جویدن
        }
    }

    public void igniteTorch() {
        if (!this.torchLit) {
            removeStatusEffect(EffectType.CHILLED);
            removeStatusEffect(EffectType.FREEZE);
            this.torchLit = true;
            this.attackPower = 999999;
        }
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        if (damageType == DamageType.ICE) {
            extinguishTorch();
        } else if (damageType == DamageType.FIRE) {
            igniteTorch();
        }

        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    public boolean isTorchLit() {
        return this.torchLit;
    }
}
