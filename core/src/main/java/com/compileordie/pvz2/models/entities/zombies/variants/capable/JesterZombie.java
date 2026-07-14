package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

/**
 * [زامبی ژانگولر - JesterZombie]
 * خوشه: CapableZombie (زامبی دارای توانمندی فعال)
 * وضعیت سینتکس: کاملاً اصلاح‌شده؛ متد useAbility اضافه شد تا ساختار اورراید کلاس مادر رعایت شود.
 * * توضیحات داک: با آمدن پرتابه شروع به چرخیدن می‌کند، سریع‌تر راه می‌رود و تمام تیرهای مستقیم را به سمت گیاهان برمی‌گرداند.
 */
public class JesterZombie extends CapableZombie {
    private boolean isSpinning;
    private final double spinningSpeedMultiplier = 1.6; // داک: حین چرخیدن، سریع‌تر به جلو حرکت می‌کند

    public JesterZombie(int health, double speed, int attackPower, int row, double startX,
                        double abilityCooldown, int abilityRange, double delta, double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed, ZombieType.JESTER_ZOMBIE);
        this.isSpinning = false;
    }

    /**
     * پیاده‌سازی متد انتزاعی اجباری خوشه CapableZombie
     * از آنجا که توانایی چرخیدن ژانگولر به صورت واکنش‌پذیر (Reactive) به تیرهاست،
     * این متد می‌تواند به عنوان فعال‌ساز وضعیت یا بررسی دوره‌ای کوول‌داون توسط سیستم توانایی‌ها استفاده شود.
     */
    @Override
    public void useAbility() {
        // در صورت نیاز به استفاده ترکیبی با سرویس توانایی‌ها
        resetCooldown();
    }

    @Override
    public void recalculateSpeed() {
        super.recalculateSpeed(); // اعمال افکت‌های کاهنده پایه از کلاس ریشه

        // داک: در صورت چرخیدن، سرعت حرکت افزایش پیدا می‌کند
        if (this.isSpinning) {
            this.currentSpeed = this.currentSpeed * this.spinningSpeedMultiplier;
        }
    }

    public void startSpinning() {
        this.isSpinning = true;
        recalculateSpeed();
    }

    public void stopSpinning() {
        this.isSpinning = false;
        recalculateSpeed();
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        // داک: حین چرخیدن تمام تیرهای مستقیم (NORMAL یا ICE) را برمی‌گرداند و دمیج نمی‌خورد
        if (this.isSpinning && (damageType == DamageType.NORMAL || damageType == DamageType.ICE)) {
            return; // دفع کامل آسیب؛ منطق بازگرداندن پرتابه توسط ZombieCombatService فیلتر می‌شود
        }

        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    /**
     * متد سیگنال‌دهی به ZombieCombatService برای بررسی امکان انعکاس پرتابه
     */
    public boolean reflectProjectileFlag(DamageType type) {
        return this.isSpinning && (type == DamageType.NORMAL || type == DamageType.ICE);
    }

    public boolean isSpinning() {
        return this.isSpinning;
    }
}
