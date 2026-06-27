package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class ImpDragon extends StandardZombie {

    public ImpDragon(int health, double speed, int attackPower, int row, double startX,
                     double x, double y, int xSpeed, int ySpeed) {
        // پاس دادن پارامترها به StandardZombie (امپ دراگون هم زره اولیه ندارد پس 0 می‌گذاریم)
        super(health, speed, attackPower, row, startX, 0, x, y, xSpeed, ySpeed);
    }

    @Override
    public void move() {
        // حرکت مستقیم به سمت چپ (خانه بازیکن)
        this.positionX -= this.currentSpeed;
    }

    /**
     * بازنویسی متد دریافت آسیب برای اعمال مصونیت امپ دراگون فقط در برابر دمیج آتشین
     */
    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        // داک/مکانیک دقیق: امپ دراگون در برابر آسیب‌های آتشین (FIRE) کاملاً مصون است
        if (damageType == DamageType.FIRE) {
            return; // تیر آتشین هیچ اثری رویش نمی‌گذارد و متد همین‌جا قطع می‌شود
        }

        // بقیه آسیب‌ها (شامل انفجاری EXPLOSIVE، معمولی STANDARD و قوسی LOBBER) به طور عادی اعمال می‌شوند
        super.takeDamage(amount, damageType);
    }
}
