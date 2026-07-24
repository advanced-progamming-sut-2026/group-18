package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

import java.util.List;

public class HomingBurstEffect implements PlantFoodEffectStrategy {
    private final Class<? extends Projectile> projectileType;
    private final int projectileCount;

    public HomingBurstEffect(Class<? extends Projectile> projectileType, int projectileCount) {
        this.projectileType = projectileType;
        this.projectileCount = projectileCount;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        List<Zombie> zombies = board.getAllZombies();
        if (zombies.isEmpty()) return;

        for (int i = 0; i < projectileCount; i++) {
            try {
                Zombie target = zombies.get((int) (Math.random() * zombies.size()));
                if (target.isDead()) continue;

                Projectile proj = projectileType
                    .getDeclaredConstructor(double.class, double.class, double.class, int.class, Zombie.class)
                    .newInstance(plant.getX(), plant.getY(), 6.0, plant.getBaseDamage(), target);

                board.getActiveProjectiles().add(proj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
