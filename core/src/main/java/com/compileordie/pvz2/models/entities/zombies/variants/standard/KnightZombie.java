package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class KnightZombie extends StandardZombie {
    private int helmetArmorHealth;
    private int shoulderArmorHealth;

    public KnightZombie(int health, double speed, int attackPower, int row, double startX, int helmet, int shoulder, double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, helmet + shoulder, x, y, xSpeed, ySpeed, ZombieType.KNIGHT);
        this.helmetArmorHealth = helmet;
        this.shoulderArmorHealth = shoulder;
    }

    // فیکس باگ ریاضی شوالیه: کل دمیج زره به صورت زنجیره‌ای متوالی و بدون تکرار کسر می‌شود
    @Override
    public int takeArmorDamage(int amount) {
        if (!hasArmor()) return amount;

        int remainingDamage = amount;

        // ۱. بررسی و کسر از کلاه شوالیه
        if (!isHelmetBroken()) {
            if (remainingDamage <= helmetArmorHealth) {
                helmetArmorHealth -= remainingDamage;
                this.armorHealth -= remainingDamage;
                return 0;
            } else {
                remainingDamage -= helmetArmorHealth;
                this.armorHealth -= helmetArmorHealth;
                helmetArmorHealth = 0;
            }
        }

        // ۲. کسر باقی‌مانده آسیب سرریز از زره شانه
        if (!isShoulderArmorBroken()) {
            if (remainingDamage <= shoulderArmorHealth) {
                shoulderArmorHealth -= remainingDamage;
                this.armorHealth -= remainingDamage;
                return 0;
            } else {
                remainingDamage -= shoulderArmorHealth;
                this.armorHealth -= shoulderArmorHealth;
                shoulderArmorHealth = 0;
                removeArmor(); // زره کاملاً نابود شد
            }
        }

        return remainingDamage;
    }

    public boolean isHelmetBroken() { return this.helmetArmorHealth <= 0; }
    public boolean isShoulderArmorBroken() { return this.shoulderArmorHealth <= 0; }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        // حذف کدهای کثیف قبلی؛ متد والد با ساختار جدید شوالیه کاملاً سازگار است و بدون باگ کار می‌کند
        super.takeDamage(amount, damageType);
    }
}
