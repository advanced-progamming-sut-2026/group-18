package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.GrapeProjectile;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class MineStrategy implements AttackStrategy {

    private final double splashRadiusTiles;

    public MineStrategy(double splashRadiusTiles) {
        this.splashRadiusTiles = splashRadiusTiles;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isArmed()) return;

        int plantRow = (int) (plant.getY() / Constants.Game.TILE_SIZE);
        int plantCol = (int) (plant.getX() / Constants.Game.TILE_SIZE);

        boolean triggered = false;

        // --- INSTANT EXPLOSIVE BYPASS ---
        if (plant.getName().equals("Cherry Bomb") || plant.getName().equals("Grapeshot")) {
            triggered = true; // Bombs detonate instantly, they don't wait for zombies!
        }
        // --- STANDARD TRAP LOGIC ---
        else {
            // 2. Trigger Detection: Did a zombie step exactly into our tile column?
            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;

                if (z.getCurrentRow() == plantRow) {
                    int zCol = (int) (z.getX() / Constants.Game.TILE_SIZE);
                    if (zCol == plantCol) {
                        triggered = true;
                        break;
                    }
                }
            }
        }

        // 3. Detonation Engine
        if (triggered) {
            if (splashRadiusTiles > 0) {
                // --- Primal Potato Mine, Cherry Bomb, Grapeshot (3x3 Splash Damage) ---
                double radiusPixels = splashRadiusTiles * Constants.Game.TILE_SIZE;
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
                        int zCol = (int) (z.getX() / Constants.Game.TILE_SIZE);
                        if (zCol == plantCol) {
                            z.takeDamage(plant.getBaseDamage(), DamageType.EXPLOSIVE, PlantType.getByName(plant.getName()));
                        }
                    }
                }
            }

            // --- THE GRAPESHOT PAYLOAD ---
            if (plant.getName().equals("Grapeshot")) {
                // Base Bounces = 3. Plus upgrades!
                int totalBounces = 3 + plant.getExtraBounces();

                // Spawn EXACTLY 5 grapes!
                for (int i = 0; i < 5; i++) {
                    // Randomize the angle completely (0 to 360 degrees)
                    double randomAngle = Math.random() * Math.PI * 2;

                    // Grapes do exactly 200 damage per your prompt
                    GrapeProjectile grape = new GrapeProjectile(plant.getX(), plant.getY(), randomAngle, 200, totalBounces);
                    board.getActiveProjectiles().add(grape);
                }
            }

            plant.die();
        }
    }
}
