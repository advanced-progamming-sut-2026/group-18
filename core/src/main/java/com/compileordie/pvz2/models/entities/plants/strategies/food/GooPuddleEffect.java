package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.GooBoulderProjectile;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class GooPuddleEffect implements PlantFoodEffectStrategy {

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        try {
            // Spawn the giant boulder (600 Impact Damage)
            // It dynamically grabs the Poison DoT upgrades from the plant to pass to the puddle!
            int tickDamage = 6 + plant.getPoisonDmgTickBonus();
            Projectile boulder = new GooBoulderProjectile(plant.getX(), plant.getY(), 6.0, 600, tickDamage);

            boulder.setSourcePlantType(PlantType.GOO_PEASHOOTER);
            board.getActiveProjectiles().add(boulder);

        } catch (Exception e) {
            e.printStackTrace();
        }

        plant.resetFeed();
    }
}
