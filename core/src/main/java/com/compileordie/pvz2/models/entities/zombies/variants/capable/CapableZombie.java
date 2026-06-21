package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class CapableZombie extends Zombie {
    protected double abilityCooldown;
    protected double currentCooldownTimer;
    protected int abilityRange;
    protected boolean isAbilityReady;

    public CapableZombie(int health, double speed, int base_damage, int row, double startX,
                         double abilityCooldown, int abilityRange) {
        super(health, speed, base_damage, row, startX);
        this.abilityCooldown = abilityCooldown;
        this.currentCooldownTimer = 0;
        this.abilityRange = abilityRange;
        this.isAbilityReady = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        if (!isAbilityReady) {
            updateCooldown();
        }

        if (canUseAbility()) {
            useAbility();
        }
    }

    public abstract void useAbility();

    public boolean canUseAbility() {
        return isAbilityReady && !isDead() && !isEating();
    }

    public void resetCooldown() {
        this.currentCooldownTimer = this.abilityCooldown;
        this.isAbilityReady = false;
    }

    public void updateCooldown() {
        if (currentCooldownTimer > 0) {
            currentCooldownTimer -= 0.05; // فرض بر تیک کلاک بازی
            if (currentCooldownTimer <= 0) {
                currentCooldownTimer = 0;
                this.isAbilityReady = true;
            }
        }
    }
}
