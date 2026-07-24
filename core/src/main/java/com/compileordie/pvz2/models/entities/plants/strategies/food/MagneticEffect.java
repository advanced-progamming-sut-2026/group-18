package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public class MagneticEffect implements PlantFoodEffectStrategy {
    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        int metalStolen = 0;
        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;

            if (zombie.hasMetalArmor()) {
                zombie.removeMetalArmor();
                metalStolen++;
            }
        }
    }
}
