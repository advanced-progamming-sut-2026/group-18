package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class AreaDamageEffect implements PlantFoodEffectStrategy {
    private final int damageAmount;

    public AreaDamageEffect(int damageAmount) {
        this.damageAmount = damageAmount;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        double radius = 3.0 * Constants.Game.TILE_SIZE;

        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;

            double distance = Math.hypot(zombie.getX() - plant.getX(), zombie.getY() - plant.getY());
            if (distance <= radius) {
                zombie.takeDamage(damageAmount, DamageType.EXPLOSIVE);
            }
        }

        // Effect resolved! Reset the feed flag.
        plant.resetFeed();
    }
}
