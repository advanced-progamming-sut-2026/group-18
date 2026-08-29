package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;

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
    public TombType type = TombType.NORMAL;
    // 👈 مثل zombie.takedDamage: هر بار دمیج واقعی می‌خوره true می‌شه، لایه‌ی
    // رندر (GameScreen) بعد از خوندنش دوباره false می‌کنه تا فلش نور یک‌بار پخش بشه.
    public boolean takedDamage = false;
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
        this.maxHealth = health;
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
        this.takedDamage = true;
        if (this.health <= 0) {
            this.health = 0;
            this.isDestroyed = true;
            this.type.dieSpawn(this, AppModel.gameSession.gameBoard);
            for (Tile tile : AppModel.gameSession.gameBoard.getAllTiles()) {
                if (tile.obstacle == this) {
                    tile.obstacle = null;
                }
            }
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
        if (zombie == null) {
            return; // اگه ساخته نشد (مثلا asset نداره)، ZombieBuilder خودش لاگ لازم رو زده
        }
        // 🌪️ این زامبی از وسط زمین اسپاون می‌شه (نه از لبه‌ی چپ عادی)، پس باید
        // اول ۱.۵ ثانیه افکت SANDSTORM_TOP پخش بشه، بعد خودش ظاهر بشه.
        zombie.startSandstormSpawn();
        gameBoard.getLane(getRow()).zombies.add(zombie);
    }
}
