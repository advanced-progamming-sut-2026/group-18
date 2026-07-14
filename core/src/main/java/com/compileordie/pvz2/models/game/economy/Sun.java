package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

import static com.compileordie.pvz2.models.AppModel.addAfterPrompt;

public class Sun extends GameEntity {
    public SunType type;
    public boolean isNatual;
    public final float GROUND_LEVEL;
    public final float LIFESPAN;
    public float tickCounter;

    public Sun(double x, double y, SunType type, boolean isNatual, float groundLevel) {
        super(x, y, 0, 0);
        this.type = type;
        this.isNatual = isNatual;
        this.GROUND_LEVEL = groundLevel;
        this.LIFESPAN = isNatual ? ConfigManager.economy().natualSunLifespan : ConfigManager.economy().plantSunLifespan;
        this.tickCounter = 0f;
    }

    public float getRemainingTime() {
        return LIFESPAN - tickCounter;
    }

    public void tick(int ticks, GameBoard gameBoard) {
        if (getRemainingTime() <= 0) {
            die();
        }

        boolean wasInAir = getY() > GROUND_LEVEL;

        float dt = ticks * Constants.Game.TIME_COEFFICIENT;
        if (getY() > GROUND_LEVEL) {
            setYSpeed(getYSpeed() - dt * Constants.Game.GRAVITY_COEFFICIENT);
        } else {
            setYSpeed(0);
        }
        super.move(ticks);

        if (wasInAir && getY() <= GROUND_LEVEL) {
            addAfterPrompt("Sun reached the ground at position (" + (int) getX() + ", " + (int) GROUND_LEVEL + ")");
            if (this.type == SunType.RADIOACTIVE) {
                this.type = SunType.NORMAL;
            }
        }

        type.tick(ticks, this, gameBoard);

        tickCounter += ticks;
    }
}
