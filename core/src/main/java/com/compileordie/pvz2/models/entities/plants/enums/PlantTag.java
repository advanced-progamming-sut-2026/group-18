package com.compileordie.pvz2.models.entities.plants.enums;

import com.compileordie.pvz2.utils.Toolbox;

public enum PlantTag {
    DAY,
    NIGHT,
    SHROOM,
    WRAMP_UP,
    PEA,
    ICE,
    FIRE,
    STACK,
    CHARGE,
    MAGIC,
    POISON,
    WATER,
    AOE,
    TRAP,
    MOVE_ZOMBIES,
    SUN,
    EXPLOSIVE;

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
