package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class HealAndRedirectEffect implements PlantFoodEffectStrategy {
    private final boolean pullsInward;
    private final double rangeTiles;

    public HealAndRedirectEffect(boolean pullsInward, double rangeTiles) {
        this.pullsInward = pullsInward;
        this.rangeTiles = rangeTiles;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        plant.setCurrentHp(plant.getBaseHp() * 2);

        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;

            double distanceX = Math.abs(zombie.getX() - plant.getX());
            double distanceY = Math.abs(zombie.getY() - plant.getY());

            if (pullsInward) {
                if (distanceX <= rangeTiles && distanceY > 0.1 && distanceY <= 1.5) {
                    zombie.setY(plant.getY());
                }
            } else {
                if (distanceY < 0.5) {
                    double newY = plant.getY() + (Math.random() > 0.5 ? 1.0 : -1.0);
                    zombie.setY(newY);
                }
            }
        }

        // Effect resolved! Reset the feed flag.
        plant.resetFeed();
    }
}
