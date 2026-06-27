package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.models.entities.zombies.variants.standard.StandardZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.KnightZombie;

public class KingZombie extends SummonerZombie {

    private final int knightingAreaX;
    private final int knightingAreaY;
    private final double delayBetweenKnightings;

    public KingZombie(int health, int row, double startX, double x, double y,
                      int knightingAreaX, int knightingAreaY, double delayBetweenKnightings) {
        // پادشاه در راست‌ترین ستون قرار می‌گیرد و حرکت نمی‌کند (سرعت = 0)
        super(health, 0.0, 0, row, startX, x, y, 0, 0, delayBetweenKnightings);
        this.knightingAreaX = knightingAreaX;
        this.knightingAreaY = knightingAreaY;
        this.delayBetweenKnightings = delayBetweenKnightings;
    }

    @Override
    public void move() {
        // متد خالی است چون زامبی پادشاه کاملاً ایستا (Stationary) است و راه نمی‌رود
    }

    @Override
    public void summon() {
        resetSummonCooldown();
    }

    @Override
    public void resetSummonCooldown() {
        this.summonCooldown = this.delayBetweenKnightings;
    }

    @Override
    public void takeDamage(int amount, com.compileordie.pvz2.models.entities.zombies.types.DamageType damageType) {
        if (isDead()) return;

        this.health -= amount;
        if (this.health < 0) {
            this.health = 0;
        }
    }

    /**
     * تبدیل زامبی ساده اطراف به شوالیه
     * فیلدها و متدها کاملاً منطبق بر ساختار ارسالی فیکس شده‌اند تا خطای نبود گتر رخ ندهد.
     */
    public KnightZombie knightifyZombie(StandardZombie target, int helmetArmor, int shoulderArmor) {
        if (target == null || target.isDead() || !canSummon()) return null;

        this.summon(); // ریست کردن تایمر کوول‌داون پادشاه

        // دسترسی به ویژگی‌های زامبی ساده بر اساس فیلدهای protected در لایه Zombie و گترهای لایه GameEntity
        return new KnightZombie(
            target.getHealth(),          // دسترسی از طریق گتر موجود در Zombie
            target.getCurrentSpeed(),    // سرعت فعلی (از گتر getCurrentSpeed)
            target.getAttackPower(),     // قدرت هجوم (از گتر getAttackPower)
            target.getCurrentRow(),      // سطر فعلی (از گتر getCurrentRow)
            target.getPositionX(),       // موقعیت شروع فیزیکی x (از گتر getPositionX)
            helmetArmor,                 // میزان زره کلاه‌خود
            shoulderArmor,               // میزان زره شانه‌بند
            target.getX(),               // مختصات دقیق X (از گتر کلاس GameEntity)
            target.getY(),               // مختصات دقیق Y (از گتر کلاس GameEntity)
            target.getXSpeed(),          // سرعت مولفه X (از گتر کلاس GameEntity)
            target.getYSpeed()           // سرعت مولفه Y (از گتر کلاس GameEntity)
        );
    }

    public int getKnightingAreaX() { return knightingAreaX; }
    public int getKnightingAreaY() { return knightingAreaY; }
}
