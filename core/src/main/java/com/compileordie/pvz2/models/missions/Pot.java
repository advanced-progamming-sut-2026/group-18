package com.compileordie.pvz2.models.missions;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;

public class Pot {
    public boolean isEmpty;
    public boolean isMarigold;
    public PlantType targetPlant;
    public long readyTimeMillis;

    public Pot() {
        this.isEmpty = true;
        this.isMarigold = false;
        this.targetPlant = null;
        this.readyTimeMillis = 0;
    }
}
