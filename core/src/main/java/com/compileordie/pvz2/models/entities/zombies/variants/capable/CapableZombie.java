package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class CapableZombie extends Zombie {
    protected double abilityCooldown;
    protected double currentCooldownTimer;
    protected int abilityRange;
    protected boolean isAbilityReady;
    protected double delta;

    public CapableZombie(int health, double speed, int base_damage, int row, double startX,
                         double abilityCooldown, int abilityRange, double delta, double x, double y, double xSpeed, double ySpeed, ZombieType type) {
        super(health, speed, base_damage, row, startX, x, y, xSpeed, ySpeed, type);
        this.abilityCooldown = abilityCooldown;
        this.currentCooldownTimer = 0;
        this.abilityRange = abilityRange;
        this.isAbilityReady = true;
        this.delta = delta;
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        // مدیریت زمانی کول‌داون کاملاً مستقل عمل می‌کند
        if (!isAbilityReady) {
            updateCooldown(this.delta);
        }
    }

    // متد انتزاعی که توسط ZombieAbilityService صدا زده می‌شود تا کار اصلی انجام شود
    public abstract void useAbility();

    // سرویس بازی قبل از صدا زدن useAbility این شرط را چک می‌کند
    public boolean canUseAbility() {
        return isAbilityReady && !isDead();
    }

    public void resetCooldown() {
        this.currentCooldownTimer = this.abilityCooldown;
        this.isAbilityReady = false;
    }

    public void updateCooldown(double delta) {
        if (currentCooldownTimer > 0) {
            currentCooldownTimer -= delta;
            if (currentCooldownTimer <= 0) {
                currentCooldownTimer = 0;
                this.isAbilityReady = true;
            }
        }
    }

    public void setDelta(double delta){
        this.delta = delta;
    }

    public int getAbilityRange() { return this.abilityRange; }
}
