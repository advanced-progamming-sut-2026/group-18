package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.HomingProjectile;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;
import java.util.List;
import java.util.stream.Collectors;

public class HypnotizeEffect implements PlantFoodEffectStrategy {
    private final int targets;

    public HypnotizeEffect(int targets) {
        this.targets = targets;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        if (plant.getName().equals("Hypno-shroom")) {
            plant.setCurrentHp(plant.getBaseHp());
            return;
        }

        List<Zombie> strongestZombies = board.getAllZombies().stream()
            .filter(z -> !z.isDead() && !z.hasEffect(EffectType.HYPNOTIZED))
            .sorted((z1, z2) -> Double.compare(z2.getHealth(), z1.getHealth()))
            .limit(targets)
            .collect(Collectors.toList());

        for (Zombie zombie : strongestZombies) {
            try {
                Projectile proj = HomingProjectile.class.getDeclaredConstructor(
                        double.class, double.class, double.class, int.class, Zombie.class)
                    .newInstance(plant.getX(), plant.getY(), 8.0, 0, zombie);

                proj.setSourcePlantType(PlantType.CAULIPOWER);
                board.getActiveProjectiles().add(proj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        plant.resetFeed();
    }
}
