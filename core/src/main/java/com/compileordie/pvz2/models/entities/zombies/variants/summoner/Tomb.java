package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.EnumSet;
import static com.compileordie.pvz2.models.entities.plants.enums.ProjectileType.*;

public class Tomb extends Obstacle {
    private double health;
    private final int row;
    private final int col;
    private final double positionX;
    private final double positionY;
    private boolean isDestroyed;
    private static final EnumSet<ProjectileType> BLOCKED_BY_GRAVESTONE = EnumSet.of(
        NORMAL,
        FIRE,
        ICE,
        POISON,
        LOBBED,
        ELECTRIC,
        BUTTER
    );

    public Tomb(double health, int row, int col, double positionX, double positionY) {
        super(positionX, positionY, ObstacleType.TOMB);
        this.health = health;
        this.row = row;
        this.col = col;
        this.positionX = positionX;
        this.positionY = positionY;
        this.isDestroyed = false;
    }

    public void takeDamage(double amount, ProjectileType type) {
        if (isDestroyed) return;
        if (!BLOCKED_BY_GRAVESTONE.contains(type)) return;
        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
            this.isDestroyed = true;
        }
    }

    // گترها برای استفاده در لایه رندر و سرویس برخورد تیرها
    public int getRow() { return row; }
    public int getCol() { return col; }
    public double getPositionX() { return positionX; }
    public double getPositionY() { return positionY; }
    public boolean isDestroyed() { return isDestroyed; }

    public void spawnZombie(GameBoard gameBoard, ZombieType zombieType) {
        Zombie zombie = ZombieBuilder.create(zombieType, getX(), getY(), getRow());
        gameBoard.getLane(getRow()).zombies.add(zombie);
    }
}
