package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class ZombieCard {
    public final ZombieType zombieType;
    public final int cost;
    public final float cooldownDuration;
    public float currentCooldown = 0f;

    // Added to satisfy ZombieCardActor rendering logic
    public float cooldownTicks;
    public float remainingCooldownTicks;

    public ZombieCard(ZombieType type, int cost, float cooldown) {
        this.zombieType = type;
        this.cost = cost;
        this.cooldownDuration = cooldown;
        this.cooldownTicks = cooldown; // Mapping duration directly to ticks for simplicity
    }

    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
            remainingCooldownTicks = currentCooldown; // Keep UI sync variables updated
        }
    }

    public void resetTimer() {
        this.currentCooldown = this.cooldownDuration;
        this.remainingCooldownTicks = this.cooldownDuration;
    }

    public boolean isReady() {
        return currentCooldown <= 0;
    }

    // Requested by ZombieCardActor to format the label
    public float getRemainingCooldownSeconds() {
        return Math.max(0f, currentCooldown);
    }
}
