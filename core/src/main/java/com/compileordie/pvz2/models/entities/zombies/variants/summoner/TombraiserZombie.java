package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class TombraiserZombie extends SummonerZombie {
    public static final int waveCost = 300;
    private boolean spawnTomb = false;

    public TombraiserZombie(double health, double speed, int attackPower, int row, double startX,
                            double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.TOMBRAISER, 10.0);
    }


    @Override
    public void summon() {
        spawnTomb = true;
    }

    public boolean shouldWeSpawnTomb() {
        return spawnTomb;
    }

    public void stopSpawnTomb() {
        this.spawnTomb = false;
    }
}
