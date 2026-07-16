package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class BowlingProjectile extends Projectile {

    private final int damage;
    private int bounceCount = 0;
    private static final int MAX_BOUNCES = 3;
    private static final int RICOCHET_VERTICAL_SPEED = 60;

    public BowlingProjectile(double x, double y, double xSpeed, double ySpeed, int bulbLevel) {
        super(x, y, xSpeed, ySpeed);

        if (bulbLevel == 1) this.damage = 40;
        else if (bulbLevel == 2) this.damage = 120;
        else if (bulbLevel == 3) this.damage = 180;
        else this.damage = 0;
    }

    @Override
    public void tick(int tickDelta, GameBoard board) {
        // Look how clean this is now! We just use the parent's move method.
        this.move(tickDelta);
    }

    @Override
    public void destroy() {
        bounceCount++;

        if (bounceCount >= MAX_BOUNCES) {
            super.destroy(); // Calls die() in GameEntity
        } else {
            handleRicochet();
        }
    }

    private void handleRicochet() {
        if (this.getYSpeed() <= 0) {
            this.setYSpeed(RICOCHET_VERTICAL_SPEED);
        } else {
            this.setYSpeed(-RICOCHET_VERTICAL_SPEED);
        }
    }

    @Override
    public int getDamage() { return this.damage; }

    @Override
    public DamageType getType() { return DamageType.LOBBER; }
}
