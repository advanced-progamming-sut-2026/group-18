package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;

public class SunProduceStrategy implements AttackStrategy {

    // Safely isolated for your Daily Quest system!
    private int totalSunsProduced = 0;

    // NEW: Dedicated flag specifically for the Plant Food override
    private boolean isMaxStageForced = false;

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        String name = plant.getName();
        totalSunsProduced++; // Safely increments for quests without breaking growth

        double spawnX = plant.getX() + 0.5;
        double spawnY = plant.getY();
        float ground = (float) plant.getY();

        try {
            // 1. Twin Sunflower (100 Suns)
            if (name.equals("Twin Sunflower")) {
                board.economyManager.suns.add(new Sun(spawnX, spawnY, SunType.SPECIAL, false, ground));
            }
            // 2. Primal Sunflower (75 Suns)
            else if (name.equals("Primal Sunflower")) {
                board.economyManager.suns.add(new Sun(spawnX, spawnY, SunType.LARGE, false, ground));
            }
            // 3. Sun-shroom (True Time-Based Dynamic Growth)
            else if (name.equals("Sun-shroom")) {
                SunType shroomType;

                double age = plant.getAgeTicks();
                double stg2Threshold = Math.max(0, 240.0 - plant.getGrowTimeReductionTicks());
                double stg3Threshold = Math.max(0, 720.0 - plant.getGrowTimeReductionTicks());

                // Checked first: If fed Plant Food, it is locked at Stage 3 permanently
                if (isMaxStageForced) {
                    shroomType = SunType.LARGE;  // Stage 3 (75 suns)
                }
                // Checks true age against the thresholds
                else if (age >= stg3Threshold) {
                    shroomType = SunType.LARGE;  // Stage 3 (75 suns)
                }
                else if (age >= stg2Threshold) {
                    shroomType = SunType.MEDIUM; // Stage 2 (50 suns) - Happens at exactly 24s!
                }
                else {
                    shroomType = SunType.NORMAL; // Stage 1 (25 suns) - Happens on immediate spawn!
                }

                board.economyManager.suns.add(new Sun(spawnX, spawnY, shroomType, false, ground));
            }
            // 4. Gold Bloom (Instant 375, then dies)
            else if (name.equals("Gold Bloom")) {
                board.economyManager.suns.add(new Sun(spawnX - 0.3, spawnY, SunType.SPECIAL, false, ground));
                board.economyManager.suns.add(new Sun(spawnX, spawnY, SunType.SPECIAL, false, ground));
                board.economyManager.suns.add(new Sun(spawnX + 0.3, spawnY, SunType.SPECIAL, false, ground));
                board.economyManager.suns.add(new Sun(spawnX, spawnY + 0.3, SunType.LARGE, false, ground));
                if(plant.getExtraSunYield() >= 50) board.economyManager.suns.add(new Sun(spawnX + 0.3, spawnY + 0.3, SunType.MEDIUM, false, ground));
            }
            // 5. Default / Standard Sunflower (50 Suns)
            else {
                board.economyManager.suns.add(new Sun(spawnX, spawnY, SunType.MEDIUM, false, ground));
                // Add the Level 4 perk!
                if (plant.hasDoubleSunChance() && Math.random() < 0.4) {
                    board.economyManager.suns.add(new Sun(spawnX + 0.3, spawnY, SunType.MEDIUM, false, ground));
                }
            }

            // 6. Trigger the custom UI Message Queue!
            int xInt = (int) plant.getX();
            int yInt = (int) plant.getY();
            AppModel.addAfterPrompt("plant " + name + " produced a sun at (" + xInt + ", " + yInt + ")");

            // 7. Cleanup Gold Bloom
            if (name.equals("Gold Bloom")) {
                plant.die();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Called by BurstSunEffect to flip the dedicated switch!
    public void forceMaxStage() {
        this.isMaxStageForced = true;
    }

    // For your quest engine to check how many suns this specific plant yielded
    public int getTotalSunsProduced() {
        return this.totalSunsProduced;
    }
}
