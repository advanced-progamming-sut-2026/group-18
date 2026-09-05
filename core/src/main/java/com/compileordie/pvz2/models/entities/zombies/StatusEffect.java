package com.compileordie.pvz2.models.entities.zombies;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.ProspectorZombie;

public class StatusEffect {
    private final EffectType effectType;
    private final int durationTicks;
    private int elapsedTicks;
    public boolean isApplied;
    private double cSpeed;
    private int cAttack;
    private int tickDamage = 0;

    public StatusEffect(EffectType effectType, int durationTicks) {
        this.effectType = effectType;
        this.durationTicks = durationTicks;
        this.elapsedTicks = 0;
        this.isApplied = false;
    }

    public StatusEffect(EffectType effectType, int durationTicks, int tickDamage) {
        this.effectType = effectType;
        this.durationTicks = durationTicks;
        this.elapsedTicks = 0;
        this.isApplied = false;
        this.tickDamage = tickDamage;
    }

    public boolean isExpired() {
        return elapsedTicks >= durationTicks;
    }

    public void refreshDuration() {
        this.elapsedTicks = 0;
    }

    public void applyToZombie(Zombie zombie) {
        this.isApplied = true;

        switch (effectType) {
            case FROZEN:
                zombie.setStopZombieNow(true);
                break;
            case CHILLED:
                this.cSpeed = zombie.getXSpeed();
                if (zombie.getType()== ZombieType.PROSPECTOR_ZOMBIE && ((ProspectorZombie)zombie).isReversedDirection) {
                    this.cSpeed = -zombie.getStableSpeed();
                } else {
                    this.cSpeed = zombie.getStableSpeed();
                }
                zombie.setXSpeed(cSpeed * 0.5);
                this.cAttack = zombie.getAttackPower();
                zombie.setAttackPower((int)(cAttack * 0.5));
                break;
            case GOO_SLOW:
                this.cSpeed = zombie.getXSpeed();
                if (zombie.getType()== ZombieType.PROSPECTOR_ZOMBIE && ((ProspectorZombie)zombie).isReversedDirection) {
                    this.cSpeed = -zombie.getStableSpeed();
                } else {
                    this.cSpeed = zombie.getStableSpeed();
                }
                zombie.setXSpeed(cSpeed * 0.2);
                break;
            case HYPNOTIZED:
                zombie.setHypnotized(true);
                break;
            case STUNNED:
                zombie.setStopZombieNow(true);
                break;
            case POISON:
                break;
        }
    }

    public void removeFromZombie(Zombie zombie) {
        if(!isApplied) return;

        this.isApplied = false;

        switch (effectType) {
            case FROZEN, STUNNED:
                zombie.setStopZombieNow(false);
                break;
            case CHILLED:
                zombie.setXSpeed(this.cSpeed);
                zombie.setAttackPower(this.cAttack);
                break;
            case GOO_SLOW:
                zombie.setXSpeed(this.cSpeed);
                break;
            case HYPNOTIZED:
                zombie.setHypnotized(false);
                break;
            case POISON:
                break;
        }
    }
    public void updateZombieTick(Zombie zombie) {
        if (!isApplied) {
            applyToZombie(zombie);
        }
        if (effectType == EffectType.POISON) {
            zombie.takeDamage(this.tickDamage, DamageType.POISON);
        }
        elapsedTicks++;
        if (isExpired()) {
            removeFromZombie(zombie);
        }
    }
    public float getRemainingTime() {
        return (durationTicks - elapsedTicks) * Constants.Game.TIME_COEFFICIENT;
    }
    public EffectType getEffectType() { return effectType; }
    public int getDurationTicks() { return durationTicks; }
    public int getElapsedTicks() { return elapsedTicks; }
    public boolean isApplied() { return isApplied; }
}
