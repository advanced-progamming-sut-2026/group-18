package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class JesterZombie extends CapableZombie {
    private boolean isSpinning;
    private final double spinSpeedMultiplier; // دریافت ضریب سرعت از سازنده برای عدم استفاده از عدد فرضی

    public JesterZombie(int health, double speed, int attackPower, int row, double startX,
                        double abilityCooldown, int abilityRange, double delta, double x, double y,
                        int xSpeed, int ySpeed, double spinSpeedMultiplier) {
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);
        this.isSpinning = false;
        this.spinSpeedMultiplier = spinSpeedMultiplier;
        setDelta(delta);
    }

    // ** مرتبط با سرویس خاص **
    @Override
    public void useAbility() {
        // رفتار واکنشی است و لایه اصلی آن در takeDamage مدیریت می‌شود
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        // داک: چرخش را تا زمانی که پرتابه ای به سمتش نیاید ادامه میدهد. بعد آن دوباره شروع به حرکت عادی میکند
        // اگر در این تیک پرتابه جدیدی به دلقک برخورد نکرده باشد، چرخش متوقف می‌شود
        if (isSpinning) {
            this.isSpinning = false;
            recalculateSpeed();
        }
    }

    @Override
    public void recalculateSpeed() {
        super.recalculateSpeed();

        // داک: حین چرخیدن، سریع تر به جلو حرکت میکند
        if (isSpinning) {
            this.currentSpeed *= this.spinSpeedMultiplier;
        }
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        // داک: تیر باقی گیاهان از گیاهان یخ زده عبور نمیکند، مگر lobberها
        if (damageType == DamageType.LOBBER) {
            this.isSpinning = false;
            recalculateSpeed();
            this.health -= amount;
            if (this.health < 0) this.health = 0;
            return;
        }

        // داک: وقتی یک پرتابه به سمتش بیاید، شروع میکند به چرخیدن
        this.isSpinning = true;
        recalculateSpeed();

        // داک: حین چرخیدن، تمام تیر های مستقیمی که به سمتش میایند را به سمت گیاهان برمیگرداند
        // داک: اگر تیر ها یخی باشند، سبب یخ زدن گیاهان میشوند (اثر شکارچی)
        if (damageType == DamageType.ICE) {
            reflectProjectile(true);
        } else {
            reflectProjectile(false);
        }

        // داک: تعداد تیر های بازگردانده شده، مدت زمان چرخش و تعداد دفعات شروع چرخیدن محدودیتی ندارد
        // دمیج تیر مستقیم بلاک شده و از جان زامبی کم نمی‌شود
    }

    // ** مرتبط با سرویس خاص **
    private void reflectProjectile(boolean isIce) {
        // لایه سرویس مبارزه بر اساس این متد، پرتابه مستقیم معکوس شده را به سمت گیاهان شلیک می‌کند
    }

    public boolean isSpinning() {
        return this.isSpinning;
    }
}
