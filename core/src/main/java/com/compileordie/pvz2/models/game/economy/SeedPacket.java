package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.models.entities.plants.PlantTemplate;

public class SeedPacket {
    private final PlantTemplate plantTemplate;
    private final double maxCooldownTicks;
    private double currentCooldownTimer = 0;
    private boolean isAvailable = true;

    public SeedPacket(PlantTemplate plantTemplate) {
        this.plantTemplate = plantTemplate;
        // Convert the recharge time from seconds (assuming it's in the template) to ticks
        // If Recharge isn't in PlantTemplate yet, you can add it, or default to 5 seconds (300 ticks)
        this.maxCooldownTicks = 300;
    }

    public void tick(double delta) {
        if (!isAvailable) {
            currentCooldownTimer -= delta;
            if (currentCooldownTimer <= 0) {
                currentCooldownTimer = 0;
                isAvailable = true;
            }
        }
    }

    public void startCooldown() {
        this.isAvailable = false;
        this.currentCooldownTimer = maxCooldownTicks;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public PlantTemplate getTemplate() {
        return plantTemplate;
    }

    // For your UI to draw the grey overlay on the card
    public double getCooldownPercentage() {
        if (isAvailable) return 0.0;
        return currentCooldownTimer / maxCooldownTicks;
    }
}
