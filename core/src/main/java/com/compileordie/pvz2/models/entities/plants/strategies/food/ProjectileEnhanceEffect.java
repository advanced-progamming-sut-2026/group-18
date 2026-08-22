package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.PiercingProjectile;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class ProjectileEnhanceEffect implements PlantFoodEffectStrategy {
    private final int damageMultiplier;

    public ProjectileEnhanceEffect(int damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {

        if (plant.getName().equals("Cactus")) {
            // ... (keep your existing Cactus logic exactly as is) ...
            for (int i = 0; i < 3; i++) {
                try {
                    Projectile thorn = new PiercingProjectile(
                            plant.getX() + (i * 0.5 * Constants.Game.TILE_WIDTH),
                        plant.getY(),
                        6.0,
                        200,
                        9999 // Unlimited penetration!
                    );

                    thorn.setSourcePlantType(PlantType.CACTUS);
                    board.getActiveProjectiles().add(thorn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // --- NEW: TORCHWOOD LOGIC ---
        else if (plant.getName().equals("Torchwood")) {
            plant.setBlueFlame(true);
            plant.setCurrentHp(plant.getBaseHp()); // Heal back to full!
        }
        // --- FALLBACK ---
        else {
            // Failsafe for other plants using this effect
            plant.setBaseDamage(plant.getBaseDamage() * damageMultiplier);
            plant.setCurrentHp(plant.getBaseHp() * 2);
        }

        plant.resetFeed();
    }
}
