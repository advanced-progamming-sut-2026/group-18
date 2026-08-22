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
        String name = plant.getName();

        // --- ICE-SHROOM LOGIC ---
        if (name.equals("Ice-shroom")) {
            // Base freeze time (e.g., 4 seconds = 240 ticks) + Upgrade bonus
            int totalFreezeTicks = (int) (240.0 + plant.getFreezeTimeBonusTicks());

            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;

                // 1. Freeze every zombie on the board!
                z.addEffect(new StatusEffect(EffectType.FROZEN, totalFreezeTicks));

                // 2. Deal damage! (Base is 0, but Upgrade 3 adds +50 damage)
                if (plant.getBaseDamage() > 0) {
                    z.takeDamage(plant.getBaseDamage(), DamageType.ICE, PlantType.getByName(name));
                }
            }
        }
// --- HOT POTATO LOGIC ---
        else if (name.equals("Hot Potato")) {
            int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
            int plantCol = (int) (plant.getX() / Constants.Game.TILE_WIDTH);

            // Check if we have the Level 3 upgrade!
            boolean is3x3 = plant.hasMeltArea3x3();

            int rowStart = is3x3 ? plantRow - 1 : plantRow;
            int rowEnd = is3x3 ? plantRow + 1 : plantRow;
            int colStart = is3x3 ? plantCol - 1 : plantCol;
            int colEnd = is3x3 ? plantCol + 1 : plantCol;

            for (int r = rowStart; r <= rowEnd; r++) {
                for (int c = colStart; c <= colEnd; c++) {
                    // Make sure don't check outside the board boundaries:
                    if (r >= 0 && r < board.totalRows && c >= 0 && c < board.totalCols) {
                        Tile t = board.getTile(r, c);
                        if (t != null) {

                            // 1. Melt IceBlock obstacles!
                            // TODO:
//                            if (t.obstacle instanceof com.compileordie.pvz2.models.entities.obstacles.IceBlock) {
//                                ((com.compileordie.pvz2.models.entities.obstacles.IceBlock) t.obstacle).takeDamage(9999, com.compileordie.pvz2.models.entities.plants.enums.ProjectileType.FIRE);
//                            }

                            // 2. Melt frozen plants! (Damage goes entirely to the Ice Cover)
                            if (t.plant != null && t.plant.hasActiveCover()) {
                                t.plant.takeDamage(9999);
                            }
                        }
                    }
                }
            }

            // CRITICAL: Hot Potato's base damage in the CSV is 0.
            // If it has the Explode on Finish upgrade, we give it bomb damage right before it dies!
            // When plant.die() is called below, it will naturally use triggerExplosion()!
            if (plant.isExplodesOnDeath()) {
                plant.setBaseDamage(800);
            }
        }

        // --- GRAVE BUSTER LOGIC ---
        else if (name.equals("Grave Buster")) {
            int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
            int plantCol = (int) (plant.getX() / Constants.Game.TILE_WIDTH);

            Tile t = board.getTile(plantRow, plantCol);

            // Look for the Tomb on this exact tile
            if (t != null && t.obstacle instanceof Tomb) {

                // Instantly destroy the grave!
                // Using the exact signature from your Projectile.java logic: takeDamage(int damage, ProjectileType type)
                ((Tomb) t.obstacle).takeDamage(9999, ProjectileType.NORMAL);
            }

            // CRITICAL: Just like Hot Potato, if it has the Explode on Finish upgrade,
            // we give it massive bomb damage right before it dies!
            if (plant.isExplodesOnDeath()) {
                plant.setBaseDamage(1800);
            }
        }
        // After an instant plant triggers its effect, it destroys itself!
        plant.die();
    }
}
