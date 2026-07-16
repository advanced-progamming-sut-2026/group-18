package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.game.board.GameBoard;

public abstract class Projectile extends GameEntity {

    private boolean isReversed = false;

    public Projectile(double x, double y, double xSpeed, double ySpeed) {
        // Matches GameEntity EXACTLY. No more red lines!
        super(x, y, xSpeed, ySpeed);
    }

    public abstract void tick(int tickDelta, GameBoard board);

    public abstract int getDamage();
    public abstract DamageType getType();

    // Hooks into GameEntity's lifecycle
    public void destroy() {
        this.die();
    }

    // The zombie engine looks for "isDead()", so we just invert GameEntity's "isAlive()"
    public boolean isDead() {
        return !this.isAlive();
    }

    public boolean isReversed() { return isReversed; }
    public void setReversed(boolean reversed) { isReversed = reversed; }
}
