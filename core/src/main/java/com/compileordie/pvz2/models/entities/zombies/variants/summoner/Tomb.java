package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.compileordie.pvz2.config.Constants;
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
    private final double maxHealth;
    private final int row;
    private final int col;
    private final double positionX;
    private final double positionY;
    private boolean isDestroyed;
    // 👈 مثل zombie.takedDamage: هر بار دمیج واقعی می‌خوره true می‌شه، لایه‌ی
    // رندر (GameScreen) بعد از خوندنش دوباره false می‌کنه تا فلش نور یک‌بار پخش بشه.
    public boolean takedDamage = false;
    private static final EnumSet<ProjectileType> BLOCKED_BY_GRAVESTONE = EnumSet.of(
        NORMAL,
        FIRE,
        ICE,
        POISON,
        BLUE_FIRE,
        PLASMA,
        SPORE,
        SPIKE,
        STAR,
        BULB_CYAN,
        BULB_BLUE,
        BULB_ORANGE
    );

    public Tomb(double health, int row, int col, double positionX, double positionY) {
        super(positionX+ Constants.Game.PADDING_X_REALITY, positionY, ObstacleType.TOMB);
        this.health = health;
        this.maxHealth = health;
        this.row = row;
        this.col = col;
        this.positionX = positionX+Constants.Game.PADDING_X_REALITY;
        this.positionY = positionY;
        this.isDestroyed = false;
    }

    public void takeDamage(double amount, ProjectileType type) {
        if (isDestroyed) return;
        if (!BLOCKED_BY_GRAVESTONE.contains(type)) return;
        this.health -= amount;
        this.takedDamage = true;
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
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }

    /**
     * نسبت جون فعلی به جون کامل (۱.۰ = سالم، ۰.۰ = نابود). برای انتخاب حالت
     * گرافیکی (undamaged/damage1..4) در لایه‌ی رندر استفاده می‌شه.
     */
    public double getHealthRatio() {
        if (maxHealth <= 0) return 0;
        return health / maxHealth;
    }

    public void spawnZombie(GameBoard gameBoard, ZombieType zombieType) {
        Zombie zombie = ZombieBuilder.create(zombieType, getX(), getY(), getRow());
        gameBoard.getLane(getRow()).zombies.add(zombie);
    }
}






