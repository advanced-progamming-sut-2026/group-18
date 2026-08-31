package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.badlogic.gdx.utils.Timer;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class ScreenFreezeEffect implements PlantFoodEffectStrategy {
    private final int baseFreezeDurationTicks;

    public ScreenFreezeEffect(int baseFreezeDurationTicks) {
        this.baseFreezeDurationTicks = baseFreezeDurationTicks;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        // Add the plant's upgrade bonus to the base duration!
        int totalFreezeTicks = baseFreezeDurationTicks + (int) plant.getFreezeTimeBonusTicks();

        for (Zombie zombie : board.getAllZombies()) {
            if (zombie.isDead()) continue;

            // Apply the freeze to every zombie on the board
            zombie.addEffect(new StatusEffect(EffectType.FROZEN, totalFreezeTicks));
        }

        // --- THE FIX: Sacrifice the plant exactly when the 2-second visual finishes! ---
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (!plant.isDead()) {
                    plant.die(); // Deletes it from the board cleanly!
                }
            }
        }, 0.3f);
    }
}
