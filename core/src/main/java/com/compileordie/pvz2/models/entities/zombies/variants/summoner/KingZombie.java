package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.models.entities.zombies.variants.standard.StandardZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.KnightZombie;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class KingZombie extends SummonerZombie {
    private final int knightingAreaX;
    private final int knightingAreaY;

    public KingZombie(int health, int row, double startX, double x, double y,
                      int knightingAreaX, int knightingAreaY, double delayBetweenKnightings) {
        super(health, 0.0, 0, row, startX, x, y, 0, 0, delayBetweenKnightings);
        this.knightingAreaX = knightingAreaX;
        this.knightingAreaY = knightingAreaY;
    }

    @Override
    public void move() {
        // پادشاه ثابت است و حرکتی ندارد
    }

    @Override
    public void summon() {
        resetSummonCooldown();
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    /**
     * تبدیل زامبی عادی به شوالیه (توسط سرویس توانایی‌ها اجرا می‌شود)
     */
    public KnightZombie knightifyZombie(StandardZombie target, int helmetArmor, int shoulderArmor) {
        if (target == null || target.isDead() || !canSummon()) return null;

        this.summon(); // ریست کردن تایمر کوول‌داون

        return new KnightZombie(
            target.getHealth(),
            target.getCurrentSpeed(),
            target.getAttackPower(),
            target.getCurrentRow(),
            target.getPositionX(),
            helmetArmor,
            shoulderArmor,
            target.getX(),
            target.getY(),
            target.getXSpeed(),
            target.getYSpeed()
        );
    }

    public int getKnightingAreaX() { return knightingAreaX; }
    public int getKnightingAreaY() { return knightingAreaY; }
}
