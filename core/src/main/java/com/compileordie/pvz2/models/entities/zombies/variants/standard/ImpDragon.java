package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class ImpDragon extends StandardZombie {

    public ImpDragon(int health, double speed, int attackPower, int row, double startX,
                     double x, double y, int xSpeed, int ySpeed) {
        // پاس دادن پارامترها به StandardZombie (امپ دراگون هم زره اولیه ندارد پس 0 می‌گذاریم)
        super(health, speed, attackPower, row, startX, 0, x, y, xSpeed, ySpeed);
    }

    // متد move حذف شد تا جابجایی بر بر عهده لایه والد و متدهای سیستمی GameEntity باشد.

    /**
     * بازنویسی متد دریافت آسیب برای اعمال مصونیت امپ دراگون فقط در برابر دمیج آتشین
     */
    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        // داک/مکانیک دقیق: امپ دراگون در برابر آسیب‌های آتشین (FIRE) کاملاً مصون است
        if (damageType == DamageType.FIRE) {
            return;
        }

        super.takeDamage(amount, damageType);
    }
}
