package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.StandardZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.KnightZombie;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class KingZombie extends SummonerZombie {
    public static final int waveCost = 750;
    private final int knightingAreaX;
    private final int knightingAreaY;
    private boolean readyToKnight;

    public KingZombie(int health, int row, double startX, double x, double y,
                      int knightingAreaX, int knightingAreaY, double delayBetweenKnightings) {
        super(health, 0.0, 0, row, startX, x, y, 0.0, 0.0, ZombieType.KING_ZOMBIE, delayBetweenKnightings);
        this.knightingAreaX = knightingAreaX;
        this.knightingAreaY = knightingAreaY;
        this.readyToKnight = false;
    }

    @Override
    public void move(int ticks) {
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {

    }

    @Override
    public void summon() {
        this.readyToKnight = true;
    }

    public boolean canSummon() {
        return this.readyToKnight;
    }


    public KnightZombie knightifyZombie(StandardZombie target, int helmetArmor, int shoulderArmor) {
        if (target == null || target.isDead() || !canSummon()) {
            return null;
        }

        this.readyToKnight = false;
        this.summonTimer = 0;

        return new KnightZombie(
            target.getHealth(),
            target.getXSpeed(),
            target.getAttackPower(),
            target.getCurrentRow(),
            target.getX(),
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
