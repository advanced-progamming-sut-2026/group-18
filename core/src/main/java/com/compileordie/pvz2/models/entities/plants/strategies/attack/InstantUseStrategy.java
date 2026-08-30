package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;

public class InstantUseStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {

        // --- THE FIX: The Ghost Lock! ---
        // If the plant is dead, or if we already triggered the explosion, DO NOTHING!
        if (!plant.isAlive() || plant.holdAction) return;

        String name = plant.getName();

        // --- ICE-SHROOM LOGIC ---
        if (name.equals("Ice-shroom")) {

            // 1. WIND-UP DELAY
            if (plant.windupTimer == 0) {
                plant.windupTimer = 1.0;
                plant.isWindingUp = true;
                return;
            } else {
                plant.windupTimer += tickDelta;
                // Wait 24 ticks (~1.2 seconds) for his face to squeeze before freezing!
                if (plant.windupTimer < 24.0) {
                    return;
                }
            }

            // 2. EXECUTE THE FREEZE!
            int totalFreezeTicks = (int) (240.0 + plant.getFreezeTimeBonusTicks());

            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;

                z.addEffect(new StatusEffect(EffectType.FROZEN, totalFreezeTicks));

                if (plant.getBaseDamage() > 0) {
                    z.takeDamage(plant.getBaseDamage(), DamageType.ICE, PlantType.getByName(name));
                }
            }

            // 3. SECURE THE LOCK AND DIE
            plant.holdAction = true; // Prevents the infinite freeze bug!
            plant.die();
            return;
        }

        // --- HOT POTATO LOGIC ---
        else if (name.equals("Hot Potato")) {
            int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
            int plantCol = (int) (plant.getX() / Constants.Game.TILE_WIDTH);

            boolean is3x3 = plant.hasMeltArea3x3();

            int rowStart = is3x3 ? plantRow - 1 : plantRow;
            int rowEnd = is3x3 ? plantRow + 1 : plantRow;
            int colStart = is3x3 ? plantCol - 1 : plantCol;
            int colEnd = is3x3 ? plantCol + 1 : plantCol;

            for (int r = rowStart; r <= rowEnd; r++) {
                for (int c = colStart; c <= colEnd; c++) {
                    if (r >= 0 && r < board.totalRows && c >= 0 && c < board.totalCols) {
                        Tile t = board.getTile(r, c);
                        if (t != null) {
                            if (t.plant != null && t.plant.hasActiveCover()) {
                                t.plant.takeDamage(9999);
                            }
                        }
                    }
                }
            }

            if (plant.isExplodesOnDeath()) {
                plant.setBaseDamage(800);
            }
        }

        // --- GRAVE BUSTER LOGIC ---
        else if (name.equals("Grave Buster")) {
            int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
            int plantCol = (int) (plant.getX() / Constants.Game.TILE_WIDTH);

            Tile t = board.getTile(plantRow, plantCol);

            if (t != null && t.obstacle instanceof Tomb) {
                ((Tomb) t.obstacle).takeDamage(9999, ProjectileType.NORMAL);
            }

            if (plant.isExplodesOnDeath()) {
                plant.setBaseDamage(1800);
            }
        }

        // Generic cleanup for Hot Potato & Grave Buster
        plant.holdAction = true; // Lock these too just in case!
        plant.die();
    }
}
