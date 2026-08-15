package com.compileordie.pvz2.models.entities.plants.strategies.food;

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
                spawnStream(board, x, y, 1.0, 0.0, 15, baseDamage, plant);
                spawnStream(board, x, y, -1.0, 0.0, 15, baseDamage, plant);
            }
            else if (name.equals("Repeater")) {
                spawnStream(board, x, y, 1.0, 0.0, 15, baseDamage, plant);
                spawnGiantPea(board, x, y, 1.0, 0.0, baseDamage * 20, plant);
            }
            else if (name.equals("Threepeater")) {
                spawnStream(board, x, y, 1.0, 0.0, 15, baseDamage, plant);
                spawnStream(board, x, y, 1.0, 0.5, 15, baseDamage, plant);
                spawnStream(board, x, y, 1.0, -0.5, 15, baseDamage, plant);
                spawnStream(board, x, y, 1.0, 1.0, 15, baseDamage, plant);
                spawnStream(board, x, y, 1.0, -1.0, 15, baseDamage, plant);
            }
            else if (name.equals("Pea Pod")) {
                int stacks = plant.getStackCount();
                for (int i = 0; i < stacks; i++) {
                    spawnGiantPea(board, x + (i * 0.5), y, 1.0, 0.0, baseDamage * 20, plant);
                }
            }
            else if (name.equals("Rotobaga")) {
                spawnStream(board, x, y, 1.0, 1.0, 15, baseDamage, plant);
                spawnStream(board, x, y, 1.0, -1.0, 15, baseDamage, plant);
                spawnStream(board, x, y, -1.0, 1.0, 15, baseDamage, plant);
                spawnStream(board, x, y, -1.0, -1.0, 15, baseDamage, plant);
            }
            else if (name.equals("Snow Pea")) {
                int plantRow = (int) (y / Constants.Game.TILE_SIZE);
                for (Zombie z : board.getAllZombies()) {
                    if (!z.isDead() && z.getCurrentRow() == plantRow) {
                        StatusEffect freezeEffect = new StatusEffect(EffectType.FROZEN, 35);
                        z.addEffect(freezeEffect);
                    }
                }
                spawnStream(board, x, y, 1.0, 0.0, 60, baseDamage, plant);
            }
            else {
                spawnStream(board, x, y, 1.0, 0.0, 20, baseDamage, plant);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        plant.resetFeed();
    }

    private void spawnStream(GameBoard board, double startX, double startY, double xDir, double yDir, int count, int damage, Plant plant) throws Exception {
        for (int i = 0; i < count; i++) {
            double spawnX = startX + (i * 0.2 * xDir);
            double spawnY = startY + (i * 0.2 * yDir);

            Projectile proj;
            if (projectileType == IceProjectile.class) {
                double totalChillTime = 100.0 + plant.getChillTimeBonusTicks();
                proj = projectileType.getDeclaredConstructor(double.class, double.class, double.class, int.class, double.class)
                    .newInstance(spawnX, spawnY, 6.0, damage, totalChillTime);
            } else {
                proj = projectileType.getDeclaredConstructor(double.class, double.class, double.class, int.class)
                    .newInstance(spawnX, spawnY, 6.0, damage);
            }

            proj.setSourcePlantType(PlantType.getByName(plant.getName()));

            proj.setXSpeed(6.0 * xDir);
            proj.setYSpeed(6.0 * yDir);
            board.getActiveProjectiles().add(proj);
        }
    }

    private void spawnGiantPea(GameBoard board, double x, double y, double xDir, double yDir, int giantDamage, Plant plant) throws Exception {
        Projectile giantProj;

        if (projectileType == IceProjectile.class) {
            double totalChillTime = 100.0 + plant.getChillTimeBonusTicks();
            giantProj = projectileType.getDeclaredConstructor(double.class, double.class, double.class, int.class, double.class)
                .newInstance(x, y, 6.0, giantDamage, totalChillTime);
        } else {
            giantProj = projectileType.getDeclaredConstructor(double.class, double.class, double.class, int.class)
                .newInstance(x, y, 6.0, giantDamage);
        }

        giantProj.setSourcePlantType(PlantType.getByName(plant.getName()));

        giantProj.setXSpeed(6.0 * xDir);
        giantProj.setYSpeed(6.0 * yDir);
        board.getActiveProjectiles().add(giantProj);
    }
}
