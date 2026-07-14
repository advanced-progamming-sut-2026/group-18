package com.compileordie.pvz2.models.entities;

import com.compileordie.pvz2.config.Constants;

abstract public class GameEntity {
    private double x;
    private double y;
    private double xSpeed;
    private double ySpeed;

    public GameEntity(double x, double y, double xSpeed, double ySpeed) {
        this.x = x;
        this.y = y;
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
    }


    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getXSpeed() {
        return xSpeed;
    }

    public void setXSpeed(double xSpeed) {
        this.xSpeed = xSpeed;
    }

    public double getYSpeed() {
        return ySpeed;
    }

    public void setYSpeed(double ySpeed) {
        this.ySpeed = ySpeed;
    }

    public void move(int ticks) {
        float dt = ticks * Constants.Game.TIME_COEFFICIENT;
        this.x = x + xSpeed * dt;
        this.y = y + ySpeed * dt;
    }
}
