package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class FishermanZombie extends CapableZombie {

    public FishermanZombie(int health, double speed, int attackPower, int row, double startX,
                           double abilityCooldown, int abilityRange, double delta, double x, double y,
                           int xSpeed, int ySpeed) {
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);

        // داک: ماهیگیر در راست ترین ستون زمین ثابت میماند
        setXSpeed(0);
        setYSpeed(0);
        this.currentSpeed = 0;

        setDelta(delta);
    }

    // ** مرتبط با سرویس خاص **
    @Override
    public void useAbility() {
        // لایه سرویس ابتدا نزدیک‌ترین یا اولین گیاه زنده در سطر این زامبی را پیدا می‌کند
        Object targetPlant = findTargetPlantInRow();

        if (targetPlant != null) {
            // داک: در صورتی که گیاه در کنارش باشد، گیاه قلاب شده را پرتاب کرده و از بین میبرد
            if (isPlantDirectlyNextToMe(targetPlant)) {
                throwAndDestroyPlant(targetPlant);
            }
            // داک: یک خانه به جلو میاورد (خانه سمت راست گیاه باید خالی باشد)
            else if (isGridToTheRightEmpty(targetPlant)) {
                pullPlantOneGridForward(targetPlant);
            }
        }

        resetCooldown();
    }

    @Override
    public void move() {
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    // ** مرتبط با سرویس خاص **
    private Object findTargetPlantInRow() {
        // لایه سرویس نقشه، سطر زامبی را برای یافتن گیاه جستجو می‌کند
        return null;
    }

    // ** مرتبط با سرویس خاص **
    private boolean isPlantDirectlyNextToMe(Object plant) {
        // بررسی اینکه آیا گیاه هدف در ستون مجاور و چسبیده به زامبی قرار دارد یا خیر
        return false;
    }

    // ** مرتبط با سرویس خاص **
    private boolean isGridToTheRightEmpty(Object plant) {
        // بررسی خالی بودن مختصات/خانهٔ سمت راست گیاه هدف در لایه نقشه
        return false;
    }

    // ** مرتبط با سرویس خاص **
    private void pullPlantOneGridForward(Object plant) {
        // لایه سرویس مختصات گیاه را یک خانه به سمت راست (به سمت زامبی) منتقل می‌کند
    }

    // ** مرتبط با سرویس خاص **
    private void throwAndDestroyPlant(Object plant) {
        // لایه سرویس گیاه کنار زامبی را نابود و از نقشه حذف می‌کند
    }
}
