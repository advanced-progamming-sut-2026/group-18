package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class MagneticEffect implements PlantFoodEffectStrategy {

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        // Standard Plant Food heal
        plant.setCurrentHp(plant.getBaseHp());

        // Plant food rips metal off EVERY zombie on the screen instantly!
//        TODO:
//        for (Zombie zombie : board.getAllZombies()) {
//            if (zombie.isDead()) continue;
//
//            // This cleanly handles Knights, Bucketheads, and any future metal zombies
//            zombie.stripMetalArmor();
//        }

        // Effect resolved! Reset the feed flag.
        plant.resetFeed();
    }
}
