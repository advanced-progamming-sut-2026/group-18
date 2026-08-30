package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.List;

public class SquashProjectile extends LobbedProjectile {
    private final Plant owner;
    private final int crushesRemaining;
    private final boolean isPlantFood;

    // --- THE BULLETPROOF PHYSICS FIX ---
    private double currentProgress = 0.0;
    private double jumpTimer = 0.0;
    // 20 ticks = exactly 1 second jump time. (Increase to 30.0 if you want him to hang in the air longer!)
    private final double JUMP_DURATION = 20.0;

    public SquashProjectile(Plant owner, double startX, double startY, double targetX, double targetY, boolean isReturning, int crushesRemaining, boolean isPlantFood) {
        super(startX, startY, targetX, 6.0, owner.getBaseDamage(), 0, 0.0);
        this.owner = owner;
        this.crushesRemaining = crushesRemaining;
        this.isPlantFood = isPlantFood;
        this.enumType = ProjectileType.LOBBED;

        this.setSourcePlantType(PlantType.getByName(owner.getName()));
    }

    @Override
    public void tick(GameBoard board, double delta) {
        // DO NOT CALL super.tick() - We handle movement completely independently so no Area Damage happens!

        // 1. Advance the timer smoothly
        this.jumpTimer += delta;

        double p = this.jumpTimer / JUMP_DURATION;
        if (p > 1.0) p = 1.0;
        this.currentProgress = p;

        // 2. PERFECT INTERPOLATION: This one line flawlessly handles both forward AND backward jumps!
        this.x = this.startX + ((this.targetX - this.startX) * p);

        // 3. Massive 1.5-tile high vertical jump!
        this.altitude = Math.sin(p * Math.PI) * (Constants.Game.TILE_HEIGHT * 1.5);

// When it lands!
        if (p >= 1.0) {

            // --- THE ROW FIX: Figure out which row the Squash belongs to ---
            int ownerRow = (int) Math.floor((owner.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);

            // EXACTLY ONE ZOMBIE
            Zombie victim = null;
            double closestDist = Double.MAX_VALUE;

            for (Zombie z : board.getAllZombies()) {
                // --- THE ROW FIX: If the zombie is dead OR in a different row, ignore them! ---
                if (z.isDead() || z.getCurrentRow() != ownerRow) continue;

                double dist = Math.abs(z.getX() - this.targetX);

                // If they are in the landing zone...
                if (dist <= Constants.Game.TILE_WIDTH * 0.6) {
                    // Find the absolute closest one!
                    if (dist < closestDist) {
                        closestDist = dist;
                        victim = z;
                    }
                }
            }

            // Smash ONLY the single victim!
            if (victim != null) {
                victim.takeDamage(this.damage, DamageType.NORMAL, PlantType.getByName(owner.getName()));
            }

            // UPGRADE CHECK & DEATH
            if (crushesRemaining > 1) {
                Zombie nextTarget = findTarget(board);
                if (nextTarget != null) {
                    SquashProjectile nextJump = new SquashProjectile(owner, this.x, this.y, nextTarget.getX(), nextTarget.getY(), false, crushesRemaining - 1, isPlantFood);
                    board.getActiveProjectiles().add(nextJump);
                } else {
                    owner.die(); // No targets left
                }
            } else {
                owner.die(); // Standard behavior: 1 crush, delete plant
            }

            this.isDead = true;
        }
    }

    private Zombie findTarget(GameBoard board) {
        List<Zombie> zombies = board.getAllZombies();
        java.util.Collections.shuffle(zombies);

        for (Zombie z : zombies) {
            if (z.isDead()) continue;
            if (isPlantFood) return z;

            double dist = Math.abs(z.getX() - owner.getX());
            int ownerRow = (int) Math.floor((owner.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);

            if (dist <= 1.5 * Constants.Game.TILE_WIDTH && z.getCurrentRow() == ownerRow) {
                return z;
            }
        }
        return null;
    }

    public double getSqStartX() { return startX; }
    public double getSqTargetX() { return targetX; }
    public double getSqProgress() { return currentProgress; }
}
