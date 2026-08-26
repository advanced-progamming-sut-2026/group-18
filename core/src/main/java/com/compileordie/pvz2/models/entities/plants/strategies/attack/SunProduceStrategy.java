package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;

public class SunProduceStrategy implements AttackStrategy {

    private int totalSunsProduced = 0;

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        String name = plant.getName();

        // --- THE WINDUP ENGINE ---
        if (!plant.isWindingUp) {
            plant.isWindingUp = true;
            plant.windupTimer = 0;
            plant.holdAction = true; // Tell Plant.java to pause the main timer!
            return;
        }

        plant.windupTimer += tickDelta;
        plant.holdAction = true;

        // Wait 20 ticks (2 sec?) for the plant to physically glow before popping the sun!
        if (plant.windupTimer < 27) {
            return;
        }

        // --- ANIMATION FINISHED! SPAWN THE SUN! ---
        plant.isWindingUp = false;
        plant.holdAction = false; // RELEASE! Plant.java will now reset the main timer!

        totalSunsProduced++;

        double spawnX = plant.getX() + 0.17;
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
                int stage = plant.getGrowthStage();
                if (stage >= 3) shroomType = SunType.LARGE;
                else if (stage == 2) shroomType = SunType.MEDIUM;
                else shroomType = SunType.NORMAL;
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
                if (plant.hasDoubleSunChance() && Math.random() < 0.4) {
                    board.economyManager.suns.add(new Sun(spawnX + 0.3, spawnY, SunType.MEDIUM, false, ground));
                }
            }

            int xInt = (int) plant.getX();
            int yInt = (int) plant.getY();
            AppModel.addAfterPrompt("plant " + name + " produced a sun at (" + xInt + ", " + yInt + ")");

            if (name.equals("Gold Bloom")) {
                plant.setCurrentHp(0);
                plant.die();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTotalSunsProduced() {
        return this.totalSunsProduced;
    }
}
