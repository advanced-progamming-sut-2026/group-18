package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.IceProjectile;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.entities.projectiles.PiercingProjectile;
import java.util.List;

public class DirectShootStrategy implements AttackStrategy {

    private final List<Integer> laneOffsets;
    private final List<int[]> shootVectors;
    private final Class<? extends Projectile> projectileType;
    public DirectShootStrategy(List<Integer> laneOffsets,
                               List<int[]> shootVectors,
                               Class<? extends Projectile> projectileType) {
        this.laneOffsets = laneOffsets;
        this.shootVectors = (shootVectors != null && !shootVectors.isEmpty()) ? shootVectors : List.of(new int[]{1, 0, 0});
        this.projectileType = projectileType;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        double x = plant.getX();
        double y = plant.getY();
        int damage = plant.getBaseDamage();
        double speed = 4.0;
        double tileSize = Constants.Game.TILE_SIZE;
        double maxY = board.totalRows * tileSize;

        int stackMultiplier = plant.getName().equals("Pea Pod") ? plant.getStackCount() : 1;

        for (int offset : laneOffsets) {
            double spawnY = y + (offset * tileSize);

            if (spawnY >= 0 && spawnY < maxY) {
                for (int[] vector : shootVectors) {
                    for (int s = 0; s < stackMultiplier; s++) {
                        try {
                            int orderIndex = vector.length > 2 ? vector[2] + s : s;
                            double spawnX = x + (orderIndex * 0.2 * tileSize * vector[0]);
                            double finalSpawnY = spawnY + (orderIndex * 0.2 * tileSize * vector[1]);

                            Projectile proj;

                            if (projectileType == IceProjectile.class) {
                                double totalChillTime = 100.0 + plant.getChillTimeBonusTicks();
                                proj = projectileType
                                    .getDeclaredConstructor(double.class, double.class, double.class, int.class, double.class)
                                    .newInstance(spawnX, finalSpawnY, speed, damage, totalChillTime);

                            } else if (projectileType == PiercingProjectile.class) {
                                // NEW: Cactus piercing math! (3 base pierces + any upgrades)
                                int totalPierces = 3 + plant.getPierceBonus();
                                proj = projectileType
                                    .getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                                    .newInstance(spawnX, finalSpawnY, speed, damage, totalPierces);

                            } else {
                                proj = projectileType
                                    .getDeclaredConstructor(double.class, double.class, double.class, int.class)
                                    .newInstance(spawnX, finalSpawnY, speed, damage);
                            }

                            proj.setSourcePlantType(PlantType.getByName(plant.getName()));

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
