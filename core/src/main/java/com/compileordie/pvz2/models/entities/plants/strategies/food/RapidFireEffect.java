package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.badlogic.gdx.utils.Timer;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.IceProjectile;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

import java.util.List;
import java.util.Random;

public class RapidFireEffect implements PlantFoodEffectStrategy {

    private final Class<? extends Projectile> projectileType;

    public RapidFireEffect(Class<? extends Projectile> projectileType) {
        this.projectileType = projectileType;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        String name = plant.getName();
        double x = plant.getX();
        double y = plant.getY();
        int baseDamage = plant.getBaseDamage();

        try {
            if (name.equals("Split Pea")) {
                spawnStream(board, x, y, 1.0, 0.0, 30, baseDamage, plant);
                spawnStream(board, x, y, -1.0, 0.0, 30, baseDamage, plant);
            } else if (name.equals("Repeater")) {
                spawnStream(board, x, y, 1.0, 0.0, 60, baseDamage, plant);
                // Giant Pea fires at the very end of the stream (3 seconds later)
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        if (!plant.isDead()) {
                            try {
                                spawnGiantPea(board, x, y, 1.0, 0.0, baseDamage * 20, plant);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }, 3.0f);
            } else if (name.equals("Threepeater")) {
                spawnStream(board, x, y, 1.0, 0.0, 30, baseDamage, plant);
                spawnStream(board, x, y, 1.0, 0.5, 30, baseDamage, plant);
                spawnStream(board, x, y, 1.0, -0.5, 30, baseDamage, plant);
                spawnStream(board, x, y, 1.0, 1.0, 30, baseDamage, plant);
                spawnStream(board, x, y, 1.0, -1.0, 30, baseDamage, plant);
            } else if (name.equals("Pea Pod")) {
                int stacks = plant.getStackCount();
                Timer.schedule(new Timer.Task() {
                    int fired = 0;
                    @Override
                    public void run() {
                        if (plant.isDead()) {
                            this.cancel();
                            return;
                        }
                        try {
                            spawnGiantPea(board, x, y, 1.0, 0.0, baseDamage * 20, plant);
                        } catch (Exception ignored) {
                        }
                        fired++;
                        if (fired >= stacks) {
                            this.cancel();
                        }
                    }
                }, 0f, 0.8f, stacks - 1);
            } else if (name.equals("Rotobaga")) {
                spawnStream(board, x, y, 1.0, 1.0, 30, baseDamage, plant);
                spawnStream(board, x, y, 1.0, -1.0, 30, baseDamage, plant);
                spawnStream(board, x, y, -1.0, 1.0, 30, baseDamage, plant);
                spawnStream(board, x, y, -1.0, -1.0, 30, baseDamage, plant);
            } else if (name.equals("Snow Pea")) {
                int plantRow = (int) Math.floor((y - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
                for (Zombie z : board.getAllZombies()) {
                    if (!z.isDead() && z.occupiesRow(plantRow)) {
                        z.addEffect(new StatusEffect(EffectType.FROZEN, 100)); // Frozen for 5 seconds
                    }
                }
                spawnStream(board, x, y, 1.0, 0.0, 60, baseDamage, plant);
            } else if (name.equals("Mega Gatling Pea")) {
                spawnStream(board, x, y, 1.0, 0.0, 90, baseDamage, plant); // 4.5 seconds of firing!
                Timer.schedule(new Timer.Task() { // Giant peas follow up after the massive stream
                    @Override
                    public void run() {
                        if (plant.isDead()) return;
                        try {
                            spawnGiantPea(board, x, y, 1.0, 0.0, baseDamage * 20, plant);
                            spawnGiantPea(board, x + 0.5, y, 1.0, 0.0, baseDamage * 20, plant);
                            spawnGiantPea(board, x + 1.0, y, 1.0, 0.0, baseDamage * 20, plant);
                            spawnGiantPea(board, x + 1.5, y, 1.0, 0.0, baseDamage * 20, plant);
                        } catch (Exception ignored) {
                        }
                    }
                }, 4.5f);
            } else if (name.equals("Puff-shroom") || name.equals("Sea-shroom")) {
                for (Plant p : board.getAllPlants()) {
                    if (p.getName().equals(name)) p.resetLifespan();
                }
                spawnStream(board, x, y, 1.0, 0.0, 40, baseDamage, plant);
            } else if (name.equals("Starfruit")) {
                spawnStream(board, x, y, -1.0, 0.0, 30, baseDamage, plant);
                spawnStream(board, x, y, 0.0, -1.0, 30, baseDamage, plant);
                spawnStream(board, x, y, 0.0, 1.0, 30, baseDamage, plant);
                spawnStream(board, x, y, 1.0, -0.5, 30, baseDamage, plant);
                spawnStream(board, x, y, 1.0, 0.5, 30, baseDamage, plant);
            } else if (name.equals("Cat-tail")) {
                spawnHomingStream(board, x, y, 30, baseDamage, plant);
            } else if (name.equals("Fire Peashooter") || name.equals("Peashooter")) {
                spawnStream(board, x, y, 1.0, 0.0, 60, baseDamage, plant);
            } else {
                spawnStream(board, x, y, 1.0, 0.0, 30, baseDamage, plant);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- THE NEW BARRAGE ENGINE ---
    private void spawnStream(GameBoard board, double startX, double startY, double xDir, double yDir,
                             int count, int damage, Plant plant) {
        // Fires 1 pea every 0.05 seconds until 'count' is reached!
        Timer.schedule(new Timer.Task() {
            int spawned = 0;

            @Override
            public void run() {
                if (plant.isDead()) {
                    this.cancel();
                    return;
                }

                try {
                    Projectile proj;
                    if (projectileType == IceProjectile.class) {
                        double totalChillTime = 100.0 + plant.getChillTimeBonusTicks();
                        proj = projectileType.getDeclaredConstructor(
                                double.class, double.class, double.class, int.class, double.class)
                            .newInstance(startX, startY, 6.0, damage, totalChillTime);
                    } else {
                        proj = projectileType.getDeclaredConstructor(
                                double.class, double.class, double.class, int.class)
                            .newInstance(startX, startY, 6.0, damage);
                    }
                    proj.setSourcePlantType(PlantType.getByName(plant.getName()));
                    proj.setXSpeed(6.0 * xDir);
                    proj.setYSpeed(6.0 * yDir);
                    board.getActiveProjectiles().add(proj);
                } catch (Exception ignored) {
                }

                spawned++;
                if (spawned >= count) {
                    this.cancel();
                    // Optional: Reset plant feed state here if we want to tie it exactly to the end of the barrage
                }
            }
        }, 0f, 0.05f, count - 1);
    }

    private void spawnGiantPea(GameBoard board, double x, double y, double xDir, double yDir,
                               int giantDamage, Plant plant) throws Exception {
        Projectile giantProj;
        if (projectileType == IceProjectile.class) {
            double totalChillTime = 100.0 + plant.getChillTimeBonusTicks();
            giantProj = projectileType.getDeclaredConstructor(
                    double.class, double.class, double.class, int.class, double.class)
                .newInstance(x, y, 6.0, giantDamage, totalChillTime);
        } else {
            giantProj = projectileType.getDeclaredConstructor(
                    double.class, double.class, double.class, int.class)
                .newInstance(x, y, 6.0, giantDamage);
        }

        giantProj.setSourcePlantType(PlantType.getByName(plant.getName()));
        giantProj.setXSpeed(6.0 * xDir);
        giantProj.setYSpeed(6.0 * yDir);
        board.getActiveProjectiles().add(giantProj);
    }

    private void spawnHomingStream(GameBoard board, double startX, double startY,
                                   int count, int damage, Plant plant) {
        Timer.schedule(new Timer.Task() {
            int spawned = 0;
            transient final Random rand = new Random();

            @Override
            public void run() {
                if (plant.isDead()) {
                    this.cancel();
                    return;
                }

                try {
                    List<Zombie> activeZombies = board.getAllZombies().stream().filter(z -> !z.isDead()).toList();
                    double spawnX = startX + (rand.nextDouble() * 0.5 - 0.25);
                    double spawnY = startY + (rand.nextDouble() * 0.5 - 0.25);
                    Zombie target = activeZombies.isEmpty()
                        ? null
                        : activeZombies.get(rand.nextInt(activeZombies.size()));

                    Projectile proj = projectileType.getDeclaredConstructor(
                            double.class, double.class, double.class, int.class, Zombie.class)
                        .newInstance(spawnX, spawnY, 6.0, damage, target);
                    proj.setSourcePlantType(PlantType.getByName(plant.getName()));
                    board.getActiveProjectiles().add(proj);
                } catch (Exception ignored) {
                }

                spawned++;
                if (spawned >= count) {
                    this.cancel();
                }
            }
        }, 0f, 0.05f, count - 1);
    }
}
