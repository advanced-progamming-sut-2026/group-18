package com.compileordie.pvz2.models.entities.plants.enums;

/**
 * Defines the elemental and physical nature of a projectile.
 * Used primarily by Obstacles (Tomb, Barrel) and Zombie armor
 * to calculate resistances, immunities, or special reactions.
 */
public enum ProjectileType {

    // --- Physical Projectiles ---
    NORMAL,      // Peashooters, Spores (Puff/Sea/Fume-shroom), Stars, Cactus Thorns, Bowling Bulbs
    LOBBED,      // Cabbage-pult, Melon-pult (Arcs over standard shields)

    // --- Elemental Projectiles ---
    FIRE,        // Fire Peashooter, Pepper-pult (Melts ice, deals bonus damage to some obstacles)
    ICE,         // Snow Pea, Winter Melon (Applies FROZEN/CHILLED effect)
    POISON,      // Goo Peashooter (Applies POISON DoT, bypasses armor)
    ELECTRIC,    // Electric Blueberry, Lightning Reed (Bypasses Jester/Shields)

    // --- Status Payload Projectiles ---
    BUTTER       // Kernel-pult (Applies STUNNED effect, instantly kills seagulls)
}
