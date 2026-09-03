package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.badlogic.gdx.utils.Timer;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.strategies.food.AreaDamageEffect;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class MeleeStrategy implements AttackStrategy {

    private final boolean isAoE;
    private final boolean isInstantKill;

    public MeleeStrategy(boolean isAoE, boolean isInstantKill) {
        this.isAoE = isAoE;
        this.isInstantKill = isInstantKill;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isAlive() || !plant.isArmed()) return;

        int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
        int plantCol = (int) Math.floor((plant.getX() - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH);

        boolean attacked = false;
        int currentDamage = plant.getBaseDamage();
        if (plant.getName().equals("Kiwibeast")) {
            int stage = plant.getGrowthStage();
            currentDamage += (stage - 1) * 15;
        }

        // ==========================================
        // 1. PHAT BEET & KIWIBEAST LOGIC (Pulse + Dynamic Tile Hits)
        // ==========================================
        if (isAoE && !plant.getName().equals("Wasabi Whip")) {
            double radiusPx = Constants.Game.TILE_WIDTH * 1.5;

            // 1. Scan for valid targets before triggering the attack
            boolean hasTarget = false;
            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;
                if (Math.abs(z.getCurrentRow() - plantRow) <= 1 && Math.abs(z.getX() - plant.getX()) <= radiusPx) {
                    hasTarget = true;
                    break;
                }
            }

            if (hasTarget) {
                attacked = true;
                final int finalDamage = currentDamage;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        if (plant.isDead()) return;
                        AreaDamageEffect.spawnVisualHit(board, plant.getX(), plant.getY(), PlantType.getByName(plant.getName()), false);

                        for (Zombie z : board.getAllZombies()) {
                            if (z.isDead()) continue;

                            int zRow = z.getCurrentRow();
                            double distPx = Math.abs(z.getX() - plant.getX());

                            if (Math.abs(zRow - plantRow) <= 1 && distPx <= radiusPx) {
                                z.takeDamage(finalDamage, DamageType.NORMAL, PlantType.getByName(plant.getName()));
                                AreaDamageEffect.spawnTileHit(board, z.getX(), z.getY(), PlantType.getByName(plant.getName()), false);
                            }
                        }
                    }
                }, 0.2f);
            }
        }
        else if (!isInstantKill || plant.getName().equals("Wasabi Whip")) {

            Zombie bestFront = null;
            double closestFrontPx = Double.MAX_VALUE;

            Zombie bestBack = null;
            double closestBackPx = Double.MAX_VALUE;

            int rangeTiles = plant.getName().equals("Wasabi Whip") ? 2 : 1;

            for (Zombie z : board.getAllZombies()) {
                if (z.isDead() || !z.occupiesRow(plantRow)) continue;

                int zCol = (int) Math.floor((z.getX() - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH);
                double distPx = z.getX() - plant.getX();

                if ((zCol >= plantCol && zCol <= plantCol + rangeTiles) && distPx >= 0) {
                    if (distPx < closestFrontPx) {
                        closestFrontPx = distPx;
                        bestFront = z;
                    }
                }
                else if ((zCol <= plantCol && zCol >= plantCol - rangeTiles) && distPx < 0) {
                    if (Math.abs(distPx) < closestBackPx) {
                        closestBackPx = Math.abs(distPx);
                        bestBack = z;
                    }
                }
            }

            Zombie target = null;

            if (bestFront != null && bestBack != null) {
                plant.windupTimer = 3.0;
                target = Math.random() > 0.5 ? bestFront : bestBack;
            } else if (bestFront != null) {
                plant.windupTimer = 1.0;
                target = bestFront;
            } else if (bestBack != null) {
                plant.windupTimer = 2.0;
                target = bestBack;
            }

            if (target != null) {
                DamageType dmgType = plant.getName().equals("Wasabi Whip") ? DamageType.FIRE : DamageType.NORMAL;
                target.takeDamage(currentDamage, dmgType, PlantType.getByName(plant.getName()));
                attacked = true;
            }
        }

        if (!attacked) {
            plant.holdAction = true;
        } else {
            plant.holdAction = false;
        }
    }
}
