package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.List;

public class DirectShootStrategy implements AttackStrategy {

    private final List<Integer> laneOffsets;
    private final List<int[]> shootVectors; // NEW: [X, Y] direction arrays
    private final Class<? extends Projectile> projectileType;
    private final int projectileCount;

    public DirectShootStrategy(List<Integer> laneOffsets,
                               List<int[]> shootVectors,
                               Class<? extends Projectile> projectileType,
                               int projectileCount) {
        this.laneOffsets = laneOffsets;
        // Default to forward [1, 0] if no special vectors are provided
        this.shootVectors = (shootVectors != null && !shootVectors.isEmpty()) ? shootVectors : List.of(new int[]{1, 0});
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

            if (spawnY >= 0 && spawnY < maxY) {
                for (int i = 0; i < projectileCount; i++) {

                    // NEW: Loop through all assigned shooting vectors
                    for (int[] vector : shootVectors) {
                        try {
                            double spawnX = x + (i * 0.2 * tileSize);

                            Projectile proj = projectileType
                                .getDeclaredConstructor(double.class, double.class, double.class, int.class)
                                .newInstance(spawnX, spawnY, speed, damage);

                            // NEW: Multiply base speed by vector direction
                            proj.setXSpeed(speed * vector[0]);
                            proj.setYSpeed(speed * vector[1]);

                            board.getActiveProjectiles().add(proj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
