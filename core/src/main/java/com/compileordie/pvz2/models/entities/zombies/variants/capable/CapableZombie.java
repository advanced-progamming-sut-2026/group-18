package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

//Zs :
//RaZombie
//ExplorerZombie
//HunterZombie
//FishermanZombie
//OctopusZombie
//WizardZombie
//TurquoiseZombie
//PianistZombie
//JesterZombie

public abstract class CapableZombie extends Zombie {
    protected double abilityCooldown;
    protected double currentCooldownTimer;
    protected int abilityRange;
    protected boolean isAbilityReady;
    protected double delta;

    public CapableZombie(int health, double speed, int base_damage, int row, double startX,
                         double abilityCooldown, int abilityRange, double delta, double x, double y, int xSpeed, int ySpeed) {
        super(health, speed, base_damage, row, startX, x, y, xSpeed, ySpeed);
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

        if (!isAbilityReady) {
            updateCooldown(this.delta);
        }

        if (canUseAbility()) {
            useAbility();
        }
    }

    public abstract void useAbility();

    public boolean canUseAbility() {
        return isAbilityReady && !isDead();
    }

    public void resetCooldown() {
        this.currentCooldownTimer = this.abilityCooldown;
        this.isAbilityReady = false;
    }

    public void updateCooldown(double delta) {
        if (currentCooldownTimer >= 0) {
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
}
