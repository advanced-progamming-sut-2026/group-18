package com.compileordie.pvz2.models.entities;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.boss.GargantuarZombie;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

import java.util.ArrayList;

public class LawnMower extends GameEntity {
    private static final double MOWER_SPEED = 1.5f;
    public Lane lane;
    public boolean isTriggered;

    public LawnMower(Lane lane) {
        // Correctly sets up position with respect to the continuous tracking system
        super(Constants.Game.X_OF_MOWER,
                Constants.Game.TILE_HEIGHT * (lane.row + 0.7f) + Constants.Game.PADDING_Y,
                0,
                0);
        this.lane = lane;
        this.isTriggered = false;
    }

    public void tick(int ticks) {
        if (!isAlive()) return;
        if (isTriggered) {
            ArrayList<Zombie> casualties = new ArrayList<>();
            for (int i = lane.zombies.size() - 1; i >= 0; i--) {
                Zombie zombie = lane.zombies.get(i);
                if (zombie.getX() <= this.getX()+0.6 && Math.abs(zombie.getX()-this.getX())<=0.6) {
                    if (!(zombie instanceof GargantuarZombie)
                        && !(zombie.getType().toString().toLowerCase().contains("zomboss"))
                        && zombie.isAlive()) {
                        casualties.add(zombie);
                        zombie.takeDamage(99999, DamageType.LawnMower);
                        lane.zombies.remove(i);
                    }
                }
            }

            if (!casualties.isEmpty()) {
                QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, casualties.size(), "MOWER");
                StringBuilder sb = new StringBuilder();
                sb.append("The lawn mower in the row ")
                    .append(lane.row)
                    .append(" is triggered and killed these zombies: ");
                for (int i = 0; i < casualties.size(); i++) {
                    sb.append(casualties.get(i).getType().toString());
                    if (i < casualties.size() - 1) sb.append(", ");
                }
            }

            if (this.getX() >= Constants.Game.LANE_LENGTH) {
                this.isTriggered = false;
                this.die();
            }

            move(ticks);
        } else {
            for (Zombie zombie : lane.zombies) {
                if (zombie.getX() <= this.getX()+Constants.Game.PADDING_FOR_MOWER) {
                    this.isTriggered = true;
                    this.setXSpeed(MOWER_SPEED);
                    break;
                }
            }
        }
    }
}
