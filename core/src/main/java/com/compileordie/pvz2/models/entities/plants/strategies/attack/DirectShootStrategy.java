package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.List;

public class DirectShootStrategy implements AttackStrategy {

    private final List<Integer> laneOffsets;
    private final Class<? extends Projectile> projectileType;
    private final int projectileCount;

    public DirectShootStrategy(List<Integer> laneOffsets,
                               Class<? extends Projectile> projectileType,
                               int projectileCount) {
        this.laneOffsets = laneOffsets;
        this.projectileType = projectileType;
        this.projectileCount = projectileCount;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        double x = plant.getX();
        double y = plant.getY();
        int damage = plant.getBaseDamage();
        double speed = 4.0;
        double tileSize = Constants.Game.TILE_SIZE;
        double maxY = board.totalRows * tileSize;

        for (int offset : laneOffsets) {
            double spawnY = y + (offset * tileSize);

            // Boundary check to prevent spawning projectiles out of bounds
            if (spawnY >= 0 && spawnY < maxY) {

                // Fire multiple times if projectileCount > 1 (e.g., Repeater)
                for (int i = 0; i < projectileCount; i++) {
                    try {
                        // Offset the X coordinate slightly for multi-shots so they don't overlap perfectly
                        double spawnX = x + (i * 0.2 * tileSize);

                        Projectile proj = projectileType
                            .getDeclaredConstructor(double.class, double.class, double.class, int.class)
                            .newInstance(spawnX, spawnY, speed, damage);

                        board.getActiveProjectiles().add(proj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
