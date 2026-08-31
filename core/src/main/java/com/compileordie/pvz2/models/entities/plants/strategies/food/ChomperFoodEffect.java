package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.badlogic.gdx.utils.Timer;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChomperFoodEffect implements PlantFoodEffectStrategy {

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
        List<Zombie> laneZombies = new ArrayList<>();

        for (Zombie z : board.getAllZombies()) {
            if (!z.isDead() && z.getCurrentRow() == plantRow && z.getX() >= plant.getX() - 20) {
                laneZombies.add(z);
            }
        }

        laneZombies.sort(Comparator.comparingDouble(z -> z.getX()));

        int eaten = 0;
        for (Zombie z : laneZombies) {
            if (eaten >= 3) break;
            z.takeDamage(99999, DamageType.NORMAL, PlantType.getByName(plant.getName()));
            eaten++;
        }
        Timer.schedule(new Timer.Task() {
            int ticks = 0;
            @Override
            public void run() {
                if (plant.isDead()) { this.cancel(); return; }
                if (ticks < 20) {
                    double pushStep = (2.0 * Constants.Game.TILE_WIDTH) / 20.0;
                    double maxX = Constants.Game.PADDING_X + (board.totalCols * Constants.Game.TILE_WIDTH);

                    for (Zombie z : board.getAllZombies()) {
                        if (!z.isDead()) {
                            z.setX(Math.min(z.getX() + pushStep, maxX));
                        }
                    }
                }

                ticks++;

                if (ticks >= 80) {
                    plant.resetFeed();
                    this.cancel();
                }
            }
        }, 0.5f, 0.1f, 79);
    }
}
