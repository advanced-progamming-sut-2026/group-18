package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class PianistZombie extends CapableZombie {
    public static final int WAVE_COST = 450;
    private boolean isPlaying;
    private double playingTimer;
    private double playingTime = 10;

    public PianistZombie(double health, double speed, int attackPower, int row, double startX,
                         double x, double y,
                         double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.PIANIST_ZOMBIE);
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;
        //---
        float dt = 1 * Constants.Game.TIME_COEFFICIENT;
        playingTimer += dt;
        if (playingTimer >= playingTime) {
            playingTimer = 0;
            isPlaying = true;
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void stopPlaying() {
        isPlaying = false;
    }
}
