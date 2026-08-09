package com.compileordie.pvz2.models.entities.plants.enums;

import com.compileordie.pvz2.utils.Toolbox;

public enum PlantCategory {
    SUN_PRODUCERS,
    SHOOTERS,
    LOBBERS,
    EXPLOSIVES,
    MELEE_ATTACKERS,
    WALL_NUTS,
    MODIFIERS,
    STRIKE_THROUGH,
    HOMING,
    MINTS;

    public static PlantCategory getByName(String name) {
        for (PlantCategory plantCategory : PlantCategory.values()) {
            if (plantCategory.toString().equalsIgnoreCase(name)) {
                return plantCategory;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
