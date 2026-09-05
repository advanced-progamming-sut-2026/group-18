package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.plants.strategies.food.AreaDamageEffect;
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
        if (!plant.isAlive() || plant.holdAction) return;

        String name = plant.getName();

        // --- ICE-SHROOM LOGIC ---
        if (name.equals("Ice-shroom")) {
            if (plant.windupTimer == 0) {
                plant.windupTimer = 1.0;
                plant.isWindingUp = true;
                return;
            } else {
                plant.windupTimer += tickDelta;
                if (plant.windupTimer < 24.0) return; // Wait 0.4s for animation
            }

            int totalFreezeTicks = (int) (240.0 + plant.getFreezeTimeBonusTicks());
            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;
                z.addEffect(new StatusEffect(EffectType.FROZEN, totalFreezeTicks));
                if (plant.getBaseDamage() > 0) z.takeDamage(plant.getBaseDamage(), DamageType.ICE, PlantType.getByName(name));
            }
            plant.holdAction = true;
            plant.die();
            return;
        }

        // --- HOT POTATO LOGIC ---
        else if (name.equals("Hot Potato")) {
            if (plant.windupTimer == 0) {
                plant.windupTimer = 1.0;
                plant.isWindingUp = true;
                plant.holdAction = true; // Lock it!
                return;
            } else {
                plant.windupTimer += tickDelta;
                if (plant.windupTimer < 90.0) {
                    plant.holdAction = true;
                    return; // Wait 1.5s for melting animation
                }
            }
            int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
            int plantCol = (int) Math.floor((plant.getX() - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH);

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
                            boolean meltedSomething = false;

                            // Melt Ice Block obstacles
                            if (t.obstacle instanceof com.compileordie.pvz2.models.entities.obstacles.IceBlock) {
                                ((com.compileordie.pvz2.models.entities.obstacles.IceBlock) t.obstacle).takeDamage(9999, ProjectileType.FIRE);
                                meltedSomething = true;
                            }

                            // Melt ALL Plant Slots
                            if (t.plant != null && t.plant.hasActiveCover()) { t.plant.takeDamage(9999); meltedSomething = true; }
                            if (t.pumpkin != null && t.pumpkin.hasActiveCover()) { t.pumpkin.takeDamage(9999); meltedSomething = true; }
                            if (t.lilyPad != null && t.lilyPad.hasActiveCover()) { t.lilyPad.takeDamage(9999); meltedSomething = true; }

                            // Trigger the puddle visual if ice was successfully broken!
                            if (meltedSomething) {
                                double pX = Constants.Game.PADDING_X + (c * Constants.Game.TILE_WIDTH) + (Constants.Game.TILE_WIDTH / 2.0);
                                double pY = Constants.Game.PADDING_Y + (r * Constants.Game.TILE_HEIGHT) + (Constants.Game.TILE_HEIGHT / 2.0);
                                AreaDamageEffect.spawnPuddle(board, pX, pY);
                            }
                        }
                    }
                }
            }

            if (plant.isExplodesOnDeath()) plant.setBaseDamage(1800);
            plant.holdAction = true;
            plant.die();
        }

        // --- GRAVE BUSTER LOGIC (EMERGENCY SAFE MODE) ---
        else if (name.equals("Grave Buster")) {
            // Do absolutely nothing. Just sit on the grave safely so the game doesn't crash during the demo!
            plant.holdAction = true;
            return;
        }
    }
}
