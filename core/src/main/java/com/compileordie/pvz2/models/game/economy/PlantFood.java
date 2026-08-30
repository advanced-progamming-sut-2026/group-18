package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public class PlantFood extends GameEntity {
    public final int despawnTicks;
    public int tickTimer;

    public PlantFood(double x, double y) {
        super(x, y, 0, 0);
        this.despawnTicks =
            (int) Math.floor(ConfigManager.gameplay().seedPacketDespawn / Constants.Game.TIME_COEFFICIENT);
        this.tickTimer = 0;
    }

    public void tick(int ticks, GameBoard gameBoard) {
        if (tickTimer >= despawnTicks) {
            gameBoard.plantFoods.remove(this);
        }

        tickTimer += ticks;
    }
}
