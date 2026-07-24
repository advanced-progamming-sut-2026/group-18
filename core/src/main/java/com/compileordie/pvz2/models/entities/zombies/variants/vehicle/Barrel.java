package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.ImpZombie;

import java.util.ArrayList;
import java.util.List;

public class Barrel extends MovableObject {
    private final int row;
    private final double positionY;
    private final List<ImpZombie> spawnedImps = new ArrayList<>();
    private boolean spawnImp = false;
    private double positionX;

    public Barrel(double barrelHealth, int row, double positionX, double positionY) {
        super(barrelHealth, positionX, positionY, ObstacleType.BARREL);
        this.row = row;
        this.positionX = positionX;
        this.positionY = positionY;
    }


    public void updatePosition(double newX) {
        this.positionX = newX;
    }

    public void takeDamage(double amount, int impHealth, double impSpeed, int impAttackPower) {
        if (this.isDestroyed) return;

        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
            this.isDestroyed = true;
            this.spawnImp = true;
        }
    }


    public int getRow() {
        return row;
    }

    public boolean shouldWeSpawnImp() {
        return spawnImp;
    }

    public void stopSpawnImp() {
        this.spawnImp = false;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public boolean isImpassable() {
        return true;
    }
}
