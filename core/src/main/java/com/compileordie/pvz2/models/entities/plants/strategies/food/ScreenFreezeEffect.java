package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class ScreenFreezeEffect implements PlantFoodEffectStrategy {
    private final int freezeDurationTicks;

    public ScreenFreezeEffect(int freezeDurationTicks) {
        this.freezeDurationTicks = freezeDurationTicks;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;
            // FIXED: Uses EffectType.FROZEN and addEffect()
            zombie.addEffect(new StatusEffect(EffectType.FROZEN, freezeDurationTicks));
        }
    }

}
