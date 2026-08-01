package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;

public class PlantCard {
    public float cooldownTime;
    public PlantType plantType;
    public float timer;
    public boolean isBoosted;

    public PlantCard(PlantType plantType, float cooldownTime, boolean isBoosted) {
        this.plantType = plantType;
        this.cooldownTime = cooldownTime;
        this.timer = this.cooldownTime;
        this.isBoosted = isBoosted;
    }

    public PlantCard(PlantType plantType) {
        this.plantType = plantType;
        this.cooldownTime = 0f;
        this.timer = 0f;
        this.isBoosted = false;
    }

    public void tick(int ticks) {
        float dt = ticks * Constants.Game.TIME_COEFFICIENT;
        timer -= dt;
        if (timer < 0) timer = 0;
    }

    public void setTimer() {
        timer = cooldownTime;
    }

    public void resetTimer() {
        timer = 0;
    }

    public boolean isReady() {
        return timer == 0;
    }
}
