package com.compileordie.pvz2.models.entities.obstacles;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.game.board.Tile;

public class IceBlock extends Obstacle {
    private double health;
    private final double maxHealth;
    private final int row;
    private final int col;
    private final double positionX;
    private final double positionY;
    private boolean isDestroyed;

    // For the rendering layer to flash white when hit (just like Tomb)
    public boolean takedDamage = false;

    public IceBlock(double health, int row, int col, double positionX, double positionY) {
        super(positionX, positionY, ObstacleType.ICE_BLOCK);
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

        if (type == ProjectileType.FIRE) {
            amount *= 2;
        }

        this.health -= amount;
        this.takedDamage = true;

        if (this.health <= 0) {
            this.health = 0;
            this.isDestroyed = true;

            // Instantly clear it from the game board when it melts/breaks
            if (AppModel.gameSession != null && AppModel.gameSession.gameBoard != null) {
                for (Tile tile : AppModel.gameSession.gameBoard.getAllTiles()) {
                    if (tile.obstacle == this) {
                        tile.obstacle = null;
                        break;
                    }
                }
            }
        }
    }

    // --- Getters for the graphics engine ---
    public int getRow() { return row; }
    public int getCol() { return col; }
    public double getPositionX() { return positionX; }
    public double getPositionY() { return positionY; }
    public boolean isDestroyed() { return isDestroyed; }
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }

    /**
     * Ratio of current health to max health (1.0 = full, 0.0 = destroyed).
     * The rendering engine can use this to draw cracks in the ice block over time!
     */
    public double getHealthRatio() {
        if (maxHealth <= 0) return 0;
        return health / maxHealth;
    }
}
