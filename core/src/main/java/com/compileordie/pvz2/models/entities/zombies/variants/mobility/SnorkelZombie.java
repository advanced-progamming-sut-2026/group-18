package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class SnorkelZombie extends MobilityZombie {

    public SnorkelZombie(int health, double speed, int attackPower, int row, double startX,
                         int initialArmor, double delta, double x, double y, double xSpeed, double ySpeed,
                         double underwaterSpeedModifier) {
        // فرستادن تمام پارامترها به والدِ موبیلیتی
        super(health, speed, attackPower, row, startX, initialArmor, delta, x, y, xSpeed, ySpeed, underwaterSpeedModifier, ZombieType.SNORKEL_ZOMBIE);
        setDelta(delta);

        // وضعیت اولیه هنگام ورود (اگر زمین پیش‌فرض آب باشد، شیرجه می‌زند)
        dive();
    }

    /**
     * داک: در صورت رسیدن به آب (مثلا در جزر و مد) زیر آن حرکت میکند
     */
    public void dive() {
        changeMovementState(MovementState.UNDERWATER);
    }

    /**
     * داک: برای خوردن گیاهان، به سطح آب میاید
     */
    public void surface() {
        changeMovementState(MovementState.EATING);
    }

    /**
     * مدیریت رویداد برخورد فیزیکی با گیاه از طریق سرویس بازی
     */
    public void onPlantCollision() {
        // داک: برای خوردن گیاهان، به سطح آب میاید و در این حالت نسبت به حملات گیاهان اسیب پذیر است
        startEating(); // ۱. فعال کردن ترمز فیزیکی در کلاس ریشه Zombie (isEating = true)
        surface();     // ۲. تغییر وضعیت حرکتی به EATING و رو آمدن روی سطح آب
    }

    /**
     * رویداد باز شدن مسیر بعد از نابودی گیاه جلو غواص
     */
    public void onPathCleared() {
        stopEating(); // ۱. آزاد کردن ترمز فیزیکی کلاس ریشه Zombie (isEating = false)
        dive();       // ۲. مسیر باز شد؛ دوباره به زیر آب برمی‌گردد
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        // داک: مادامی که زیر آب است، تنها lobber ها و مواد منفجره میتوانند به آن آسیب بزنند
        if (this.movementState == MovementState.UNDERWATER) {
            if (damageType != DamageType.LOBBER && damageType != DamageType.EXPLOSIVE) {
                return; // اگر تیر مستقیم (Standard) بود، هیچ آسیبی نمی‌بیند و تیر از سرش رد می‌شود
            }
        }

        // محاسبه دمیج در حالت عادی (روی سطح آب یا دریافت تیر Lobber زیر آب)
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }
}
