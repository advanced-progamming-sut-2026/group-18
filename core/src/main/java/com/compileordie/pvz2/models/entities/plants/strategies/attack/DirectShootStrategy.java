package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.*;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.List;

public class DirectShootStrategy implements AttackStrategy {

    private final List<Integer> laneOffsets;
    private final List<double[]> shootVectors;
    private final Class<? extends Projectile> projectileType;
    public DirectShootStrategy(List<Integer> laneOffsets,
                               List<double[]> shootVectors,
                               Class<? extends Projectile> projectileType) {
        this.laneOffsets = laneOffsets;
        this.shootVectors = (shootVectors != null && !shootVectors.isEmpty()) ? shootVectors : List.of(new double[]{1.0, 0.0, 0.0});
        this.projectileType = projectileType;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        // --- Smart Range Radar ---
        double rangeInPixels = plant.getRangeTiles() * Constants.Game.TILE_HEIGHT;
        int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);

        // Scan for any living zombie in the exact same lane that is within the plant's range
        boolean targetExists = board.getAllZombies().stream()
            .anyMatch(z -> !z.isDead() && z.getCurrentRow() == plantRow
                && z.getX() > plant.getX() && z.getX() <= plant.getX() + rangeInPixels);

        if (!targetExists) {
            plant.holdAction = true;
            return;
        }
        // ------------------------------
        double x = plant.getX();
        double y = plant.getY();
        int damage = plant.getBaseDamage();
        double speed = 4.0;
        double maxY = board.totalRows * Constants.Game.TILE_HEIGHT;

        int stackMultiplier = plant.getName().equals("Pea Pod") ? plant.getStackCount() : 1;

        for (int offset : laneOffsets) {
            double spawnY = y + (offset * Constants.Game.TILE_HEIGHT);

            if (spawnY >= 0 && spawnY < maxY) {
                for (double[] vector : shootVectors) {
                    for (int s = 0; s < stackMultiplier; s++) {
                        try {
                            int orderIndex = vector.length > 2 ? (int) (vector[2] + s) : s;
                            double spawnX = x + (orderIndex * 0.2 * Constants.Game.TILE_WIDTH * vector[0]);
                            double finalSpawnY = spawnY + (orderIndex * 0.2 * Constants.Game.TILE_HEIGHT * vector[1]);

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

                            } else if (projectileType == PoisonProjectile.class) {
                                // Goo Peashooter DoT math (Base 6 + Upgrades)
                                int totalPoisonDmg = 6 + plant.getPoisonDmgTickBonus();
                                proj = projectileType
                                    .getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                                    .newInstance(spawnX, finalSpawnY, speed, damage, totalPoisonDmg);

                            } else if (projectileType == FumeProjectile.class) {
                                double maxRangePixels = plant.getRangeTiles() * Constants.Game.TILE_HEIGHT;

                                proj = projectileType
                                    .getDeclaredConstructor(double.class, double.class, double.class, int.class, double.class)
                                    .newInstance(spawnX, finalSpawnY, speed, damage, maxRangePixels);
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
