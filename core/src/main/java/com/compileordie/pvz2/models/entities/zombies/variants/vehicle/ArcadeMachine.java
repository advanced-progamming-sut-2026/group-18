package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

public class ArcadeMachine extends MovableObject {

    public ArcadeMachine(int bucketHeadHealth, double x, double y) {
        super(bucketHeadHealth, x, y);
    }

    public void updatePosition(double zombieX, double zombieY) {
        this.setX(zombieX);
        this.setY(zombieY);
    }
}
