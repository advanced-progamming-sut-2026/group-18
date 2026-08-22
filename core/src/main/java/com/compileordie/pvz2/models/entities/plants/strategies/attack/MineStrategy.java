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
        if (!plant.isArmed()) return;

        int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
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

            // --- Jalapeno (Lane-Wide Fire Attack) ---
            if (plant.getName().equals("Jalapeno")) {
                // 1. Burn all zombies in the lane and cleanse ice
                for (Zombie z : board.getAllZombies()) {
                    if (!z.isDead() && z.getCurrentRow() == plantRow) {
                        z.takeDamage(plant.getBaseDamage(), DamageType.EXPLOSIVE, PlantType.getByName(plant.getName()));
                        z.removeStatusEffect(EffectType.FROZEN);
                        z.removeStatusEffect(EffectType.CHILLED);
                    }
                }

                // 2. Melt all Ice Blocks in the lane /// TODO : check this part later
//                for (int c = 0; c < board.totalCols; c++) {
//                    Tile t = board.getTile(plantRow, c);
//                      if (t != null && t.obstacle instanceof com.compileordie.pvz2.models.entities.obstacles.IceBlock) {
//                        // Blast the ice block with massive damage so it instantly breaks
//                        ((com.compileordie.pvz2.models.entities.obstacles.IceBlock) t.obstacle).takeDamage(9999, ProjectileType.FIRE);
//                    }
//
//                    // Also unfreeze any plants in this lane that were frozen!
//                    if (t != null && t.plant != null && t.plant.hasActiveCover()) {
//                        t.plant.takeDamage(9999); // Damages the cover (ice), freeing the plant
//                    }
//                }
            }
            // --- RADIAL & SINGLE TILE BOMBS ---
            else {
                if (splashRadiusTiles > 0) {
                    // --- Primal Potato Mine, Cherry Bomb, Grapeshot (3x3 Splash Damage) ---
                    double radiusPixels = splashRadiusTiles * Constants.Game.TILE_HEIGHT;
                    for (Zombie z : board.getAllZombies()) {
                        if (z.isDead()) continue;

                        double dist = Math.hypot(z.getX() - plant.getX(), z.getY() - plant.getY());
                        if (dist <= radiusPixels) {
                            z.takeDamage(plant.getBaseDamage(), DamageType.EXPLOSIVE, PlantType.getByName(plant.getName()));
                        }
                    }
                } else {
                    // --- Normal Potato Mine (Single Tile Damage) ---
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
                    // --- THE DOOM-SHROOM PAYLOAD (CRATER) ---
                        if (plant.getName().equals("Doom-shroom")) {
                            Tile centerTile = board.getTile(plantRow, plantCol);
                                if (centerTile != null) {
                                    // --- LILY PAD & POOL CHECK ---
                                    if (centerTile.hasLilyPad || centerTile.isUnderWater()) {
                                        // The Lily Pad absorbs the blast! No crater is formed. We "destroy" the Lily Pad by resetting the tile's flag:
                                        centerTile.hasLilyPad = false;
                                        } else {
                                            // Solid ground gets a crater! 1800 ticks = 180 seconds!
                                            centerTile.obstacle = new Crater(plantRow, plantCol, 1800.0);
                                        }
                                }
                        }

            // --- ICEBERG LETTUCE PAYLOAD ---
            if (plant.getName().equals("Iceberg Lettuce")) {
                // Base trap freeze is usually 10 seconds (100 ticks)
                int baseFreezeTicks = 100;
                int totalFreeze = baseFreezeTicks + (int) plant.getFreezeTimeBonusTicks();

                for (Zombie z : board.getAllZombies()) {
                    if (!z.isDead() && z.getCurrentRow() == plantRow) {
                        int zCol = (int) (z.getX() / Constants.Game.TILE_WIDTH);
                        if (zCol == plantCol) {

                            // Freeze the zombie!
                            z.addEffect(new StatusEffect(EffectType.FROZEN, totalFreeze));
                            // Iceberg Lettuce only freezes the FIRST zombie it touches!
                            break;
                        }
                    }
                }
            }
            // --- THE GRAPESHOT PAYLOAD ---
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
