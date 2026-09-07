package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.SquashProjectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class FlattenEffect implements PlantFoodEffectStrategy {

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        if (plant.isHidden()) return; // Can't feed while it's in the air!

        // Find a random first victim
        Zombie target = null;
        for (Zombie z : board.getAllZombies()) {
            if (!z.isDead()) {
                target = z; break;
            }
        }

        if (target != null) {
            plant.setHidden(true);
            plant.setExhausted(false); // Plant food resets exhaustion!

            // Launch the first jump! 2 crushes remaining, and isPlantFood = true
            SquashProjectile jumpOut = new SquashProjectile(plant,
                plant.getX(),
                plant.getY(),
                target.getX(),
                target.getY(),
                false,
                2,
                true);
            board.getActiveProjectiles().add(jumpOut);
        }

        plant.resetFeed();
    }
}
