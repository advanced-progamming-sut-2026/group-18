package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;

public class SunProduceStrategy implements AttackStrategy {

    // Tracks how many times this specific plant has produced sun
    private int productionCycles = 0;

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        String name = plant.getName();
        productionCycles++; // Increment age stage

        double spawnX = plant.getX() + 0.5;
        double spawnY = plant.getY();
        float ground = (float) plant.getY();

        try {
            // 1. Twin Sunflower (100 Suns)
            if (name.equals("Twin Sunflower")) {
                board.economyManager.suns.add(new Sun(spawnX, spawnY, SunType.SPECIAL, false, ground));
                return;
            }

            // 2. Primal Sunflower (75 Suns)
            if (name.equals("Primal Sunflower")) {
                board.economyManager.suns.add(new Sun(spawnX, spawnY, SunType.LARGE, false, ground));
                return;
            }

            // 3. Sun-shroom (Dynamic Growth)
            if (name.equals("Sun-shroom")) {
                SunType shroomType;
                if (productionCycles <= 1) {
                    shroomType = SunType.NORMAL; // Stage 1 (25 suns)
                } else if (productionCycles <= 4) {
                    shroomType = SunType.MEDIUM; // Stage 2 (50 suns)
                } else {
                    shroomType = SunType.LARGE;  // Stage 3 (75 suns)
                }
                board.economyManager.suns.add(new Sun(spawnX, spawnY, shroomType, false, ground));
                return;
            }

            // 4. Gold Bloom (Instant 375, then dies)
            if (name.equals("Gold Bloom")) {
                // 3x SPECIAL (300) + 1x LARGE (75) = 375 Suns
                board.economyManager.suns.add(new Sun(spawnX - 0.3, spawnY, SunType.SPECIAL, false, ground));
                board.economyManager.suns.add(new Sun(spawnX, spawnY, SunType.SPECIAL, false, ground));
                board.economyManager.suns.add(new Sun(spawnX + 0.3, spawnY, SunType.SPECIAL, false, ground));
                board.economyManager.suns.add(new Sun(spawnX, spawnY + 0.3, SunType.LARGE, false, ground));
                plant.die();
                return;
            }

            // 5. Default / Standard Sunflower (50 Suns)
            board.economyManager.suns.add(new Sun(spawnX, spawnY, SunType.MEDIUM, false, ground));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Called by the Plant Food Strategy to instantly max out the Sun-shroom!
    public void forceMaxStage() {
        this.productionCycles = 5;
    }
}
