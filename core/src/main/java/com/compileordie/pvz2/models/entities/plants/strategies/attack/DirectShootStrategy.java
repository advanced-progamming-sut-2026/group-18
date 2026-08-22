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

        boolean isRotobaga = plant.getName().equals("Rotobaga");
        boolean isPeaPod = plant.getName().equals("Pea Pod");

        // --- Smart Bi-Directional Radar ---
        double rangeInPixels = plant.getRangeTiles() * Constants.Game.TILE_SIZE;
        int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
        double plantWorldX = plant.getX() + Constants.Game.PADDING_X_REALITY;

        // Check Forward (Peashooter, Split Pea front)
        boolean forwardTargetExists = board.getAllZombies().stream()
            .anyMatch(z -> !z.isDead() && z.getCurrentRow() == plantRow
                && z.getX() > plantWorldX && z.getX() <= plantWorldX + rangeInPixels);

        // Check Backward (Split Pea back)
        boolean backwardTargetExists = board.getAllZombies().stream()
            .anyMatch(z -> !z.isDead() && z.getCurrentRow() == plantRow
                && z.getX() < plantWorldX && z.getX() >= plantWorldX - rangeInPixels);

        plant.isShootingForward = forwardTargetExists;
        plant.isShootingBackward = backwardTargetExists;

        if (!forwardTargetExists && !backwardTargetExists && !isRotobaga) {
            plant.holdAction = true;
            return;
        }

        double x = plant.getX();
        double y = plant.getY();
        int damage = plant.getBaseDamage();
        double speed = 4.0;
        double tileHeight = Constants.Game.TILE_HEIGHT;
        double maxY = board.totalRows * tileHeight;
        int stackMultiplier = isPeaPod ? plant.getStackCount() : 1;

        // Dynamic Gap: Repeater = 0.6, Rotobaga = 0.37, Pea Pod = 0.2 (Tight!)
        double gapMultiplier = isRotobaga ? 0.37 : (isPeaPod ? 0.4 : 0.6);

        double baseX = x;
        double baseY = y;
        if (isRotobaga) {
            baseX -= (Constants.Game.TILE_WIDTH * 0.2);
            baseY += (Constants.Game.TILE_HEIGHT * 0.045);
        }

        for (int offset : laneOffsets) {
            double spawnY = baseY + (offset * tileHeight);

            if (spawnY >= 0 && spawnY < maxY) {
                for (double[] vector : shootVectors) {

                    // SPLIT PEA LOGIC: Only fire the backward vector if there is a zombie behind!
                    if (!isRotobaga) {
                        if (vector[0] > 0 && !forwardTargetExists) continue;
                        if (vector[0] < 0 && !backwardTargetExists) continue;
                    }

                    for (int s = 0; s < stackMultiplier; s++) {
                        try {
                            int orderIndex = vector.length > 2 ? (int) (vector[2] + s) : s;

                            double spawnX = baseX + (orderIndex * gapMultiplier * Constants.Game.TILE_WIDTH * vector[0]);
                            double finalSpawnY = spawnY + (orderIndex * gapMultiplier * tileHeight * vector[1]);

                            Projectile proj;

                            if (projectileType == IceProjectile.class) {
                                double totalChillTime = 100.0 + plant.getChillTimeBonusTicks();
                                proj = projectileType
                                    .getDeclaredConstructor(double.class, double.class, double.class, int.class, double.class)
                                    .newInstance(spawnX, finalSpawnY, speed, damage, totalChillTime);

                            } else if (projectileType == PiercingProjectile.class) {
                                int totalPierces = 3 + plant.getPierceBonus();
                                proj = projectileType
                                    .getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                                    .newInstance(spawnX, finalSpawnY, speed, damage, totalPierces);

                            } else if (projectileType == PoisonProjectile.class) {
                                int totalPoisonDmg = 6 + plant.getPoisonDmgTickBonus();
                                proj = projectileType
                                    .getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                                    .newInstance(spawnX, finalSpawnY, speed, damage, totalPoisonDmg);

                            } else if (projectileType == FumeProjectile.class) {
                                double maxRangePixels = plant.getRangeTiles() * Constants.Game.TILE_SIZE;
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
