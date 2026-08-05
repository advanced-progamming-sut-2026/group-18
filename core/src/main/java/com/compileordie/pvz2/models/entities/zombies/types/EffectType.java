package com.compileordie.pvz2.models.entities.zombies.types;

import com.compileordie.pvz2.utils.Toolbox;

public enum EffectType {
    FROZEN,     // منجمد کامل
    CHILLED,    // کند شده با یخ
    HYPNOTIZED, // هیپنوتیزم شده
    CATIFIED;   // نفرین گربه

    public static EffectType getByName(String name) {
        for (EffectType effectType : EffectType.values()) {
            if (effectType.toString().equalsIgnoreCase(name)) {
                return effectType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
