package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class DodoRiderZombie extends MobilityZombie {

    private boolean isFlyingState;
    private double flightTimer;
    private final double maxFlightDuration; // مدت زمان پرواز از روی مانع بر اساس ثانیه/دلتا
    private final double flyingSpeedModifier; // ضریب تغییر سرعت در حالت پرواز

    public DodoRiderZombie(int health, double speed, int attackPower, int row, double startX,
                           int initialArmor, double delta, double x, double y, int xSpeed, int ySpeed,
                           double underwaterSpeedModifier, double maxFlightDuration, double flyingSpeedModifier) {
        super(health, speed, attackPower, row, startX, initialArmor, delta, x, y, xSpeed, ySpeed, underwaterSpeedModifier);
        setDelta(delta);

        this.isFlyingState = false;
        this.flightTimer = 0.0;
        this.maxFlightDuration = maxFlightDuration;
        this.flyingSpeedModifier = flyingSpeedModifier;
    }

    /**
     * شنونده رویداد برخورد با گیاه که توسط سرویس منیجر بازی صدا زده می‌شود
     * @param plantType نام یا نوع گیاه (برای تشخیص منطق داکیومنت)
     */
    // TODO : ببینیم بررسی تایپ گیاه چیجوریاست
    public void onPlantCollision(String plantType) {
        if (plantType == null) return;

        // داک: از گردوی بلند (tall-nut) رد نمی شود.
        if (plantType.equalsIgnoreCase("TallNut")) {
            startEating(); // فعال کردن ترمز فیزیکی در کلاس ریشه Zombie (isEating = true)
            changeMovementState(MovementState.EATING);
            return;
        }

        // داک: بررسی موانع خاص (گردو، سیر، مین) برای پرواز
        if (isObstacleForDodo(plantType)) {
            if (!isFlyingState) {
                startFlyingOver(); // شروع پرواز و رد شدن از روی مانع بدون فعال کردن ترمز زامبی
            }
        } else {
            // داک: از بقیه موارد پرواز نمی کند. (پس متوقف می‌شود و آن‌ها را می‌خورد)
            startEating(); // فعال کردن ترمز فیزیکی در کلاس ریشه Zombie (isEating = true)
            changeMovementState(MovementState.EATING);
        }
    }

    /**
     * متد کمکی برای تطبیق با انواع موانع ذکر شده در داکیومنت شما
     */
    private boolean isObstacleForDodo(String plantType) {
        return plantType.equalsIgnoreCase("WallNut") ||
            plantType.equalsIgnoreCase("PotatoMine") ||
            plantType.equalsIgnoreCase("Garlic");
    }

    /**
     * تغییر حالت زامبی به پرواز
     */
    private void startFlyingOver() {
        this.isFlyingState = true;
        this.flightTimer = 0.0;
        changeMovementState(MovementState.FLYING);
    }

    /**
     * بازنویسی متد به‌روزرسانی سرعت برای اعمال ضریب سرعت مخصوص پرواز دودو
     */
    @Override
    public void updateMovementState() {
        if (this.movementState == MovementState.FLYING) {
            this.currentSpeed = this.movementSpeed * this.flyingSpeedModifier;
        } else {
            super.updateMovementState();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        // مدیریت مدت زمان پرواز با استفاده از دلتا
        if (isFlyingState) {
            this.flightTimer += this.delta;
            if (this.flightTimer >= this.maxFlightDuration) {
                land();
            }
        }
    }

    /**
     * فرود آمدن زامبی پس از اتمام زمان مجاز پرواز روی موانع
     */
    private void land() {
        this.isFlyingState = false;
        this.flightTimer = 0.0;
        changeMovementState(MovementState.WALKING);
    }

    /**
     * رویداد پاک شدن مسیر (توسط سرویس مدیریت نقشه صدا زده می‌شود)
     */
    public void onPathCleared() {
        if (this.movementState == MovementState.EATING) {
            stopEating(); // آزاد کردن ترمز فیزیکی کلاس ریشه Zombie
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
