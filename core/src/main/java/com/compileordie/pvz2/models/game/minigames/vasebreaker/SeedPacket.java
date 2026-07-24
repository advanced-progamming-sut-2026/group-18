package com.compileordie.pvz2.models.game.minigames.vasebreaker;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public class SeedPacket extends GameEntity {
    public PlantType plantType;
    public final int despawnTicks;
    public int tickTimer;

    public SeedPacket(PlantType plantType, double x, double y) {
        super(x, y, 0, 0);
        this.plantType = plantType;
        this.despawnTicks =
            (int) Math.floor(ConfigManager.gameplay().seedPacketDespawn / Constants.Game.TIME_COEFFICIENT);
        this.tickTimer = 0;
    }

    public void tick(int ticks, GameBoard gameBoard) {
        if (tickTimer >= despawnTicks) {
            gameBoard.seedPackets.remove(this);
        }

        tickTimer += ticks;
    }
}
