package com.compileordie.pvz2.models.entities.zombies.variants;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.StatusEffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Zombie extends GameEntity {
    protected int health;
    protected int maxHealth;
    protected double movementSpeed; // The base speed
    protected double currentSpeed;  // The active speed after chills/hypnosis
    protected int attackPower;
    protected int currentRow;
    protected List<StatusEffect> activeEffects;
    protected boolean skipThisTick;
    protected boolean isEating;
    protected ZombieType type;

    // NEW: Needed for Caulipower and Hypno-shroom
    protected boolean isHypnotized = false;

    public Zombie(int health, double speed, int base_damage, int row, double startX, double x, double y, double xSpeed, double ySpeed, ZombieType type) {
        super(startX, y, xSpeed, ySpeed);
        this.maxHealth = health;
        this.health = this.maxHealth;
        this.movementSpeed = speed;
        this.currentSpeed = this.movementSpeed;
        this.attackPower = base_damage;
        this.currentRow = row;
        this.activeEffects = new ArrayList<>();
        this.skipThisTick = false;
        this.isEating = false;
        this.type = type;
    }

    public void tick() {
        if (isDead()) {
            handleDeath();
            return;
        }
        skipThisTick = false;

        Iterator<StatusEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            effect.updateZombieTick(this);
            if (effect.isExpired()) {
                // The effect calls removeFromZombie inside updateZombieTick natively now
                iterator.remove();
            }
        }

        if (skipThisTick) return;

        recalculateSpeed();
    }

    public void handleDeath() {}

    @Override
    public void move(int ticks) {
        float dt = ticks * Constants.Game.TIME_COEFFICIENT;
        setXSpeed(this.currentSpeed);
        setX(getX() - getXSpeed() * dt);
    }

    public void startEating() { this.isEating = true; }
    public void stopEating() { this.isEating = false; }
    public boolean isEating() { return this.isEating; }

    public void addEffect(StatusEffect effect) {
        activeEffects.add(effect);
        effect.applyToZombie(this);
    }

    public void removeStatusEffect(StatusEffectType type) {
        activeEffects.removeIf(effect -> {
            if (effect.getEffectType() == type) {
                effect.removeFromZombie(this);
                return true;
            }
            return false;
        });
    }

    public boolean canMove() {
        if (isDead() || isEating) return false;
        for (StatusEffect effect : activeEffects) {
            // NEW: Added STUNNED check for Butter mechanics
            if (effect.getEffectType() == StatusEffectType.FROZEN || effect.getEffectType() == StatusEffectType.STUNNED) {
                return false;
            }
        }
        return true;
    }

    public void recalculateSpeed() {
        double speedModifier = 1.0;
        for (StatusEffect effect : activeEffects) {
            if (effect.getEffectType() == StatusEffectType.CHILLED) {
                speedModifier *= 0.5;
            }
        }

        // Handle hypnotized direction flip
        if (isHypnotized) {
            this.currentSpeed = -Math.abs(this.movementSpeed * speedModifier);
        } else {
            this.currentSpeed = this.movementSpeed * speedModifier;
        }
    }

    public boolean isDead() { return this.health <= 0; }

    public boolean hasEffect(StatusEffectType type) {
        if (activeEffects == null || activeEffects.isEmpty()) return false;
        for (StatusEffect effect : activeEffects) {
            if (effect.getEffectType() == type && effect.isApplied()) return true;
        }
        return false;
    }

    public void setSkip(boolean s) { this.skipThisTick = s; }

    public double getPositionX() { return getX(); }
    public void setPositionX(double positionX) { setX(positionX); }

    public int getHealth() { return health; }
    public int getAttackPower() { return attackPower; }
    public double getCurrentSpeed() { return currentSpeed; }
    public double getBaseSpeed() { return movementSpeed; } // NEW: Needed for Rage/Hypnosis
    public void setMovementSpeed(double speed) { this.movementSpeed = speed; } // NEW: Needed for Rage

    public int getCurrentRow() { return currentRow; }
    public void setCurrentRow(int currentRow) { this.currentRow = currentRow; }

    public boolean isHypnotized() { return isHypnotized; } // NEW
    public void setHypnotized(boolean h) { this.isHypnotized = h; } // NEW

    // NEW: Hook for Jester zombie mechanic referenced in the Combat Engine
    public void startSpinning() { /* Add specific animation/logic for Jester */ }

    public abstract void takeDamage(int amount, DamageType damageType);

    public ZombieType getType() { return type; }
    public void setType(ZombieType type) { this.type = type; }
}
