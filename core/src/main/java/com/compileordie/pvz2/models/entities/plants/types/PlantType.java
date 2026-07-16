package com.compileordie.pvz2.models.entities.plants.types;

import com.compileordie.pvz2.utils.Toolbox;

public enum PlantType {
    ;

    public static PlantType getByName(String name) {
        for (PlantType plantType : PlantType.values()) {
            if (plantType.toString().equalsIgnoreCase(name)) {
                return plantType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
