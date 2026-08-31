package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.obstacles.Crater;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.GrapeProjectile;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;

public class MineStrategy implements AttackStrategy {

    private final double splashRadiusTiles;

    public MineStrategy(double splashRadiusTiles) {
        this.splashRadiusTiles = splashRadiusTiles;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isAlive()) return;
        if (!plant.isArmed()) return;

        int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
        // --- THE FIX: Subtract PADDING_X to get the true array index (0 to 8)! ---
        int plantCol = (int) Math.floor((plant.getX() - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH);

        boolean triggered = false;

        if (plant.getName().equals("Cherry Bomb") || plant.getName().equals("Grapeshot") || plant.getName().equals("Jalapeno") || plant.getName().equals("Doom-shroom")) {
            triggered = true;
        } else {
            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;
                if (z.getCurrentRow() == plantRow) {
                    // --- THE FIX: Zombie columns get the padding subtraction too ---
                    int zCol = (int) Math.floor((z.getX() - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH);
                    if (zCol == plantCol) {
                        triggered = true;
                        break;
                    }
                }
            }
        }

        if (triggered) {
            if (!plant.isWindingUp) {
                plant.isWindingUp = true;
                plant.windupTimer = 0;
            }

            double oldTimer = plant.windupTimer;
            plant.windupTimer += tickDelta;

            double requiredWindup = 0.0;
            if (plant.getName().equals("Potato Mine") || plant.getName().equals("Primal Potato Mine")) requiredWindup = 10.0;
            else if (plant.getName().equals("Cherry Bomb") || plant.getName().equals("Grapeshot") || plant.getName().equals("Jalapeno")) requiredWindup = 20.0;
            else if (plant.getName().equals("Doom-shroom")) requiredWindup = 75.0; // Your tweaked death timer!
            else if (plant.getName().equals("Iceberg Lettuce")) requiredWindup = 11.0;
            boolean executeDamage = false;
            boolean executeDeath = false;

            if (plant.getName().equals("Doom-shroom")) {
                double damageTick = 60.0;
                if (oldTimer < damageTick && plant.windupTimer >= damageTick) executeDamage = true;
                if (plant.windupTimer >= requiredWindup) executeDeath = true;
            } else {
                if (plant.windupTimer >= requiredWindup) {
                    executeDamage = true;
                    executeDeath = true;
                }
            }

            if (!executeDamage && !executeDeath) {
                return;
            }

            if (executeDamage) {
                if (plant.getName().equals("Jalapeno")) {
                    for (Zombie z : board.getAllZombies()) {
                        if (!z.isDead() && z.getCurrentRow() == plantRow) {
                            z.takeDamage(plant.getBaseDamage(), DamageType.EXPLOSIVE, PlantType.getByName(plant.getName()));
                            z.removeStatusEffect(EffectType.FROZEN);
                            z.removeStatusEffect(EffectType.CHILLED);
                        }
                    }
                } else {
                    if (splashRadiusTiles > 0) {
                        double radiusPixels = splashRadiusTiles * Constants.Game.TILE_HEIGHT;
                        for (Zombie z : board.getAllZombies()) {
                            if (z.isDead()) continue;
                            double dist = Math.hypot(z.getX() - plant.getX(), z.getY() - plant.getY());
                            if (dist <= radiusPixels) {
                                z.takeDamage(plant.getBaseDamage(), DamageType.EXPLOSIVE, PlantType.getByName(plant.getName()));
                            }
                        }
                    } else {
                        for (Zombie z : board.getAllZombies()) {
                            if (!z.isDead() && z.getCurrentRow() == plantRow) {
                                // --- THE FIX: Zombie columns get the padding subtraction too ---
                                int zCol = (int) Math.floor((z.getX() - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH);
                                if (zCol == plantCol) {
                                    z.takeDamage(plant.getBaseDamage(), DamageType.EXPLOSIVE, PlantType.getByName(plant.getName()));
                                }
                            }
                        }
                    }
                }

                if (plant.getName().equals("Doom-shroom")) {
                    Tile centerTile = board.getTile(plantRow, plantCol);
                    if (centerTile != null) {
                        if (centerTile.hasLilyPad || centerTile.isUnderWater()) {
                            centerTile.hasLilyPad = false;
                        } else {
                            centerTile.obstacle = new Crater(plantRow, plantCol, 1800.0);
                        }
                    }
                }

                if (plant.getName().equals("Iceberg Lettuce")) {
                    int baseFreezeTicks = 100;
                    int totalFreeze = baseFreezeTicks + (int) plant.getFreezeTimeBonusTicks();
                    for (Zombie z : board.getAllZombies()) {
                        if (!z.isDead() && z.getCurrentRow() == plantRow) {
                            // --- THE FIX: Zombie columns get the padding subtraction too ---
                            int zCol = (int) Math.floor((z.getX() - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH);
                            if (zCol == plantCol) {
                                z.addEffect(new StatusEffect(EffectType.FROZEN, totalFreeze));
                                break;
                            }
                        }
                    }
                }

                if (plant.getName().equals("Grapeshot")) {
                    int totalBounces = 3 + plant.getExtraBounces();
                    for (int i = 0; i < 4; i++) {
                        double randomAngle = Math.random() * Math.PI * 2;
                        GrapeProjectile grape = new GrapeProjectile(plant.getX(), plant.getY(), randomAngle, 200, totalBounces);
                        board.getActiveProjectiles().add(grape);
                    }
                }
            }

            if (executeDeath) {
                plant.die();
            }
        }
    }
}
