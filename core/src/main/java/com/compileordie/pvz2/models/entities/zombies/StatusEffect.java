package com.compileordie.pvz2.models.entities.zombies;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public class StatusEffect {
    private final EffectType effectType;
    private final int durationTicks;
    private int elapsedTicks;
    public boolean isApplied;
    private double cSpeed;
    private int cAttack;

    public StatusEffect(EffectType effectType, int durationTicks) {
        this.effectType = effectType;
        this.durationTicks = durationTicks;
        this.elapsedTicks = 0;
        this.isApplied = false;
    }

    public boolean isExpired() {
        return elapsedTicks >= durationTicks;
    }

    // NEW: Safely resets the timer without stacking the physical stat reductions!
    public void refreshDuration() {
        this.elapsedTicks = 0;
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
                this.cSpeed = zombie.getXSpeed();
                zombie.setXSpeed(cSpeed * 0.5);
                this.cAttack = zombie.getAttackPower();
                zombie.setAttackPower((int)(cAttack * 0.5));
                break;
            case HYPNOTIZED:
                zombie.setHypnotized(true);
                break;
            case STUNNED:
                this.elapsedTicks = 50; // 5 Seconds
                zombie.setStopZombieNow(true);
            case POISON:
                this.elapsedTicks = 50; // 5 Seconds
                zombie.takeDamage(6, DamageType.NORMAL);
        }
    }

    public void removeFromZombie(Zombie zombie) {
        if(!isApplied) return;

        this.isApplied = false;
        this.elapsedTicks = 0;

        switch (effectType) {
            case FROZEN:
                zombie.setStopZombieNow(false);
                break;
            case CHILLED:
                zombie.setXSpeed(this.cSpeed);
                zombie.setAttackPower(this.cAttack);
                break;
            case HYPNOTIZED:
                zombie.setHypnotized(false);
                break;
            case STUNNED:
                zombie.setStopZombieNow(false);
            case POISON:

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
