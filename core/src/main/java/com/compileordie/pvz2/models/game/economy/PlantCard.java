package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;

public class PlantCard {
    public float cooldownTicks;
    public PlantType plantType;
    public float remainingCooldownTicks;
    public boolean isBoosted;

    public PlantCard(PlantType plantType, int cooldownTicks, boolean isBoosted) {
        this.plantType = plantType;
        this.cooldownTicks = cooldownTicks;
        this.remainingCooldownTicks = 0;
        this.isBoosted = isBoosted;
    }

    public PlantCard(PlantType plantType) {
        this.plantType = plantType;
        this.cooldownTicks = 0f;
        this.remainingCooldownTicks = 0f;
        this.isBoosted = false;
    }

    public void tick(int ticks) {
        remainingCooldownTicks -= ticks;
        if (remainingCooldownTicks < 0) remainingCooldownTicks = 0;
    }

    public void setTimer() {
        remainingCooldownTicks = cooldownTicks;
    }

    public void resetTimer() {
        remainingCooldownTicks = 0;
    }

    public float getRemainingCooldownSeconds() {
        return remainingCooldownTicks * Constants.Game.TIME_COEFFICIENT;
    }

    public boolean isReady() {
        return remainingCooldownTicks == 0;
    }
}
