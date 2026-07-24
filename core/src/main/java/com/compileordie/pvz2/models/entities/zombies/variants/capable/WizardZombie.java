package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

import java.util.ArrayList;
import java.util.List;

public class WizardZombie extends CapableZombie {
    public static final int waveCost = 650;
    public static final float abilityCooldown = 7.0f;

    private boolean shouldHex = false;
    private boolean shouldReleaseHex = false;
    private double timer = 0;
    private final List<Object> hexedPlants;

    public WizardZombie(double health, double speed, int attackPower, int row, double startX,
                        double x, double y,
                        double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.WIZARD_ZOMBIE);
        this.hexedPlants = new ArrayList<>();
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        float dt = 1 * Constants.Game.TIME_COEFFICIENT;
        timer += dt;

        if (timer >= abilityCooldown) {
            shouldHex = true;
            timer = 0;
        }
    }

    @Override
    public void handleDeath() {
        this.shouldReleaseHex = true;
        super.handleDeath();
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
            handleDeath();
        }
    }

    public void addHexedPlant(Object plant) {
        if (plant != null) {
            this.hexedPlants.add(plant);
        }
    }

    public List<Object> getHexedPlants() {
        return hexedPlants;
    }

    public boolean shouldWeHex() { return shouldHex; }
    public void stopHex() { this.shouldHex = false; }
    public boolean shouldWeReleaseHex() { return shouldReleaseHex; }
    public void stopReleaseHex() { this.shouldReleaseHex = false; }
}
