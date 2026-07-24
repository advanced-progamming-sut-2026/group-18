package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;

public class RapidFireEffect implements PlantFoodEffectStrategy {
    private final Class<? extends Projectile> projectileType;
    private final int projectileCount;

    public RapidFireEffect(Class<? extends Projectile> projectileType, int projectileCount) {
        this.projectileType = projectileType;
        this.projectileCount = projectileCount;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        for (int i = 0; i < projectileCount; i++) {
            try {
                double spawnX = plant.getX() + (i * 0.5);
                Projectile proj = projectileType
                    .getDeclaredConstructor(double.class, double.class, double.class, int.class)
                    .newInstance(spawnX, plant.getY(), 6.0, plant.getBaseDamage());
                board.getActiveProjectiles().add(proj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
