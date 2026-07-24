package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;

public class PlantCard {
    public final float COOLDOWN_TIME;
    public PlantType plantType;
    public float timer;
    public boolean isBoosted;

    public PlantCard(PlantType plantType, float cooldownTime, boolean isBoosted) {
        this.plantType = plantType;
        this.COOLDOWN_TIME = cooldownTime;
        this.timer = COOLDOWN_TIME;
        this.isBoosted = isBoosted;
    }

    public PlantCard(PlantType plantType) {
        this.plantType = plantType;
        this.COOLDOWN_TIME = 0f;
        this.timer = 0f;
        this.isBoosted = false;
    }

    public void tick(int ticks) {
        float dt = ticks * Constants.Game.TIME_COEFFICIENT;
        timer -= dt;
        if (timer < 0) timer = 0;
    }

    public boolean isReady() {
        return timer == 0;
    }
}
