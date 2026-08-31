package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.List;

import static com.compileordie.pvz2.config.Constants.Game.PADDING_Y;
import static com.compileordie.pvz2.config.Constants.Game.TILE_HEIGHT;

public class ChargeShootStrategy implements AttackStrategy {

    private final Class<? extends Projectile> projectileType;

    public ChargeShootStrategy(Class<? extends Projectile> projectileType) {
        this.projectileType = projectileType;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        // --- FIX 1: The Logical Row Fix ---
        int plantRow = (int) Math.floor((plant.getY() - PADDING_Y) / TILE_HEIGHT);
        List<Zombie> zombies = board.getAllZombies();

        // --- FIX 2: Removed Double-Padding! ---
        double plantWorldX = plant.getX();

        boolean targetExists = zombies.stream()
            .anyMatch(z -> !z.isDead() && !z.isHypnotized() && z.occupiesRow(plantRow) && z.getX() > plantWorldX);

        if (!targetExists) {
            plant.holdAction = true;
            return;
        }

        try {
            Projectile proj = projectileType
                .getDeclaredConstructor(double.class, double.class, double.class, int.class)
                .newInstance(plant.getX(), plant.getY(), 5.0, plant.getBaseDamage());

            proj.setSourcePlantType(PlantType.getByName(plant.getName()));
            board.getActiveProjectiles().add(proj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
