package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class ImpZombie extends StandardZombie {

    public ImpZombie(int health, double speed, int attackPower, int row, double startX,
                     double x, double y, int xSpeed, int ySpeed) {
        // پاس دادن پارامترها به StandardZombie (مقدار زره اولیه برای امپ معمولی 0 است)
        super(health, speed, attackPower, row, startX, 0, x, y, xSpeed, ySpeed);
    }

    // متد move کاملاً حذف شد چون دقیقاً همان رفتار کلاس والد را دارد و سیستم ارث‌بری خودکار آن را هندل می‌کند.
}
