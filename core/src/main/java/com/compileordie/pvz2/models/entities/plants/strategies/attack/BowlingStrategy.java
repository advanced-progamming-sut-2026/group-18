package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class BowlingStrategy implements AttackStrategy {

    private final Class<? extends Projectile> projectileType;
    private final int maxBounces;

    public BowlingStrategy(Class<? extends Projectile> projectileType, int maxBounces) {
        this.projectileType = projectileType;
        this.maxBounces = maxBounces;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        try {
            // The BowlingProjectile constructor takes maxBounces.
            // When it hits a zombie, the projectile's own tick() method handles deflecting to adjacent lanes.
            Projectile proj = projectileType
                .getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                .newInstance(plant.getX(), plant.getY(), 4.0, plant.getBaseDamage(), maxBounces);

            board.getActiveProjectiles().add(proj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
