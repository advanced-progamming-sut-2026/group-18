package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InstantKillEffect implements PlantFoodEffectStrategy {
    private final int targetCount;

    public InstantKillEffect(int targetCount) {
        this.targetCount = targetCount;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        List<Zombie> closestZombies = board.getAllZombies().stream()
            .filter(z -> !z.isDead())
            .sorted(Comparator.comparingDouble(z -> Math.hypot(z.getX() - plant.getX(), z.getY() - plant.getY())))
            .limit(targetCount)
            .collect(Collectors.toList());

        for (Zombie zombie : closestZombies) {
            zombie.takeDamage(99999, DamageType.NORMAL);
        }

        // Effect resolved! Reset the feed flag.
        plant.resetFeed();
    }
}
