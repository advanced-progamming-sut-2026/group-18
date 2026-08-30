package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.*;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private boolean hasTargetInVector(GameBoard board, Plant plant, double[] vector, double range) {
        double pX = plant.getX();
        int pRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);

        return board.getAllZombies().stream().anyMatch(z -> {
            if (z.isDead() || z.isHypnotized()) return false;

            double dx = z.getX() - pX;
            int rowDiff = z.getCurrentRow() - pRow;

            if (Math.abs(dx) > range || Math.abs(rowDiff * Constants.Game.TILE_HEIGHT) > range) return false;

            // Horizontal check
            if (Math.abs(vector[1]) < 0.01) {
                return rowDiff == 0 && (vector[0] > 0 ? dx >= -0.1 : dx <= 0.1);
            }

            // Vertical check
            if (Math.abs(vector[0]) < 0.01) {
                return Math.abs(dx) <= (Constants.Game.TILE_WIDTH / 1.5)
                    && (vector[1] > 0 ? rowDiff > 0 : rowDiff < 0);
            }

            // Diagonal raycast
            if (vector[1] > 0 && rowDiff <= 0) return false;
            if (vector[1] < 0 && rowDiff >= 0) return false;
            if (vector[0] > 0 && dx < -Constants.Game.TILE_WIDTH) return false;
            if (vector[0] < 0 && dx > Constants.Game.TILE_WIDTH) return false;

            double expectedDxAtRow = (rowDiff * Constants.Game.TILE_HEIGHT) * (vector[0] / vector[1]);
            return Math.abs(dx - expectedDxAtRow) <= Constants.Game.TILE_WIDTH;
        });
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        boolean isRotobaga = plant.getName().equals("Rotobaga");
        boolean isPeaPod = plant.getName().equals("Pea Pod");
        boolean isStarfruit = plant.getName().equals("Starfruit");
        boolean isFumeShroom = plant.getName().equals("Fume-shroom");
        double rangeInPixels = plant.getRangeTiles() * Constants.Game.TILE_WIDTH;

        plant.isShootingForward = hasTargetInVector(board, plant, new double[]{1.0, 0.0}, rangeInPixels);
        plant.isShootingBackward = hasTargetInVector(board, plant, new double[]{-1.0, 0.0}, rangeInPixels);

        Map<double[], Boolean> vectorTargetMap = new HashMap<>();
        boolean anyTargetExists = false;

        for (double[] vec : shootVectors) {
            boolean hasTarget = hasTargetInVector(board, plant, vec, rangeInPixels);
            vectorTargetMap.put(vec, hasTarget);
            if (hasTarget) anyTargetExists = true;
        }

        if (!anyTargetExists) {
            plant.holdAction = true;
            plant.isWindingUp = false;
            return;
        }

        // --- WINDUP ENGINE ---
        if (!plant.isWindingUp) {
            plant.isWindingUp = true;
            plant.windupTimer = 0;
            plant.holdAction = true;
            return;
        }

        plant.windupTimer += tickDelta;
        plant.holdAction = true;

        double maxWindup = isFumeShroom ? 15 : 7;
        if (plant.windupTimer < maxWindup) {
            return;
        }

        // --- FIRE PROJECTILES ---
        plant.isWindingUp = false;
        plant.holdAction = false;

        double x = plant.getX();
        double y = plant.getY();
        int damage = plant.getBaseDamage();

        // --- FIX 2: Increased speed for snappy gas expansion ---
        double speed = isFumeShroom ? 5.0 : 4.0;
        double tileHeight = Constants.Game.TILE_HEIGHT;
        double maxY = Constants.Game.PADDING_Y + (board.totalRows * tileHeight) + tileHeight;
        int stackMultiplier = isPeaPod ? plant.getStackCount() : 1;
        double gapMultiplier = isRotobaga ? 0.37 : (isPeaPod ? 0.4 : 0.6);

        double baseX = x;
        double baseY = y;
        if (plant.getName().equals("Puff-shroom")) {
            baseX += (Constants.Game.TILE_WIDTH * 0.25);
        }

        for (int offset : laneOffsets) {
            double spawnY = baseY + (offset * tileHeight);
            if (spawnY >= 0 && spawnY < maxY) {
                for (double[] vector : shootVectors) {
                    if (!isStarfruit && !isRotobaga && !vectorTargetMap.getOrDefault(vector, false)) {
                        continue;
                    }

                    // --- FIX 3: Multi-Puff Smoke Stream for Fume-shroom ---
                    if (isFumeShroom && projectileType == FumeProjectile.class) {
                        double maxRangePixels = plant.getRangeTiles() * Constants.Game.TILE_WIDTH;
                        int puffCount = 4; // Spawns 4 overlapping puffs
                        int splitDamage = Math.max(1, damage / puffCount); // Distribute damage evenly

                        for (int p = 0; p < puffCount; p++) {
                            try {
                                double puffOffset = p * (Constants.Game.TILE_WIDTH * 0.35);
                                double spawnX = baseX + puffOffset;

                                Projectile proj = projectileType.getDeclaredConstructor(
                                    double.class, double.class, double.class, int.class, double.class
                                ).newInstance(spawnX, spawnY, speed, splitDamage, maxRangePixels);

                                proj.setSourcePlantType(PlantType.FUME_SHROOM);
                                proj.setXSpeed(speed * vector[0]);
                                proj.setYSpeed(speed * vector[1]);

                                board.getActiveProjectiles().add(proj);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        continue;
                    }

                    // Standard projectile spawning for all other plants
                    for (int s = 0; s < stackMultiplier; s++) {
                        try {
                            int orderIndex = vector.length > 2 ? (int) (vector[2] + s) : s;
                            double spawnX = baseX + (orderIndex * gapMultiplier * Constants.Game.TILE_WIDTH * vector[0]);
                            double finalSpawnY = spawnY + (orderIndex * gapMultiplier * tileHeight * vector[1]);

                            if (isStarfruit) {
                                spawnX += (Constants.Game.TILE_WIDTH * 0.25) * vector[0];
                                finalSpawnY += (Constants.Game.TILE_HEIGHT * 0.25) * vector[1];
                            }

                            Projectile proj;
                            if (projectileType == IceProjectile.class) {
                                double totalChillTime = 100.0 + plant.getChillTimeBonusTicks();
                                proj = projectileType.getDeclaredConstructor(double.class, double.class, double.class, int.class, double.class)
                                    .newInstance(spawnX, finalSpawnY, speed, damage, totalChillTime);
                            } else if (projectileType == PiercingProjectile.class) {
                                int totalPierces = plant.isBlueFlame() ? 9999 : (3 + plant.getPierceBonus());
                                int finalDmg = plant.isBlueFlame() ? 200 : damage;
                                proj = projectileType.getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                                    .newInstance(spawnX, finalSpawnY, speed, finalDmg, totalPierces);
                            } else if (projectileType == PoisonProjectile.class) {
                                int totalPoisonDmg = 6 + plant.getPoisonDmgTickBonus();
                                proj = projectileType.getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                                    .newInstance(spawnX, finalSpawnY, speed, damage, totalPoisonDmg);
                            } else {
                                proj = projectileType.getDeclaredConstructor(double.class, double.class, double.class, int.class)
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
