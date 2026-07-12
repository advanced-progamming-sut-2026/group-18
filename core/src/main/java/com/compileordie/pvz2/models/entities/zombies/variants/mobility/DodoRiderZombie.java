package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class DodoRiderZombie extends MobilityZombie {
    private boolean isFlyingState;
    private double flightTimer;
    private final double maxFlightDuration;
    private final double flyingSpeedModifier;

    public DodoRiderZombie(int health, double speed, int attackPower, int row, double startX,
                           int initialArmor, double delta, double x, double y, int xSpeed, int ySpeed,
                           double underwaterSpeedModifier, double maxFlightDuration, double flyingSpeedModifier) {
        super(health, speed, attackPower, row, startX, initialArmor, delta, x, y, xSpeed, ySpeed, underwaterSpeedModifier);
        this.isFlyingState = false;
        this.flightTimer = 0.0;
        this.maxFlightDuration = maxFlightDuration;
        this.flyingSpeedModifier = flyingSpeedModifier;
    }

    /**
     * مدیریت برخورد از طریق سرویس مبارزه (CombatService)
     */
    public void onPlantCollision(String plantType) {
        if (plantType == null) return;

        if (plantType.equalsIgnoreCase("TallNut")) {
            startEating();
            changeMovementState(MovementState.EATING);
            return;
        }

        if (isObstacleForDodo(plantType)) {
            if (!isFlyingState) {
                startFlyingOver();
            }
        } else {
            startEating();
            changeMovementState(MovementState.EATING);
        }
    }

    private boolean isObstacleForDodo(String plantType) {
        return plantType.equalsIgnoreCase("WallNut") ||
            plantType.equalsIgnoreCase("PotatoMine") ||
            plantType.equalsIgnoreCase("Garlic");
    }

    private void startFlyingOver() {
        this.isFlyingState = true;
        this.flightTimer = 0.0;
        changeMovementState(MovementState.FLYING);
    }

    @Override
    public void updateMovementState() {
        super.updateMovementState();
        if (this.movementState == MovementState.FLYING) {
            this.currentSpeed *= this.flyingSpeedModifier;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        if (isFlyingState) {
            this.flightTimer += this.delta;
            if (this.flightTimer >= this.maxFlightDuration) {
                land();
            }
        }
    }

    private void land() {
        this.isFlyingState = false;
        this.flightTimer = 0.0;
        changeMovementState(MovementState.WALKING);
    }

    public void onPathCleared() {
        if (this.movementState == MovementState.EATING) {
            stopEating();
            changeMovementState(MovementState.WALKING);
        }
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }
}
