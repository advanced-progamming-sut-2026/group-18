package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparingInt;

public class HypnotizeEffect implements PlantFoodEffectStrategy {
    private final int targets;

    public HypnotizeEffect(int targets) {
        this.targets = targets;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        // --- HYPNO-SHROOM LOGIC ---
        if (plant.getName().equals("Hypno-shroom")) {
            plant.setCurrentHp(plant.getBaseHp()); // Heal it
            // We intentionally DO NOT call resetFeed() here!
            // It stays 'isFed = true' so the next zombie that eats it becomes a Gargantuar.
            return;
        }

        List<Zombie> strongestZombies = board.getAllZombies().stream()
            .filter(z -> !z.isDead()) // 1. Don't target dead zombies
            .filter(z -> !z.hasEffect(EffectType.HYPNOTIZED)) // 2. Don't target already hypnotized zombies!
            .sorted((z1, z2) -> Double.compare(z2.getHealth(), z1.getHealth())) // 3. Sort by highest HP first
            .limit(targets) // 4. Only take the number of targets allowed by the Plant Food
            .toList();

        for (Zombie zombie : strongestZombies) {
            zombie.addEffect(new StatusEffect(EffectType.HYPNOTIZED, 99999));
        }

        // Effect resolved! Reset the feed flag.
        plant.resetFeed();
    }
}
