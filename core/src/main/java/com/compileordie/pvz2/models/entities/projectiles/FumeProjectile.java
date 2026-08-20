package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.entities.zombies.variants.vehicle.Barrel;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.HashSet;
import java.util.Set;

public class FumeProjectile extends Projectile {
    private final double startX;
    private final double maxRangePixels;

    private final Set<Zombie> hitZombies = new HashSet<>();
    private final Set<Obstacle> hitObstacles = new HashSet<>();

    public FumeProjectile(double x, double y, double speed, int damage, double maxRangePixels) {
        super(x, y, speed, damage, DamageType.NORMAL);
        this.startX = x;
        this.maxRangePixels = maxRangePixels;
        this.enumType = ProjectileType.NORMAL;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        super.tick(board, delta);

        if (this.x - this.startX >= maxRangePixels) {
            this.isDead = true;
        }
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        if (hitZombies.contains(target)) return;
        hitZombies.add(target);

        target.takeDamage(this.damage, this.type, this.sourcePlantType);
    }

    @Override
    public void onObstacleHit(Obstacle obstacle) {
        if (hitObstacles.contains(obstacle)) return;
        hitObstacles.add(obstacle);

        // FIXED: Passing BOTH parameters (damage AND enumType) to the children!
        if (obstacle instanceof Tomb) {
            ((Tomb) obstacle).takeDamage(this.damage, this.enumType);
        } else if (obstacle instanceof Barrel) {
            ((Barrel) obstacle).takeDamage(this.damage);
        }
    }
}
