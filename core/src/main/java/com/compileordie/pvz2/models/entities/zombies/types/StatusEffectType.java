package com.compileordie.pvz2.models.entities.zombies.types;

public enum StatusEffectType {
    CHILLED,      // Slows movement and attack speed (Snow Pea / Winter Melon)
    FROZEN,       // Completely stops movement and attacks (Iceberg Lettuce)
    STUNNED,      // Completely stops movement and attacks temporarily (Butter)
    POISONED,     // Continuous damage over time that ignores armor (Goo Peashooter)
    HYPNOTIZED    // Reverses direction and attacks other zombies (Caulipower / Hypno-shroom)
}
