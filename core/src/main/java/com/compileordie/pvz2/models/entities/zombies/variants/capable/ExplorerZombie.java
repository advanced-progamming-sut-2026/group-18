package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;

public class ExplorerZombie extends CapableZombie {
    private boolean torchLit;
    private final int baseAttackPower;

    public ExplorerZombie(int health, double speed, int attackPower, int row, double startX,
                          double abilityCooldown, int abilityRange,double delta, double x, double y, int xSpeed, int ySpeed) {
        // پیش‌فرض پیشنهادی داک: محدوده توانایی کمتر از 1 خانه (abilityRange = 1)
        // کول‌داون بررسی توانایی سوختن بسیار کم (مثلاً 0.1 ثانیه) جهت چک کردن مدام وضعیت سطر
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);

        this.baseAttackPower = attackPower;
        this.torchLit = true;
        this.attackPower = 999999; // قدرت سوزاندن گیاهان در بدو ورود با مشعل روشن
        setDelta(delta);
    }

    // ** مرتبط با سرویس خاص **
    @Override
    public void useAbility() {
        // در متد tick کلاس والد، canUseAbility بررسی می‌شود.
        // اگر مشعل روشن باشد، لایه سرویس کامبت بررسی می‌کند که اگر گیاهی در سطر او و در فاصله کمتر از ۱ خانه (abilityRange) باشد،
        // به دلیل attackPower بی‌نهایت زامبی، گیاه درجا از بین می‌رود (می‌سوزد).
        if (torchLit) {
            this.attackPower = 999999;
        }
        resetCooldown();
    }

    @Override
    public void tick() {
        // ابتدا افکت‌های فعال زامبی در تیک کلاس والد آپدیت می‌شوند
        super.tick();
        if (isDead()) return;

        // پیاده‌سازی بند دوم داک (تأثیر گیاهان یخی):
        // اگر گیاه یخی افکت سرمازدگی (CHILLED) یا انجماد (FREEZE) به زامبی داده باشد، مشعل خاموش می‌شود.
        if (hasEffect(EffectType.CHILLED) || hasEffect(EffectType.FREEZE)) {
            extinguishTorch();
        }
    }

    public void extinguishTorch() {
        this.torchLit = false;
        this.attackPower = this.baseAttackPower; // مشعل که خاموش شود، مثل زامبی عادی با قدرت پایه می‌جود
    }

    public void igniteTorch() {
        // اگر افکت سرمایی روی زامبی باشد، ابتدا باید توسط آتش خنثی و حذف شود
        if (hasEffect(EffectType.CHILLED)) {
            removeStatusEffect(EffectType.CHILLED);
        }
        this.torchLit = true;
        this.attackPower = 999999; // مشعل دوباره روشن و کشنده می‌شود
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        // پیاده‌سازی بند دوم و سوم داک (تأثیر پرتابه‌های یخی و آتشین)
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
