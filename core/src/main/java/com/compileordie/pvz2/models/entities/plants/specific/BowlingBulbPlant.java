package com.compileordie.pvz2.models.entities.plants.specific;

import com.compileordie.pvz2.models.entities.plants.strategies.food.AreaDamageEffect;
import com.compileordie.pvz2.models.entities.plants.variants.ShooterPlants;
import com.compileordie.pvz2.models.entities.plants.variants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.BowlingStrategy;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class BowlingBulbPlant extends ShooterPlants {

    // 0 = Reloading, 1 = Cyan, 2 = Blue, 3 = Orange
    private int currentBulbLevel = 1;
    private int reloadTicks = 0;

    public BowlingBulbPlant(PlantTemplate template, double x, double y) {
        // Pass the BowlingStrategy directly up to the parent!
        super(template, x, y, new BowlingStrategy(), new AreaDamageEffect());
    }

    @Override
    public void tickCore(GameBoard board, int tickDelta) {
        if (currentBulbLevel < 3) {
            reloadTicks += tickDelta;

            // Your exact CSV tick timings
            if (currentBulbLevel == 0 && reloadTicks >= 20) {
                currentBulbLevel = 1; // Cyan reloaded
                reloadTicks = 0;
            } else if (currentBulbLevel == 1 && reloadTicks >= 50) {
                currentBulbLevel = 2; // Upgraded to Blue
                reloadTicks = 0;
            } else if (currentBulbLevel == 2 && reloadTicks >= 100) {
                currentBulbLevel = 3; // Upgraded to Orange
                reloadTicks = 0;
            }
        }
    }

    public int consumeBulb() {
        if (currentBulbLevel == 0) return 0; // Empty ammo

        int firedBulb = currentBulbLevel;
        currentBulbLevel = 0; // Strip the ammo
        reloadTicks = 0;      // Start reloading Cyan

        return firedBulb;
    }

    @Override
    public void applyLevelUpgrade(int newLevel) {
        // Any unique upgrades for Bowling Bulb will go here
    }
}
