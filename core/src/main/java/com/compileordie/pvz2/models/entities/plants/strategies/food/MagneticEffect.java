package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.BucketHeadZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.KnightZombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class MagneticEffect implements PlantFoodEffectStrategy {
    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;

            if (zombie instanceof KnightZombie) {
                ((KnightZombie) zombie).mushroomAbsorption();
            } else if (zombie instanceof BucketHeadZombie) {
                ((BucketHeadZombie) zombie).mushroomAbsorption();
            }
        }
    }

}
