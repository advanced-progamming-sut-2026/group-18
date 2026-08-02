package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.SunProduceStrategy;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;
import com.compileordie.pvz2.models.user.Player;

public class BurstSunEffect implements PlantFoodEffectStrategy {

    // We keep this in case your factory passes it, but we will rely on the plant's name for precision
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
            // 1. Force the Sun-shroom to grow to its maximum stage instantly!
            if (plant.getAttackStrategy() instanceof SunProduceStrategy) {
                ((SunProduceStrategy) plant.getAttackStrategy()).forceMaxStage();
            }

            // 2. Spawn the correct burst of suns based on the exact plant
            if (name.equals("Sunflower")) {
                // 150 Suns: Spawn 3x MEDIUM (50) suns
                board.economyManager.suns.add(new Sun(x - 0.3, y, SunType.MEDIUM, false, ground));
                board.economyManager.suns.add(new Sun(x, y + 0.3, SunType.MEDIUM, false, ground));
                board.economyManager.suns.add(new Sun(x + 0.3, y, SunType.MEDIUM, false, ground));
            }
            else if (name.equals("Twin Sunflower")) {
                // 250 Suns: Spawn 2x SPECIAL (100) + 1x MEDIUM (50)
                board.economyManager.suns.add(new Sun(x - 0.3, y, SunType.SPECIAL, false, ground));
                board.economyManager.suns.add(new Sun(x, y + 0.3, SunType.MEDIUM, false, ground));
                board.economyManager.suns.add(new Sun(x + 0.3, y, SunType.SPECIAL, false, ground));
            }
            else if (name.equals("Sun-shroom") || name.equals("Primal Sunflower")) {
                // 225 Suns: Spawn 3x LARGE (75) suns
                board.economyManager.suns.add(new Sun(x - 0.3, y, SunType.LARGE, false, ground));
                board.economyManager.suns.add(new Sun(x, y + 0.3, SunType.LARGE, false, ground));
                board.economyManager.suns.add(new Sun(x + 0.3, y, SunType.LARGE, false, ground));
            }
            else {
                // Generic fallback just in case a custom plant triggers this
                board.economyManager.suns.add(new Sun(x, y, SunType.NORMAL, false, ground));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Effect resolved! Reset the feed flag.
        plant.resetFeed();
    }
}
