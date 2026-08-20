package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InstantKillEffect implements PlantFoodEffectStrategy {
    private final int targetCount;

    public InstantKillEffect(int targetCount) {
        this.targetCount = targetCount;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        // 1. Get all living zombies
        List<Zombie> livingZombies = board.getAllZombies().stream()
            .filter(z -> !z.isDead())
            .collect(Collectors.toList());

        // 2. Shuffle them to make the selection completely RANDOM!
        Collections.shuffle(livingZombies);

        // 3. Pick the random zombies
        List<Zombie> selectedZombies = livingZombies.stream()
            .limit(targetCount)
            .collect(Collectors.toList());

        // 4. Drop the lightning bolts!
        for (Zombie zombie : selectedZombies) {
            // Passed the PlantType for the quest tracker!
            zombie.takeDamage(99999, DamageType.NORMAL, PlantType.getByName(plant.getName()));
        }

        plant.resetFeed();
    }
}
