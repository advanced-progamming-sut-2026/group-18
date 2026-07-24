package com.compileordie.pvz2.models.entities.zombies;

import com.compileordie.pvz2.models.entities.zombies.types.StatusEffectType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public class StatusEffect {
    private final StatusEffectType effectType;
    private final int durationTicks;
    private final int magnitude; // NEW: Needed for how much damage Poison does
    private int elapsedTicks;
    private boolean isApplied;

    // Updated constructor to accept magnitude
    public StatusEffect(StatusEffectType effectType, int durationTicks, int magnitude) {
        this.effectType = effectType;
        this.durationTicks = durationTicks;
        this.magnitude = magnitude;
        this.elapsedTicks = 0;
        this.isApplied = false;
    }

    public boolean isExpired() {
        return elapsedTicks >= durationTicks;
    }

    public void applyToZombie(Zombie zombie) {
        this.isApplied = true;

        // Immediate stat changes upon applying
        if (effectType == StatusEffectType.HYPNOTIZED) {
            zombie.setHypnotized(true);
            zombie.setCurrentSpeed(-Math.abs(zombie.getCurrentSpeed())); // Walk backward
        }
    }

    public void removeFromZombie(Zombie zombie) {
        this.isApplied = false;

        // Revert stat changes when effect ends
        if (effectType == StatusEffectType.HYPNOTIZED) {
            zombie.setHypnotized(false);
            zombie.recalculateSpeed(); // Fix direction
        } else if (effectType == StatusEffectType.CHILLED) {
            zombie.recalculateSpeed(); // Fix speed
        }
    }

    public void updateZombieTick(Zombie zombie) {
        if (!isApplied) return;
        elapsedTicks++;

        // Apply continuous Poison damage every 60 ticks (1 second)
        if (effectType == StatusEffectType.POISONED && elapsedTicks % 60 == 0) {
            zombie.takeDamage(magnitude, DamageType.POISON);
        }

        if (isExpired()) {
            removeFromZombie(zombie);
        }
    }

    // Getters
    public StatusEffectType getEffectType() { return effectType; }
    public int getDurationTicks() { return durationTicks; }
    public int getElapsedTicks() { return elapsedTicks; }
    public int getMagnitude() { return magnitude; }
    public boolean isApplied() { return isApplied; }
}
