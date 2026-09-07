package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.HashSet;
import java.util.Set;

public class ModifierPassiveStrategy implements AttackStrategy {

    private static final Set<Projectile> BLUE_PROCESSED = new HashSet<>();

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isAlive() || !plant.getName().equals("Torchwood")) return;

        BLUE_PROCESSED.removeIf(proj -> !board.getActiveProjectiles().contains(proj));

        int plantRow = (int) Math.round((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
        boolean isBlueTorchwood = plant.isBlueFlame() || plant.getLevel() >= 3;

        for (Projectile p : board.getActiveProjectiles()) {

            PlantType source = p.getSourcePlantType();
            boolean isPea = (source == null ||
                source == PlantType.PEASHOOTER ||
                source == PlantType.REPEATER ||
                source == PlantType.THREEPEATER ||
                source == PlantType.PEA_POD ||
                source == PlantType.SPLIT_PEA ||
                source == PlantType.SNOW_PEA ||
                source == PlantType.MEGA_GATLING_PEA ||
                source == PlantType.FIRE_PEASHOOTER);

            if (!isPea) continue;

            // --- FIX 1: DEFINE "ALREADY FIRE" ---
            // If it hit a previous Torchwood OR it came from a Fire Peashooter!
            boolean isAlreadyFire = p.isIgnited() || source == PlantType.FIRE_PEASHOOTER;

            // --- FIX 2: STRICT ENGINE LOCKS ---
            if (isBlueTorchwood) {
                if (BLUE_PROCESSED.contains(p)) continue;
            } else {
                // Normal Torchwood MUST completely ignore peas that are already on fire!
                // This stops the 40 damage from multiplying to 80!
                if (isAlreadyFire) continue;
            }

            int projRow = (int) Math.round((p.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
            if (plantRow != projRow) continue;

            double deltaX = p.getX() - plant.getX();

            if (deltaX >= 0 && deltaX <= (Constants.Game.TILE_WIDTH * 1.5)) {

                p.setIgnited(true);

                if (source == PlantType.SNOW_PEA) {
                    p.setType(DamageType.NORMAL);
                } else {
                    p.setType(DamageType.FIRE);

                    if (isBlueTorchwood) {
                        BLUE_PROCESSED.add(p);

                        // Fire Pea (40) -> 120! Normal Pea (20) -> 60!
                        p.setDamage(p.getDamage() * 3);
                    } else {
                        // Normal Pea (20) -> 40!
                        p.setDamage(p.getDamage() * 2);
                    }
                }
            }
        }
    }
}
