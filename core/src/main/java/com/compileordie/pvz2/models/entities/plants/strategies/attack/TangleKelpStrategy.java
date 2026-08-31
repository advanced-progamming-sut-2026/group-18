package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class TangleKelpStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isAlive() || !plant.isArmed()) return;

        int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
        int plantCol = (int) Math.floor((plant.getX() - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH);

        // 1. TRIGGER PHASE: Check if a zombie is on the tile
        if (!plant.isWindingUp) {
            boolean zombieInRange = false;
            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;
                if (z.getCurrentRow() == plantRow) {
                    int zCol = (int) Math.floor((z.getX() - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH);
                    if (zCol == plantCol) {
                        zombieInRange = true;
                        break;
                    }
                }
            }

            if (zombieInRange) {
                plant.isWindingUp = true;
                plant.windupTimer = 0;
            }
        }

        // 2. ANIMATION & DAMAGE PHASE
        if (plant.isWindingUp) {
            double oldTimer = plant.windupTimer;
            plant.windupTimer += tickDelta;

            double damageTick = 15.0; // Damage hits after it dives!
            double deathTick = 60.0;  // Total time to play submerge -> attack -> merge

            // DEAL DAMAGE EXACTLY AT TICK 15
            if (oldTimer < damageTick && plant.windupTimer >= damageTick) {
                int maxTargets = 1 + plant.getExtraTargets();
                int targetsHit = 0;

                for (Zombie z : board.getAllZombies()) {
                    if (z.isDead()) continue;
                    if (z.getCurrentRow() == plantRow) {
                        int zCol = (int) Math.floor((z.getX() - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH);
                        if (zCol == plantCol) {
                            z.takeDamage(1300, DamageType.NORMAL, PlantType.getByName(plant.getName()));
                            targetsHit++;

                            if (targetsHit >= maxTargets) {
                                break;
                            }
                        }
                    }
                }
            }

            // DIE AT TICK 60
            if (plant.windupTimer >= deathTick) {
                plant.die();
            }
        }
    }
}
