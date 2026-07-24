package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

import static com.compileordie.pvz2.models.AppModel.addAfterPrompt;

public class Sun extends GameEntity {
    public final float groundLevel;
    public final float lifespan;
    public SunType type;
    public boolean isNatual;
    public float tickCounter;
    public Zombie target;

    public Sun(double x, double y, SunType type, boolean isNatual, float groundLevel) {
        super(x, y, 0, 0);
        this.type = type;
        this.isNatual = isNatual;
        this.groundLevel = groundLevel;
        this.lifespan = isNatual ? ConfigManager.economy().natualSunLifespan : ConfigManager.economy().plantSunLifespan;
        this.tickCounter = 0f;
        this.target = null;
    }

    public float getRemainingTime() {
        return lifespan - tickCounter;
    }

    public void tick(int ticks, GameBoard gameBoard) {
        if (getRemainingTime() <= 0) {
            die();
        }

        if (target == null) {
            boolean wasInAir = getY() > groundLevel;

            float dt = ticks * Constants.Game.TIME_COEFFICIENT;
            if (getY() > groundLevel) {
                setYSpeed(getYSpeed() - dt * Constants.Game.GRAVITY_COEFFICIENT);
            } else {
                setYSpeed(0);
            }
            super.move(ticks);

            if (wasInAir && getY() <= groundLevel) {
                setX(groundLevel);
                addAfterPrompt("Sun reached the ground at position (" + (int) getX() + ", " + (int) groundLevel + ")");
            }
        } else {
            double deltaX = (target.getX() - this.getX()) * ConfigManager.economy().sunStealVelocity;
            double deltaY = (target.getY() - this.getY()) * ConfigManager.economy().sunStealVelocity;
            double distance = Math.hypot(deltaX, deltaY);
            setXSpeed(deltaX / distance * ConfigManager.economy().sunStealVelocity);
            setYSpeed(deltaY / distance * ConfigManager.economy().sunStealVelocity);
        }

        type.tick(ticks, this, gameBoard);

        tickCounter += ticks;
    }
}
