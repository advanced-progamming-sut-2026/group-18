package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class LaneClearEffect implements PlantFoodEffectStrategy {
    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;

            if (Math.abs(zombie.getY() - plant.getY()) < 0.5) {
                zombie.takeDamage(2000, DamageType.NORMAL);
            }
        }

        // Effect resolved! Reset the feed flag.
        plant.resetFeed();
    }
}
