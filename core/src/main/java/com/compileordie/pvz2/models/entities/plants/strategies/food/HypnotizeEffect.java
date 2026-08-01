package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

import java.util.Comparator;
import java.util.List;

public class HypnotizeEffect implements PlantFoodEffectStrategy {
    private final int targets;

    public HypnotizeEffect(int targets) {
        this.targets = targets;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        List<Zombie> strongestZombies = board.getAllZombies().stream()
            // ... (keep the stream logic)
            .toList();

        for (Zombie zombie : strongestZombies) {
            // FIXED: Uses EffectType.HYPNOTIZED and addEffect()
            zombie.addEffect(new StatusEffect(EffectType.HYPNOTIZED, 99999));
            // Removed zombie.reverseDirection() because Zombie.tick() handles it!
        }
    }

}
