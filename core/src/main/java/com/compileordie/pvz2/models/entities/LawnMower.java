package com.compileordie.pvz2.models.entities;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.boss.GargantuarZombie;
import com.compileordie.pvz2.models.game.board.Lane;

import java.util.ArrayList;

public class LawnMower extends GameEntity {
    private static final double MOWER_SPEED = 1f;
    public Lane lane;
    public boolean isTriggered;

    public LawnMower(Lane lane) {
        // Correctly sets up position with respect to the continuous tracking system
        super(-Constants.Game.TILE_SIZE / 2f, Constants.Game.TILE_SIZE / 2f + lane.row, 0, 0);
        this.lane = lane;
        this.isTriggered = false;
    }

    public void tick(int ticks) {
        if (isTriggered) {
            ArrayList<Zombie> casualties = new ArrayList<>();
            for (int i = lane.zombies.size() - 1; i >= 0; i--) {
                Zombie zombie = lane.zombies.get(i);
                if (zombie.getX() <= this.getX()) {
                    if (!(zombie instanceof GargantuarZombie) && zombie.isAlive()) {
                        // NOTE: Might as well use zombie manager here.
                        casualties.add(zombie);
                        zombie.die();
                        lane.zombies.remove(i);
                    }
                }
            }

            if (!casualties.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("The lawn mower in the row ").append(lane.row).append(" is triggered and killed these zombies: ");
                for (int i = 0; i < casualties.size(); i++) {
                    sb.append(casualties.get(i).getType() == ZombieType.GARGANTUAR);
                    if (i < casualties.size() - 1) sb.append(", ");
                }
                AppModel.addAfterPrompt(sb.toString());
            }

            if (this.getX() >= lane.getLength()) {
                this.isTriggered = false;
                this.die();
            }

            move(ticks);
        } else {
            for (Zombie zombie : lane.zombies) {
                if (zombie.getX() <= 0) {
                    this.isTriggered = true;
                    this.setXSpeed(MOWER_SPEED);
                    break;
                }
            }
        }
    }
}
