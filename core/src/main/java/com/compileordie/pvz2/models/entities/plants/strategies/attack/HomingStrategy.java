package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class HomingStrategy implements AttackStrategy {

    private final Class<? extends Projectile> projectileType;
    private final TargetingMode baseTargetingMode;
    private transient final Random random = new Random();

    public HomingStrategy(Class<? extends Projectile> projectileType, TargetingMode baseTargetingMode) {
        this.projectileType = projectileType;
        this.baseTargetingMode = baseTargetingMode;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        // FIX: Explicitly filter out dead AND hypnotized zombies so they are never targeted!
        List<Zombie> activeZombies = board.getAllZombies().stream()
            .filter(z -> !z.isDead() && !z.isHypnotized())
            .collect(Collectors.toList());

        if (activeZombies.isEmpty()) return;

        TargetingMode currentMode = plant.targetsHighestHp() ? TargetingMode.HIGHEST_HP : baseTargetingMode;
        Zombie target = null;

        // --- FIX: Removed Double-Padding! Use plant's native world coordinates. ---
        double pWorldX = plant.getX();
        double pWorldY = plant.getY() + 0.2;

        switch (currentMode) {
            case CLOSEST:
                target = activeZombies.stream()
                    .min(Comparator.comparingDouble(z ->
                        Math.hypot(z.getX() - pWorldX, z.getY() - pWorldY)))
                    .orElse(null);
                break;
            case RANDOM:
                target = activeZombies.get(random.nextInt(activeZombies.size()));
                break;
            case HIGHEST_HP:
                target = activeZombies.stream()
                    .max(Comparator.comparingDouble(Zombie::getHealth))
                    .orElse(null);
                break;
        }

        if (target != null) {
            try {
                // We still pass plant.getX() because Projectiles operate in logical space!
                Projectile proj = projectileType
                    .getDeclaredConstructor(double.class, double.class, double.class, int.class, Zombie.class)
                    .newInstance(plant.getX(), plant.getY(), 4.0, plant.getBaseDamage(), target);

                proj.setSourcePlantType(PlantType.getByName(plant.getName()));

                board.getActiveProjectiles().add(proj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public enum TargetingMode {CLOSEST, RANDOM, HIGHEST_HP}
}
