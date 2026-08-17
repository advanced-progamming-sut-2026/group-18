package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;

public class GooBoulderProjectile extends Projectile {
    private final int puddleTickDamage;

    public GooBoulderProjectile(double x, double y, double speed, int damage, int puddleTickDamage) {
        super(x, y, speed, damage, DamageType.POISON);
        this.enumType = ProjectileType.POISON;
        this.puddleTickDamage = puddleTickDamage;
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        // 1. Heavy Impact Damage
        target.takeDamage(this.damage, this.type, this.sourcePlantType);

        int laneIndex = target.getCurrentRow();

        // 2. Knockback: Push all zombies in this lane back 1 full tile
        for (Zombie z : board.getLane(laneIndex).zombies) {
            if (!z.isDead()) {
                z.setX(z.getX() + Constants.Game.TILE_SIZE);
            }
        }

        // 3. Stain the Tiles: Leave a puddle from the impact point to the right edge
        int impactCol = (int) (this.x / Constants.Game.TILE_SIZE);
        for (Tile t : board.getLane(laneIndex).tiles) {
            if (t.column >= impactCol - 1) { // -1 to cover the exact spot of impact nicely
                t.puddleTimer = 100.0; // 10 seconds
                t.puddleDamage = this.puddleTickDamage;
            }
        }

        // 4. Boom.
        this.destroy();
    }
}
