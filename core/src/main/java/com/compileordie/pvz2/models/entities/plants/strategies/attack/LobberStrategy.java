package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.ButterProjectile;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class LobberStrategy implements AttackStrategy {

    private final Class<? extends Projectile> projectileType;
    private final double splashRadiusTiles;

    public LobberStrategy(Class<? extends Projectile> projectileType, double splashRadiusTiles) {
        this.projectileType = projectileType;
        this.splashRadiusTiles = splashRadiusTiles;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        int plantRow = (int) (plant.getY() / Constants.Game.TILE_SIZE);

        // --- 1. Lock onto the first available zombie in the lane ---
        java.util.Optional<Zombie> targetZombie = board.getAllZombies().stream()
            .filter(z -> !z.isDead() && z.getCurrentRow() == plantRow && z.getX() >= plant.getX())
            .min((z1, z2) -> Double.compare(z1.getX(), z2.getX())); // Find the closest one

        if (targetZombie.isEmpty()) {
            plant.holdAction = true;
            return;
        }

        // --- 2. Pass the Target's X coordinate to the Projectile! ---
        double targetX = targetZombie.get().getX();

// Inside LobberStrategy.java, update the execution block:

        try {
            Class<? extends Projectile> currentProjClass = projectileType;
            int finalDamage = plant.getBaseDamage(); // Base 20

            // --- NEW: Kernel-pult Butter RNG! ---
            if (plant.getName().equals("Kernel-pult")) {
                if (Math.random() * 100 < plant.getButterChance()) {
                    currentProjClass = ButterProjectile.class;
                    finalDamage *= 2; // Kernel is 20, Butter is 40!
                }
            }

            Projectile proj = currentProjClass
                .getDeclaredConstructor(double.class, double.class, double.class, double.class, int.class, int.class, double.class)
                .newInstance(plant.getX(), plant.getY(), targetX, 3.5, finalDamage, plant.getAoeDamage(), splashRadiusTiles);

            proj.setSourcePlantType(PlantType.getByName(plant.getName()));
            board.getActiveProjectiles().add(proj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
