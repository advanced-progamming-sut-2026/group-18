package com.compileordie.pvz2.models.entities.plants.strategies.food;
import com.badlogic.gdx.utils.Timer;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.PiercingProjectile;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;
import java.util.Random;
public class LaneClearEffect implements PlantFoodEffectStrategy {
    private final Random random = new Random();
    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        String name = plant.getName();
        int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
        double maxX = Constants.Game.PADDING_X + (board.totalCols * Constants.Game.TILE_WIDTH) + (Constants.Game.TILE_WIDTH * 2);
        if (name.equals("Citron")) {
            try {
                Projectile proj = PiercingProjectile.class.getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                    .newInstance(plant.getX(), plant.getY(), 6.0, 4500, 100);
                proj.setSourcePlantType(PlantType.CITRON);
                proj.setXSpeed(6.0);
                board.getActiveProjectiles().add(proj);
            } catch (Exception e) {}
            plant.resetFeed();
        }
        else if (name.equals("Fume-shroom")) {
            Timer.schedule(new Timer.Task() {
                int ticks = 0;
                @Override
                public void run() {
                    if (plant.isDead()) { this.cancel(); return; }

                    // 1. Push and damage zombies over 5.5 seconds
                    for (Zombie z : board.getAllZombies()) {
                        if (!z.isDead() && z.getCurrentRow() == plantRow && z.getX() >= plant.getX()) {
                            z.takeDamage(1500 / 55, DamageType.NORMAL, PlantType.FUME_SHROOM);
                            if (!z.isDead()) {
                                double pushStep = (3 * Constants.Game.TILE_WIDTH) / 55.0;
                                z.setX(Math.min(z.getX() + pushStep, maxX));
                            }
                        }
                    }

                    // --- FIX: CONTINUOUS HIGH DENSITY WITH A CLEAN CUTOFF ---
                    // Spawn a thick, chaotic stream of bubbles, but shut off the "hose" early (tick 35 = 3.5s).
                    // The tail end of the stream will clear the screen perfectly as the plant finishes blowing at 5.5s!
                    if (ticks < 35) {
                        for (int i = 0; i < 3; i++) {
                            try {
                                // Add random X and Y offsets to make the cloud look thick and spread out!
                                double offsetX = (Math.random() * 0.4) * Constants.Game.TILE_WIDTH;
                                double offsetY = (Math.random() * 0.4 - 0.2) * Constants.Game.TILE_HEIGHT;

                                Projectile proj = PiercingProjectile.class.getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                                    .newInstance(plant.getX() + offsetX, plant.getY() + offsetY, 6.0, 0, 100);
                                proj.setSourcePlantType(PlantType.FUME_SHROOM);
                                proj.setXSpeed(6.0); // Fast enough to clear out smoothly
                                board.getActiveProjectiles().add(proj);
                            } catch (Exception e) {}
                        }
                    }

                    ticks++;
                    if (ticks >= 55) {
                        plant.resetFeed();
                        this.cancel();
                    }
                }
            }, 0f, 0.1f, 54);
        }
        else if (name.equals("Garlic")) {
            for (Zombie zombie : board.getAllZombies()) {
                if (!zombie.isDead() && zombie.getCurrentRow() == plantRow) {
                    int targetRow = plantRow;
                    if (plantRow == 0) targetRow = 1;
                    else if (plantRow == board.totalRows - 1) targetRow = plantRow - 1;
                    else targetRow = random.nextBoolean() ? plantRow - 1 : plantRow + 1;
                    double newY = Constants.Game.PADDING_Y + (targetRow * Constants.Game.TILE_HEIGHT) + (Constants.Game.TILE_HEIGHT / 2.0);
                    zombie.setY(newY);
                }
            }
        }

        plant.resetFeed();
    }
}
