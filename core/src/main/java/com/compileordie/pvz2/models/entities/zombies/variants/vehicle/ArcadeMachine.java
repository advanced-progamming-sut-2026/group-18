package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;

public class ArcadeMachine extends MovableObject {

    public ArcadeMachine(double bucketHeadHealth, double x, double y) {
        super(bucketHeadHealth, x, y, ObstacleType.ARCADE_MACHINE);
    }

    public void updatePosition(double zombieX, double zombieY) {
        this.setX(zombieX);
        this.setY(zombieY);
    }
}
