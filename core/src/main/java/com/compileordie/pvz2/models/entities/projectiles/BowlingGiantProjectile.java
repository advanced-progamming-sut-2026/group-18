package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class BowlingGiantProjectile extends Projectile {

    public BowlingGiantProjectile(double x, double y, int damage) {
        super(x, y, 7.0, damage, DamageType.NORMAL);
        this.setSourcePlantType(PlantType.GIANT_WALL_NUT);
        this.ignoreObstacles = true;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        super.tick(board, delta);
        if (isDead) return;

        for (Zombie z : board.getAllZombies()) {
            if (z.isDead()) continue;

            // Radius is larger (0.8) because it's a GIANT nut!
            if (Math.abs(z.getX() - this.x) < Constants.Game.TILE_WIDTH * 0.8 &&
                Math.abs(z.getY() - this.y) < Constants.Game.TILE_HEIGHT * 0.5) {

                z.takeDamage(this.damage, DamageType.NORMAL, PlantType.WALL_NUT);
                // It does NOT die or bounce. It keeps rolling and crushing!
            }
        }
    }
}
