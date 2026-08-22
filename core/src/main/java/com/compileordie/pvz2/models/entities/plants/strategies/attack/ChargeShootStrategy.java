package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.List;

public class ChargeShootStrategy implements AttackStrategy {

    private final Class<? extends Projectile> projectileType;


    public ChargeShootStrategy(Class<? extends Projectile> projectileType) {
        this.projectileType = projectileType;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {

        // Step 1: Ensure there is a target in this row before wasting the heavy charge
        int plantRow = (int) (plant.getY() / Constants.Game.TILE_HEIGHT);
        List<Zombie> zombies = board.getAllZombies();

        boolean targetExists = zombies.stream()
            .anyMatch(z -> !z.isDead() && z.getCurrentRow() == plantRow && z.getX() > plant.getX());

        // Step 2: If no target, tell Plant.java to HOLD the timer so it can shoot instantly when one appears!
        if (!targetExists) {
            plant.holdAction = true;
            return;
        }

        // Step 3: Fire the plasma ball!
        try {
            Projectile proj = projectileType
                .getDeclaredConstructor(double.class, double.class, double.class, int.class)
                .newInstance(plant.getX(), plant.getY(), 5.0, plant.getBaseDamage());

            // NEW: Passing the Enum for the quest tracker!
            proj.setSourcePlantType(PlantType.getByName(plant.getName()));

            board.getActiveProjectiles().add(proj);

            // Plant.java will natively reset the timer to 0 since we didn't flag holdAction!
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
