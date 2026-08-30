package com.compileordie.pvz2.models.entities.plants.strategies.attack;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.*;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
public class LobberStrategy implements AttackStrategy {
    private final Class<? extends Projectile> projectileType;
    private final double splashRadiusTiles;
    public LobberStrategy(Class<? extends Projectile> projectileType, double splashRadiusTiles) {
        this.projectileType = projectileType;
        this.splashRadiusTiles = splashRadiusTiles;
    }
    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
        java.util.Optional<Zombie> targetZombie = board.getAllZombies().stream()
            .filter(z -> !z.isDead() && z.getCurrentRow() == plantRow && z.getX() >= plant.getX())
            .min((z1, z2) -> Double.compare(z1.getX(), z2.getX()));
        if (targetZombie.isEmpty()) {
            plant.holdAction = true;
            plant.isWindingUp = false;
            return;
        }
        if (!plant.isWindingUp) {
            plant.isWindingUp = true;
            plant.windupTimer = 0;
            plant.holdAction = true;
            if (plant.getName().equals("Kernel-pult")) {
                plant.isFiringButter = (Math.random() * 100 < plant.getButterChance());
            }
            return;
        }
        plant.windupTimer += tickDelta;
        plant.holdAction = true;
        double requiredWindup = 10.0;
        if (plant.getName().equals("Melon-pult") || plant.getName().equals("Winter Melon")) {
            requiredWindup = 16.0;
        }
        if (plant.windupTimer < requiredWindup) {
            return;
        }
        plant.isWindingUp = false;
        plant.holdAction = false;
        double targetX = targetZombie.get().getX() - (Constants.Game.TILE_WIDTH * 0.45);
        try {
            Class<? extends Projectile> currentProjClass = projectileType;
            int finalDamage = plant.getBaseDamage();
            if (plant.getName().equals("Kernel-pult") && plant.isFiringButter) {
                currentProjClass = ButterProjectile.class;
                finalDamage *= 2;
            }
            else if (plant.getName().equals("Winter Melon")) {
                currentProjClass = IceLobbedProjectile.class;
            }
            else if (plant.getName().equals("Pepper-pult")) {
                currentProjClass = FireLobbedProjectile.class;
            }
            Projectile proj = currentProjClass
                .getDeclaredConstructor(double.class, double.class, double.class, double.class, int.class, int.class, double.class)
                .newInstance(plant.getX(), plant.getY(), targetX, 3.5, finalDamage, plant.getAoeDamage(), splashRadiusTiles);
            proj.setSourcePlantType(PlantType.getByName(plant.getName()));
            board.getActiveProjectiles().add(proj);
        } catch (Exception e) {}
    }
}
