package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.HomingProjectile;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InstantKillEffect implements PlantFoodEffectStrategy {
    private final int targetCount;

    public InstantKillEffect(int targetCount) {
        this.targetCount = targetCount;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {

        if (plant.getName().equals("Electric Blueberry")) {
            // --- ELECTRIC BLUEBERRY: Map-Wide Random Thunder Clouds ---
            List<Zombie> livingZombies = board.getAllZombies().stream()
                .filter(z -> !z.isDead())
                .collect(Collectors.toList());

            Collections.shuffle(livingZombies);

            List<Zombie> selectedZombies = livingZombies.stream()
                .limit(targetCount)
                .collect(Collectors.toList());

            for (Zombie zombie : selectedZombies) {
                try {
                    Projectile proj = HomingProjectile.class.getDeclaredConstructor(
                            double.class, double.class, double.class, int.class, Zombie.class)
                        .newInstance(plant.getX(), plant.getY(), 8.0, 0, zombie);

                    proj.setSourcePlantType(PlantType.ELECTRIC_BLUEBERRY);
                    board.getActiveProjectiles().add(proj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (plant.getName().equals("Chomper")) {
            // --- CHOMPER: Lane Vacuum & Board-Wide Repel ---
            int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
            double maxX = Constants.Game.PADDING_X + (board.totalCols * Constants.Game.TILE_WIDTH) + (Constants.Game.TILE_WIDTH * 2);

            // 1. Find all zombies in Chomper's row, directly in front of it, closest first!
            List<Zombie> laneZombies = board.getAllZombies().stream()
                .filter(z -> !z.isDead() && z.getCurrentRow() == plantRow && z.getX() >= plant.getX())
                .sorted(Comparator.comparingDouble(Zombie::getX))
                .collect(Collectors.toList());

            // 2. Devour up to the target limit (3 zombies)
            int eaten = 0;
            for (Zombie z : laneZombies) {
                if (eaten < targetCount) {
                    z.takeDamage(99999, DamageType.NORMAL, PlantType.CHOMPER);
                    eaten++;
                }
            }

            // 3. The Burp Pushback! Repel ALL surviving zombies on the board by 2 tiles
            for (Zombie z : board.getAllZombies()) {
                if (!z.isDead() && z.getHealth() > 0) { // Make sure we don't push zombies we just ate!
                    double pushStep = 2.0 * Constants.Game.TILE_WIDTH;
                    z.setX(Math.min(z.getX() + pushStep, maxX));
                }
            }

        } else {
            // --- FALLBACK: Generic instant kill for future plants ---
            List<Zombie> livingZombies = board.getAllZombies().stream()
                .filter(z -> !z.isDead())
                .collect(Collectors.toList());

            Collections.shuffle(livingZombies);

            livingZombies.stream()
                .limit(targetCount)
                .forEach(z -> z.takeDamage(99999, DamageType.NORMAL, PlantType.getByName(plant.getName())));
        }

        plant.resetFeed();
    }
}
