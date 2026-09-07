package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.badlogic.gdx.utils.Timer;
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
            plant.setBlueFlame(true);
            Timer.schedule(new Timer.Task() {
                int fired = 0;
                @Override
                public void run() {
                    if (plant.isDead()) { this.cancel(); return; }
                    try {
                        Projectile thorn = PiercingProjectile.class.getDeclaredConstructor(double.class,
                                double.class,
                                double.class,
                                int.class,
                                int.class)
                            .newInstance(plant.getX(), plant.getY(), 6.0, 200, 9999);
                        thorn.setSourcePlantType(PlantType.CACTUS);
                        board.getActiveProjectiles().add(thorn);
                    } catch (Exception e) {}
                    fired++;
                    if (fired >= 3) this.cancel();
                }
            }, 0f, 0.2f, 2);
        }
        else if (plant.getName().equals("Torchwood")) {
            plant.setBlueFlame(true);
            plant.setCurrentHp(plant.getBaseHp());
            return;
        }
        else {
            plant.setBaseDamage(plant.getBaseDamage() * damageMultiplier);
            plant.setCurrentHp(plant.getBaseHp() * 2);
        }
        plant.resetFeed();
    }
}
