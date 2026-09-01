package com.compileordie.pvz2.models.entities.plants.enums;

import com.compileordie.pvz2.utils.Toolbox;

public enum PlantCategory {
    MINIGAME,
    SUN_PRODUCER,
    SHOOTER,
    LOBBER,
    EXPLOSIVE,
    MELEE,        // Changed from MELEE_ATTACKERS
    WALL_NUT,     // Changed from WALL_NUTS
    MODIFIER,     // Changed from MODIFIERS
    STRIKE_THROUGH,
    HOMING,
    MINT;          // Changed from MINTS

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
