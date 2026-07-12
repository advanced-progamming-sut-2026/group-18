package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class ParasolZombie extends StandardZombie {

    public ParasolZombie(int health, double speed, int attackPower, int row, double startX, int initialArmor, double x, double y, int xSpeed, int ySpeed) {
        super(health, speed, attackPower, row, startX, initialArmor, x, y, xSpeed, ySpeed);
    }

    // اصلاح نام متد از نظر ادبیات برنامه نویسی (Repelled به معنی دفع شده)
    public boolean isRepelled(DamageType type) {
        return type == DamageType.LOBBER;
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead() || isRepelled(damageType)) return;
        // فراخوانی متد والد برای ارث‌بری خودکار ویژگی‌های زره و آسیب BYPASS_ARMOR
        super.takeDamage(amount, damageType);
    }
}
