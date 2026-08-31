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

    // --- THE MISSING FLAG: Now saved so tick() can use it! ---
    private final boolean isReturning;

    private final double startY_sq;
    private final double targetY_sq;

    private double currentProgress = 0.0;
    private double jumpTimer = 0.0;
    private final double JUMP_DURATION = 20.0; // 20 ticks = fast, aggressive jumps!

    public SquashProjectile(Plant owner, double startX, double startY, double targetX, double targetY, boolean isReturning, int crushesRemaining, boolean isPlantFood) {
        super(startX, startY, targetX, 6.0, owner.getBaseDamage(), 0, 0.0);
        this.owner = owner;
        this.crushesRemaining = crushesRemaining;
        this.isPlantFood = isPlantFood;
        this.isReturning = isReturning;
        this.enumType = ProjectileType.LOBBED;

        this.startY_sq = startY;
        this.targetY_sq = targetY;

        this.setSourcePlantType(PlantType.getByName(owner.getName()));
    }

    @Override
    public void tick(GameBoard board, double delta) {
        this.jumpTimer += delta;

        double p = this.jumpTimer / JUMP_DURATION;
        if (p > 1.0) p = 1.0;
        this.currentProgress = p;

        // Interpolate BOTH X and Y so he physically flies across lanes!
        this.x = this.startX + ((this.targetX - this.startX) * p);
        this.y = this.startY_sq + ((this.targetY_sq - this.startY_sq) * p);

        // Massive 1.5-tile high vertical jump!
        this.altitude = Math.sin(p * Math.PI) * (Constants.Game.TILE_HEIGHT * 1.5);

        // When it lands!
        if (p >= 1.0) {

            // If this was the final return jump, just unhide the plant and stop!
            if (this.isReturning) {
                owner.setHidden(false);
                this.isDead = true;
                return;
            }

            // Calculate the row it LANDED in, not the row it Started in
            int landedRow = (int) Math.floor((this.y - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);

            Zombie victim = null;
            double closestDist = Double.MAX_VALUE;

            for (Zombie z : board.getAllZombies()) {
                if (z.isDead() || z.getCurrentRow() != landedRow) continue;

                double dist = Math.abs(z.getX() - this.targetX);

                if (dist <= Constants.Game.TILE_WIDTH * 0.6) {
                    if (dist < closestDist) {
                        closestDist = dist;
                        victim = z;
                    }
                }
            }

            // Deal Damage
            if (victim != null) {
                victim.takeDamage(this.damage, DamageType.NORMAL, PlantType.getByName(owner.getName()));
            }

            // Check for next jumps
            if (crushesRemaining > 1) {
                Zombie nextTarget = findTarget(board);
                if (nextTarget != null) {
                    SquashProjectile nextJump = new SquashProjectile(owner, this.x, this.y, nextTarget.getX(), nextTarget.getY(), false, crushesRemaining - 1, isPlantFood);
                    board.getActiveProjectiles().add(nextJump);
                } else {
                    triggerReturnOrDie(board);
                }
            } else {
                triggerReturnOrDie(board);
            }

            this.isDead = true;
        }
    }

    private void triggerReturnOrDie(GameBoard board) {
        if (isPlantFood) {
            // PF always returns to the home tile when finished!
            SquashProjectile returnJump = new SquashProjectile(owner, this.x, this.y, owner.getX(), owner.getY(), true, 0, true);
            board.getActiveProjectiles().add(returnJump);
        } else {
            // Normal attacks just die.
            owner.die();
        }
    }

    private Zombie findTarget(GameBoard board) {
        List<Zombie> zombies = board.getAllZombies();
        java.util.Collections.shuffle(zombies);

        for (Zombie z : zombies) {
            if (z.isDead()) continue;

            // Plant Food grabs any random zombie anywhere!
            if (isPlantFood) return z;

            // Normal logic only grabs zombies nearby in the same lane
            double dist = Math.abs(z.getX() - this.x);
            int ownerRow = (int) Math.floor((owner.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);

            if (dist <= 1.5 * Constants.Game.TILE_WIDTH && z.occupiesRow((int) (owner.getY() / Constants.Game.TILE_HEIGHT))) {
                return z;
            }
        }
        return null;
    }

    public double getSqStartX() { return startX; }
    public double getSqTargetX() { return targetX; }
    public double getSqProgress() { return currentProgress; }
}
