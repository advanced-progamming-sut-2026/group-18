package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class BowlingStrategy implements AttackStrategy {

    private final Class<? extends Projectile> projectileType;
    private final int maxBounces;

    public BowlingStrategy(Class<? extends Projectile> projectileType, int maxBounces) {
        this.projectileType = projectileType;
        this.maxBounces = maxBounces;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        // Step 1: Wait for a target! (Smart Targeting)
        int plantRow = (int) (plant.getY() / com.compileordie.pvz2.config.Constants.Game.TILE_SIZE);
        boolean targetExists = board.getAllZombies().stream()
            .anyMatch(z -> !z.isDead() && z.getCurrentRow() == plantRow && z.getX() > plant.getX());

        // Hold fire if the lane is empty or if we are out of bulbs!
        if (!targetExists || (plant.getName().equals("Bowling Bulb") && plant.getBulbCount() == 0)) {
            plant.holdAction = true;
            return;
        }

// Step 2: Determine Damage based on Bulb size
        int currentDamage = plant.getBaseDamage(); // Defaults to 40 (Cyan)

        if (plant.getName().equals("Bowling Bulb")) {
            // If count is 3, it skips these if-statements and safely shoots Cyan (40)

            if (plant.getBulbCount() == 1) {
                // Only 1 bulb left? That's the big Orange one!
                currentDamage = (int)(currentDamage * 4.5); // 40 * 4.5 = 180
            } else if (plant.getBulbCount() == 2) {
                // 2 bulbs left? That's the medium Blue one!
                currentDamage = currentDamage * 3;          // 40 * 3 = 120
            }

            // Consume the bulb AFTER determining the damage
            plant.consumeBulb();
        }

        // Step 3: Fire the Bouncing Bulb
        try {
            Projectile proj = projectileType
                .getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                .newInstance(plant.getX(), plant.getY(), 4.0, currentDamage, maxBounces);

            // Clean Enum injection
            proj.setSourcePlantType(PlantType.getByName(plant.getName()));

            board.getActiveProjectiles().add(proj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
