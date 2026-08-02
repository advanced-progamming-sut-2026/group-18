package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class LobberStrategy implements AttackStrategy {

    private final Class<? extends Projectile> projectileType;
    private final double splashRadiusTiles;

    public LobberStrategy(Class<? extends Projectile> projectileType, double splashRadiusTiles) {
        this.projectileType = projectileType;
        this.splashRadiusTiles = splashRadiusTiles;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        try {
            Projectile proj = projectileType
                .getDeclaredConstructor(double.class, double.class, double.class, int.class, double.class)
                .newInstance(plant.getX(), plant.getY(), 3.5, plant.getBaseDamage(), splashRadiusTiles);

            // Tag the projectile for quests
            proj.setSourcePlantName(plant.getName());

            board.getActiveProjectiles().add(proj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
