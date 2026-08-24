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

    // --- NEW: SMART AAA RAYCAST RADAR ---
    private boolean hasTargetInVector(GameBoard board, Plant plant, double[] vector, double range) {
        double pX = plant.getX() + Constants.Game.PADDING_X_REALITY;
        int pRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);

        return board.getAllZombies().stream().anyMatch(z -> {
            if (z.isDead() || z.isHypnotized()) return false;

            double dx = z.getX() - pX;
            int rowDiff = z.getCurrentRow() - pRow;

            // Out of range check
            if (Math.abs(dx) > range) return false;

            // Pure Horizontal Shots (Peashooter, Split Pea)
            if (Math.abs(vector[1]) < 0.01) {
                return rowDiff == 0 && (vector[0] > 0 ? dx >= 0 : dx <= 0);
            }
            // Pure Vertical Shots (Starfruit Up/Down)
            if (Math.abs(vector[0]) < 0.01) {
                return Math.abs(dx) <= Constants.Game.TILE_WIDTH && (vector[1] > 0 ? rowDiff > 0 : rowDiff < 0);
            }
            // Diagonal Shots (Rotobaga, Starfruit Diagonals)
            boolean correctX = (vector[0] > 0 ? dx >= -20 : dx <= 20); // Small 20px buffer for overlap
            boolean correctY = (vector[1] > 0 ? rowDiff > 0 : rowDiff < 0);
            return correctX && correctY;
        });
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {

        boolean isRotobaga = plant.getName().equals("Rotobaga");
        boolean isPeaPod = plant.getName().equals("Pea Pod");
        boolean isStarfruit = plant.getName().equals("Starfruit");

        double rangeInPixels = plant.getRangeTiles() * Constants.Game.TILE_WIDTH;

        // Legacy flags for simple animations
        plant.isShootingForward = hasTargetInVector(board, plant, new double[]{1.0, 0.0}, rangeInPixels);
        plant.isShootingBackward = hasTargetInVector(board, plant, new double[]{-1.0, 0.0}, rangeInPixels);

        // --- PRE-CALCULATE RADAR FOR ALL VECTORS ---
        Map<double[], Boolean> vectorTargetMap = new HashMap<>();
        boolean anyTargetExists = false;

        for (double[] vec : shootVectors) {
            boolean hasTarget = hasTargetInVector(board, plant, vec, rangeInPixels);
            vectorTargetMap.put(vec, hasTarget);
            if (hasTarget) anyTargetExists = true;
        }

        // If ABSOLUTELY NOTHING is in any of the vector paths, hold fire!
        if (!anyTargetExists) {
            plant.holdAction = true;
            return;
        }

        double x = plant.getX();
        double y = plant.getY();
        int damage = plant.getBaseDamage();
        double speed = plant.getName().equals("Fume-shroom") ? 2.7 : 4.0;
        double tileHeight = Constants.Game.TILE_HEIGHT;
        double maxY = board.totalRows * tileHeight;
        int stackMultiplier = isPeaPod ? plant.getStackCount() : 1;

        double gapMultiplier = isRotobaga ? 0.37 : (isPeaPod ? 0.4 : 0.6);

        double baseX = x;
        double baseY = y;
        if (isRotobaga) {
            baseX -= (Constants.Game.TILE_WIDTH * 0.2);
            baseY += (Constants.Game.TILE_HEIGHT * 0.045);
        }
        else if (plant.getName().equals("Puff-shroom")) {
            baseX += (Constants.Game.TILE_WIDTH * 0.25); // Pushes it forward by 25% of a tile!
        }

        for (int offset : laneOffsets) {
            double spawnY = baseY + (offset * tileHeight);

            if (spawnY >= 0 && spawnY < maxY) {
                for (double[] vector : shootVectors) {

                    // --- TRUE PVZ2 FIRING LOGIC ---
                    // Starfruit shoots all 5 vectors if ANY target exists.
                    // Everyone else (Rotobaga, Split Pea) ONLY shoots the specific vectors that have targets!
                    if (!isStarfruit && !vectorTargetMap.getOrDefault(vector, false)) {
                        continue;
                    }

                    for (int s = 0; s < stackMultiplier; s++) {
                        try {
                            int orderIndex = vector.length > 2 ? (int) (vector[2] + s) : s;

                            double spawnX = baseX + (orderIndex * gapMultiplier * Constants.Game.TILE_WIDTH * vector[0]);
                            double finalSpawnY = spawnY + (orderIndex * gapMultiplier * tileHeight * vector[1]);

                            // STARFRUIT PENTAGON SPAWN LOGIC
                            if (isStarfruit) {
                                spawnX += (Constants.Game.TILE_WIDTH * 0.25) * vector[0];
                                finalSpawnY += (Constants.Game.TILE_HEIGHT * 0.25) * vector[1];
                            }

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
                                double maxRangePixels = plant.getRangeTiles() * Constants.Game.TILE_WIDTH;
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
