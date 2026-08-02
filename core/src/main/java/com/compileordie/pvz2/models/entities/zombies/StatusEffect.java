package com.compileordie.pvz2.models.entities.zombies;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public class StatusEffect {
    private final EffectType effectType;
    private final int durationTicks;
    private int elapsedTicks;
    private boolean isApplied;

    public StatusEffect(EffectType effectType, int durationTicks) {
        this.effectType = effectType;
        this.durationTicks = durationTicks;
        this.elapsedTicks = 0;
        this.isApplied = false;
    }

    public boolean isExpired() {
        return elapsedTicks >= durationTicks;
    }

    // NEW: Trigger the physical changes on the zombie
    public void applyToZombie(Zombie zombie) {
        this.isApplied = true;

        switch (effectType) {
            case FROZEN:
                zombie.setStopZombieNow(true); // Completely stops movement
                break;
            case CHILLED:
                // Cuts speed in half
                zombie.setXSpeed(zombie.getStableSpeed() * 0.5);
                break;
            case HYPNOTIZED:
                zombie.setHypnotized(true);
                break;
            case CATIFIED:
                zombie.setAttackPower(0); // Cannot eat while a cat
                break;
        }
    }

    public void removeFromZombie(Zombie zombie) {
        this.isApplied = false;
        this.elapsedTicks = 0;

        switch (effectType) {
            case FROZEN:
                // Only unfreeze if the zombie doesn't have ANOTHER frozen effect stacked
                if (zombie.getActiveEffects().stream()
                    .noneMatch(e -> e != this && e.getEffectType() == EffectType.FROZEN)) {
                    zombie.setStopZombieNow(false);
                }
                break;
            case CHILLED:
                if (zombie.getActiveEffects().stream()
                    .noneMatch(e -> e != this && e.getEffectType() == EffectType.CHILLED)) {
                    zombie.setXSpeed(zombie.getStableSpeed());
                }
                break;
            case HYPNOTIZED:
                zombie.setHypnotized(false);
                break;
            case CATIFIED:
                // Just let the natural attack power reset if handled elsewhere, or restore it here
                break;
        }
    }

    public void updateZombieTick(Zombie zombie) {
        // Apply the effect dynamically on its very first tick
        if (!isApplied) {
            applyToZombie(zombie);
        }

        elapsedTicks++;

        if (isExpired()) {
            removeFromZombie(zombie);
        }
    }

    public float getRemainingTime() {
        return (durationTicks - elapsedTicks) * Constants.Game.TIME_COEFFICIENT;
    }

    // Getters
    public EffectType getEffectType() { return effectType; }
    public int getDurationTicks() { return durationTicks; }
    public int getElapsedTicks() { return elapsedTicks; }
    public boolean isApplied() { return isApplied; }
}
