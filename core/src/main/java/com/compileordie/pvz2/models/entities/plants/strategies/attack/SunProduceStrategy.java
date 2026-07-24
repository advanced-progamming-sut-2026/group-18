package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;

public class SunProduceStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        // The baseDamage field of a Sun Producer safely holds how much sun it drops
        int sunValue = plant.getBaseDamage();

        // Spawn the sun slightly offset from the plant's exact center
        double spawnX = plant.getX() + 0.5;
        double spawnY = plant.getY();

        Sun droppedSun = new Sun(spawnX, spawnY, sunValue, SunType.NORMAL);

        // Push the sun into the economy manager or board state
        board.getEconomyManager().addSun(droppedSun);
    }
}
