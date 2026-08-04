package com.compileordie.pvz2.models.game.minigames.vasebreaker;

import com.badlogic.gdx.math.MathUtils;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.PlantCard;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;

public class Vase extends GameEntity {
    public GameBoard gameBoard;
    public VaseType type;
    public ZombieType zombieType;
    public SeedPacket seedPacket;
    public boolean isBroken;

    public Vase(GameBoard gameBoard, int row, int column, ZombieType zombieType) {
        super((column + 0.5f) * Constants.Game.TILE_WIDTH, (row + 0.5f) * Constants.Game.TILE_HEIGHT, 0, 0);
        this.gameBoard = gameBoard;
        this.type = VaseType.NORMAL;
        this.zombieType = zombieType;
        this.seedPacket = null;
        this.isBroken = false;
    }

    public Vase(GameBoard gameBoard, int row, int column, PlantType plantType) {
        super((column + 0.5f) * Constants.Game.TILE_WIDTH, (row + 0.5f) * Constants.Game.TILE_HEIGHT, 0, 0);
        this.gameBoard = gameBoard;
        this.type = MathUtils.randomBoolean() ? VaseType.PLANT : VaseType.NORMAL;
        this.zombieType = null;
        this.seedPacket = new SeedPacket(plantType,
            getX() + MathUtils.random(Constants.Game.TILE_WIDTH),
            getY() + MathUtils.random(Constants.Game.TILE_HEIGHT));
    }

    public Vase(GameBoard gameBoard, int row, int column) {
        super((column + 0.5f) * Constants.Game.TILE_WIDTH, (row + 0.5f) * Constants.Game.TILE_HEIGHT, 0, 0);
        this.gameBoard = gameBoard;
        this.type = VaseType.GARGANTUAR;
        this.zombieType = ZombieType.GARGANTUAR;
        this.seedPacket = null;
    }

    public void breakVase() {
        if (zombieType != null) {
            gameBoard.getLane((float) getY()).zombies.add(ZombieBuilder.create(zombieType,
                getX(),
                getY(),
                getTileRow()));
            AppModel.addAfterPrompt("Vase released a " + zombieType + " zombie!");
        } else if (seedPacket != null) {
            gameBoard.seedPackets.add(seedPacket);
            AppModel.addAfterPrompt("Vase dropped a " + seedPacket.plantType + " seed packet!");
            // Phase 1:
            gameBoard.seedPackets.remove(seedPacket);
            gameBoard.economyManager.plantCards.add(new PlantCard(seedPacket.plantType));
        }
        if (type == VaseType.NORMAL) {
            gameBoard.economyManager.suns.add(new Sun(getX(), getY(), SunType.NORMAL, false, 0));
            AppModel.addAfterPrompt("Vase dropped a sun");
        }
        isBroken = true;
    }
}
