package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

import java.util.List;

public class ChargeShootStrategy implements AttackStrategy {

    private final Class<? extends Projectile> projectileType;
    private final double requiredChargeTicks;
    private double currentCharge = 0;
    private boolean isCharged = false;

    public ChargeShootStrategy(Class<? extends Projectile> projectileType, double requiredChargeTicks) {
        this.projectileType = projectileType;
        this.requiredChargeTicks = requiredChargeTicks;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        // Step 1: Handle Charging
        if (!isCharged) {
            currentCharge += tickDelta;
            if (currentCharge >= requiredChargeTicks) {
                isCharged = true;
            }
            return; // Cannot shoot while charging
        }

        // Step 2: Ensure there is a target before wasting the charge
        List<Zombie> zombies = board.getAllZombies();
        boolean targetExists = zombies.stream()
            .anyMatch(z -> !z.isDead() && Math.abs(z.getY() - plant.getY()) < 0.5 && z.getX() > plant.getX());

        if (targetExists) {
            try {
                Projectile proj = projectileType
                    .getDeclaredConstructor(double.class, double.class, double.class, int.class)
                    .newInstance(plant.getX(), plant.getY(), 5.0, plant.getBaseDamage());

                board.getActiveProjectiles().add(proj);

                // Reset charge after firing
                isCharged = false;
                currentCharge = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
