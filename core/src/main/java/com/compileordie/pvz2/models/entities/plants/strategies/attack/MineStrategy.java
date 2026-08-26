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
        // --- FIX BUG 2: THE GHOST BOMB ---
        // If the plant is dead, abort! This stops the invisible infinite explosions!
        if (!plant.isAlive()) return;

        if (!plant.isArmed()) return;

        int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
        int plantCol = (int) (plant.getX() / Constants.Game.TILE_WIDTH);

        boolean triggered = false;

        // --- INSTANT EXPLOSIVE BYPASS ---
        if (plant.getName().equals("Cherry Bomb") || plant.getName().equals("Grapeshot") || plant.getName().equals("Jalapeno")|| plant.getName().equals("Doom-shroom")) {
            triggered = true; // Bombs detonate instantly, they don't wait for zombies!
        }
        // --- STANDARD TRAP LOGIC ---
        else {
            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;
                if (z.getCurrentRow() == plantRow) {
                    int zCol = (int) (z.getX() / Constants.Game.TILE_WIDTH);
                    if (zCol == plantCol) {
                        triggered = true;
                        break;
                    }
                }
            }
        }

        // 3. Detonation Engine
        if (triggered) {

            // --- THE WINDUP HOOK ---
            if (!plant.isWindingUp) {
                plant.isWindingUp = true;
                plant.windupTimer = 0;
            }

            plant.windupTimer += tickDelta;

            double requiredWindup = 0.0;
            if (plant.getName().equals("Potato Mine")) requiredWindup = 10.0;
                // --- FIX BUG 1 (Part 1): Set 20-tick fuse (10 for idle, 10 for attack) ---
            else if (plant.getName().equals("Cherry Bomb")) requiredWindup = 20.0;

            if (plant.windupTimer < requiredWindup) {
                return;
            }

            plant.isWindingUp = false;

            // --- DAMAGE EXECUTION ---
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
                            int zCol = (int) (z.getX() / Constants.Game.TILE_WIDTH);
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
                        int zCol = (int) (z.getX() / Constants.Game.TILE_WIDTH);
                        if (zCol == plantCol) {
                            z.addEffect(new StatusEffect(EffectType.FROZEN, totalFreeze));
                            break;
                        }
                    }
                }
            }

            if (plant.getName().equals("Grapeshot")) {
                int totalBounces = 3 + plant.getExtraBounces();
                for (int i = 0; i < 5; i++) {
                    double randomAngle = Math.random() * Math.PI * 2;
                    GrapeProjectile grape = new GrapeProjectile(plant.getX(), plant.getY(), randomAngle, 200, totalBounces);
                    board.getActiveProjectiles().add(grape);
                }
            }

            plant.die();
        }
    }
}
