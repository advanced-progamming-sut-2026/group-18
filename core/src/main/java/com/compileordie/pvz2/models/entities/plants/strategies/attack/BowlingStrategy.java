package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.game.board.GameBoard;

import static com.compileordie.pvz2.config.Constants.Game.PADDING_Y;
import static com.compileordie.pvz2.config.Constants.Game.TILE_HEIGHT;

public class BowlingStrategy implements AttackStrategy {

    private final Class<? extends Projectile> projectileType;
    private final int maxBounces;

    public BowlingStrategy(Class<? extends Projectile> projectileType, int maxBounces) {
        this.projectileType = projectileType;
        this.maxBounces = maxBounces;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        // --- FIX: The Logical Row Fix ---
        int plantRow = (int) Math.floor((plant.getY() - PADDING_Y) / TILE_HEIGHT);

        // STEP 1: If we aren't winding up yet, scan for targets!
        if (!plant.isWindingUp) {
            boolean targetExists = board.getAllZombies().stream()
                .anyMatch(z -> !z.isDead() && !z.isHypnotized() && z.getCurrentRow() == plantRow && z.getX() > plant.getX());

            if (!targetExists || plant.bulbs.isEmpty()) {
                plant.holdAction = true;
                return;
            }

            // START WINDUP! Lock in the bulb and freeze the plant's action timer!
            plant.isWindingUp = true;
            plant.windupTimer = 0;
            plant.holdAction = true;
            plant.currentlyFiringBulb = plant.bulbs.remove(0); // Pop the front bulb (Cyan -> Blue -> Orange!)
            return;
        }

        // STEP 2: WE ARE WINDING UP! Wait for the animation!
        plant.windupTimer += tickDelta;
        plant.holdAction = true; // Keep holding the engine hostage

        // Wait exactly 0.4 seconds (24 ticks) for the 'special' animation to physically throw the bulb!
        if (plant.windupTimer >= 8) {
            plant.isWindingUp = false;
            plant.holdAction = false; // RELEASE! This tells Plant.java to reset the timer to 0!

            int damage = (plant.currentlyFiringBulb == 3) ? 180 : (plant.currentlyFiringBulb == 2 ? 120 : 40);

            try {
                // Spawn EXACTLY on the plant since the animation timing is perfectly synced now!
                Projectile proj = projectileType
                    .getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                    .newInstance(plant.getX(), plant.getY(), 4.0, damage, maxBounces);

                proj.setSourcePlantType(PlantType.getByName(plant.getName()));
                board.getActiveProjectiles().add(proj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
