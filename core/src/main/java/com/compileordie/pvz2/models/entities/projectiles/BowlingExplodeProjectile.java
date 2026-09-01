package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class BowlingExplodeProjectile extends Projectile {

    public BowlingExplodeProjectile(double x, double y, int damage) {
        super(x, y, 7.0, damage, DamageType.NORMAL);
        this.setSourcePlantType(PlantType.BOWLING_EXPLODE_O_NUT);
        this.ignoreObstacles = true;
    }

    // Movement is fully handled by Projectile.java natively now!

    @Override
    public void onHit(Zombie target, GameBoard board) {
        // BOOM! 3x3 Explosion!
        double explosionRadius = Constants.Game.TILE_WIDTH * 1.5;
        for (Zombie victim : board.getAllZombies()) {
            if (victim.isDead()) continue;

            if (Math.abs(victim.getX() - this.x) <= explosionRadius &&
                Math.abs(victim.getY() - this.y) <= explosionRadius) {
                victim.takeDamage(1800, DamageType.NORMAL, PlantType.BOWLING_EXPLODE_O_NUT);
            }
        }

        // NOW we destroy it! When it dies here, PlantMatchManager WILL see it die
        // and instantly spawn the Cherry Bomb visual!
        this.destroy();
    }
}
