package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.SunProduceStrategy;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;
import com.compileordie.pvz2.models.user.Player;

public class BurstSunEffect implements PlantFoodEffectStrategy {

    private final int sunAmount;

    public BurstSunEffect(int sunAmount) {
        this.sunAmount = sunAmount;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        String name = plant.getName();
        double x = plant.getX();
        double y = plant.getY();
        float ground = (float) plant.getY();

        try {
            if (name.equals("Sun-shroom") && plant.getAttackStrategy() instanceof SunProduceStrategy) {
                plant.forceMaxGrowth();
            }

            // 2. Spawn the correct burst of suns based on the exact plant
            if (name.equals("Sunflower")) {
                // 150 Suns: Spawn 3x MEDIUM (50) suns
                board.economyManager.suns.add(new Sun(x - 0.5, y, SunType.MEDIUM, false, ground));
                board.economyManager.suns.add(new Sun(x, y + 0.5, SunType.MEDIUM, false, ground));
                board.economyManager.suns.add(new Sun(x + 0.5, y, SunType.MEDIUM, false, ground));
            }
            else if (name.equals("Twin Sunflower")) {
                // 250 Suns: Spawn 2x SPECIAL (100) + 1x MEDIUM (50)
                board.economyManager.suns.add(new Sun(x - 0.5, y, SunType.SPECIAL, false, ground));
                board.economyManager.suns.add(new Sun(x, y + 0.5, SunType.MEDIUM, false, ground));
                board.economyManager.suns.add(new Sun(x + 0.5, y, SunType.SPECIAL, false, ground));
            }
            else if (name.equals("Sun-shroom") || name.equals("Primal Sunflower")) {
                // 225 Suns: Spawn 3x LARGE (75) suns
                board.economyManager.suns.add(new Sun(x - 0.5, y, SunType.LARGE, false, ground));
                board.economyManager.suns.add(new Sun(x, y + 0.5, SunType.LARGE, false, ground));
                board.economyManager.suns.add(new Sun(x + 0.5, y, SunType.LARGE, false, ground));
            }
            else {
                // Generic fallback
                board.economyManager.suns.add(new Sun(x, y, SunType.NORMAL, false, ground));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // CRITICAL FIX: Do NOT call plant.resetFeed() here!
        // We will let the Graphics Engine turn off the fed state when the animation physically finishes!
    }
}
