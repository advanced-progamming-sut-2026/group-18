package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.user.Player;

import java.util.List;

public class AreaDamageEffect implements PlantFoodEffectStrategy {
    private final int damageAmount;

    public AreaDamageEffect(int damageAmount) {
        this.damageAmount = damageAmount;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {

        if (plant.getName().equals("Bowling Bulb")) {
            plant.reloadAllBulbs();
            try {
                for (int i = 0; i < 3; i++) {
                    Projectile plasma = plant.template.getProjectileType()
                        .getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                            .newInstance(plant.getX() + (i * 0.5 * Constants.Game.TILE_WIDTH), plant.getY(), 6.0, 600, 5);

                    plasma.setSourcePlantType(PlantType.BOWLING_BULB);
                    board.getActiveProjectiles().add(plasma);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // --- Cabbage-pult Random Artillery Strike ---
        else if (plant.getName().equals("Cabbage-pult")) {
            int pfDamage = plant.getBaseDamage() * 4;

            // 1. Get all alive zombies
            List<Zombie> aliveZombies = board.getAllZombies().stream()
                .filter(z -> !z.isDead())
                .collect(java.util.stream.Collectors.toList());

            // 2. Shuffle them to make it completely random!
            java.util.Collections.shuffle(aliveZombies);

            // 3. Pick up to 10 random zombies (or less, if there aren't 10 on screen)
            int targetCount = Math.min(7, aliveZombies.size());
            for (int i = 0; i < targetCount; i++) {
                aliveZombies.get(i).takeDamage(pfDamage, DamageType.LOBBER, PlantType.CABBAGE_PULT);
            }
        }

        else if (plant.getName().equals("Kernel-pult")) {
            // Butter Plant Food is 40 damage and 8 seconds of stun!
            int pfDamage = plant.getBaseDamage() * 2;

            for (Zombie zombie : board.getAllZombies()) {
                if (!zombie.isDead()) {
                    zombie.takeDamage(pfDamage, DamageType.LOBBER, PlantType.KERNEL_PULT);
                    zombie.addEffect(new StatusEffect(EffectType.STUNNED, 80));
                }
            }
        }
        // --- Heavy Lobber Artillery Strikes ---
        else if (plant.getName().equals("Melon-pult") || plant.getName().equals("Winter Melon") || plant.getName().equals("Pepper-pult")) {

            // Standard Plant Food damage formula for heavy lobbers (usually around 200 damage)
            int pfDamage = plant.getBaseDamage() * 2;

            // 1. Get all alive zombies
            java.util.List<Zombie> aliveZombies = board.getAllZombies().stream()
                .filter(z -> !z.isDead())
                .collect(java.util.stream.Collectors.toList());

            java.util.Collections.shuffle(aliveZombies);

            // Pepper-pult hits exactly 3 random zombies, Melons hit everyone!
            int targetCount = plant.getName().equals("Pepper-pult") ? Math.min(3, aliveZombies.size()) : aliveZombies.size();

            for (int i = 0; i < targetCount; i++) {
                Zombie target = aliveZombies.get(i);

                // Deal the raw damage
                target.takeDamage(pfDamage, DamageType.LOBBER, PlantType.getByName(plant.getName()));

                // Apply the elemental payload!
                if (!target.isDead()) {
                    if (plant.getName().equals("Winter Melon")) {
                        target.addEffect(new StatusEffect(EffectType.CHILLED, 50));
                    } else if (plant.getName().equals("Pepper-pult")) {
                        target.removeStatusEffect(EffectType.CHILLED);
                        target.removeStatusEffect(EffectType.FROZEN);
                    }
                }
            }
        }
        // --- TANGLE KELP (Abyssal Pull) ---
        else if (plant.getName().equals("Tangle Kelp")) {

            // 1. Gather all zombies that are currently in the water!
            java.util.List<Zombie> waterZombies = new java.util.ArrayList<>();

            for (Zombie z : board.getAllZombies()) {
                if (!z.isDead()) {
                    Tile zTile = board.getTile((float) z.getX(), (float) z.getY());

                    // Relies on the perfect isUnderWater() method you wrote earlier!
                    if (zTile != null && zTile.isUnderWater()) {
                        waterZombies.add(z);
                    }
                }
            }

            // 2. Shuffle to randomize targets
            java.util.Collections.shuffle(waterZombies);

            // 3. Pull down up to 4 zombies
            int targetsToPull = Math.min(4, waterZombies.size());

            for (int i = 0; i < targetsToPull; i++) {
                waterZombies.get(i).takeDamage(99999, DamageType.NORMAL, PlantType.getByName(plant.getName()));
            }
        }
        // --- BONK CHOY (Rapid 3x3 Punches) ---
        else if (plant.getName().equals("Bonk Choy") || plant.getName().equals("Phat Beet")
            || plant.getName().equals("Wasabi Whip") || plant.getName().equals("Kiwibeast")) {

            // Kiwibeast instantly jumps to max size!
            if (plant.getName().equals("Kiwibeast")) plant.forceMaxGrowth();

            double radiusPixels = 1.5 * Constants.Game.TILE_HEIGHT;

            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;

                double dist = Math.hypot(z.getX() - plant.getX(), z.getY() - plant.getY());
                if (dist <= radiusPixels) {
                    // Deal massive flurry damage! (e.g., 60 rapid punches * 15 damage = 900 damage)
                    z.takeDamage(900, DamageType.NORMAL, PlantType.getByName(plant.getName()));
                }
            }
        }
        // --- Standard Static Explosion (Cherry Bomb) ---
        else {
            double radius = 3.0 * Constants.Game.TILE_HEIGHT;
            for (Zombie zombie : board.getAllZombies()) {
                if (zombie.isDead()) continue;

                double distance = Math.hypot(zombie.getX() - plant.getX(), zombie.getY() - plant.getY());
                if (distance <= radius) {
                    zombie.takeDamage(damageAmount, DamageType.EXPLOSIVE, PlantType.getByName(plant.getName()));
                }
            }
        }

        plant.resetFeed();
    }
}
