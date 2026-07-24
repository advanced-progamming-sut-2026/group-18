package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class HomingStrategy implements AttackStrategy {

    public enum TargetingMode { CLOSEST, RANDOM, HIGHEST_HP }

    private final Class<? extends Projectile> projectileType;
    private final TargetingMode baseTargetingMode;
    private final Random random = new Random();

    public HomingStrategy(Class<? extends Projectile> projectileType, TargetingMode baseTargetingMode) {
        this.projectileType = projectileType;
        this.baseTargetingMode = baseTargetingMode;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        List<Zombie> activeZombies = board.getAllZombies().stream()
            .filter(z -> !z.isDead())
            .collect(Collectors.toList());

        if (activeZombies.isEmpty()) return;

        // Check if the plant has a level upgrade that forces it to target high HP
        TargetingMode currentMode = plant.targetsHighestHp() ? TargetingMode.HIGHEST_HP : baseTargetingMode;
        Zombie target = null;

        switch (currentMode) {
            case CLOSEST:
                target = activeZombies.stream()
                    .min(Comparator.comparingDouble(z ->
                        Math.hypot(z.getX() - plant.getX(), z.getY() - plant.getY())))
                    .orElse(null);
                break;
            case RANDOM:
                target = activeZombies.get(random.nextInt(activeZombies.size()));
                break;
            case HIGHEST_HP:
                target = activeZombies.stream()
                    .max(Comparator.comparingInt(Zombie::getHp)) // Assumes getHp() exists
                    .orElse(null);
                break;
        }

        if (target != null) {
            try {
                // Spawns HomingProjectile, HypnoProjectile, or LightningProjectile dynamically
                Projectile proj = projectileType
                    .getDeclaredConstructor(double.class, double.class, double.class, int.class, Zombie.class)
                    .newInstance(plant.getX(), plant.getY(), 4.0, plant.getBaseDamage(), target);
                board.getActiveProjectiles().add(proj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
