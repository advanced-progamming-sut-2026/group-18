package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.List;

public class SquashProjectile extends LobbedProjectile {
    private final Plant owner;
    private final boolean isReturning;
    private final int crushesRemaining;
    private final boolean isPlantFood;

    public SquashProjectile(Plant owner, double startX, double startY, double targetX, double targetY, boolean isReturning, int crushesRemaining, boolean isPlantFood) {
        // Massive speed (6.0) so it jumps fast!
        super(startX, startY, targetX, 6.0, owner.getBaseDamage(), 0, 0.0);
        this.owner = owner;
        this.isReturning = isReturning;
        this.crushesRemaining = crushesRemaining;
        this.isPlantFood = isPlantFood;
        this.enumType = com.compileordie.pvz2.models.entities.plants.enums.ProjectileType.LOBBED;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        super.tick(board, delta);

        // --- CALCUALTE P LOCALLY ---
        double distanceTraveled = Math.abs(this.x - this.startX);
        double p = this.totalDistance == 0 ? 1.0 : (distanceTraveled / this.totalDistance);

        // When it lands!
        if (p >= 1.0) {
            if (!isReturning) {
                // --- 1. CRUSH THE ZOMBIE ---
                for (Zombie z : board.getAllZombies()) {
                    if (!z.isDead() && Math.abs(z.getX() - this.targetX) < 10) {
                        z.takeDamage(this.damage, DamageType.EXPLOSIVE, PlantType.getByName(owner.getName()));
                    }
                }

                // --- 2. LAUNCH THE RETURN JUMP BACK TO THE HOME TILE ---
                SquashProjectile returnJump = new SquashProjectile(owner, this.x, this.y, owner.getX(), owner.getY(), true, crushesRemaining, isPlantFood);
                board.getActiveProjectiles().add(returnJump);

            } else {
                // --- 3. WE MADE IT BACK TO THE TILE ---
                if (crushesRemaining > 1) {
                    // Try to find another victim!
                    Zombie nextTarget = findTarget(board);
                    if (nextTarget != null) {
                        SquashProjectile nextJump = new SquashProjectile(owner, this.x, this.y, nextTarget.getX(), nextTarget.getY(), false, crushesRemaining - 1, isPlantFood);
                        board.getActiveProjectiles().add(nextJump);
                    } else {
                        // No targets left, shut down.
                        owner.setHidden(false);
                        owner.setExhausted(true);
                    }
                } else {
                    // Final crush complete. Unhide the plant and turn it into a meat shield!
                    owner.setHidden(false);
                    owner.setExhausted(true);
                }
            }
            this.isDead = true; // Delete current projectile phase
        }
    }

    private Zombie findTarget(GameBoard board) {
        List<Zombie> zombies = board.getAllZombies();
        java.util.Collections.shuffle(zombies); // Randomize for Plant Food

        for (Zombie z : zombies) {
            if (z.isDead()) continue;

            // Plant food finds ANY zombie anywhere. Base attack only searches within 1.5 tiles!
            if (isPlantFood) return z;

            double dist = Math.abs(z.getX() - owner.getX());
            if (dist <= 1.5 * Constants.Game.TILE_WIDTH
                && z.getCurrentRow() == (int) (owner.getY() / Constants.Game.TILE_HEIGHT)) {
                return z;
            }
        }
        return null;
    }
}
