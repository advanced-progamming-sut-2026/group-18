package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class FishermanZombie extends CapableZombie {

    public FishermanZombie(int health, double speed, int attackPower, int row, double startX,
                           double abilityCooldown, int abilityRange, double delta, double x, double y,
                           int xSpeed, int ySpeed) {
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);
        setXSpeed(0);
        setYSpeed(0);
        this.currentSpeed = 0;
    }

    @Override
    public void useAbility() {
        Object targetPlant = findTargetPlantInRow();

        // فیکس حیاتی: کول‌داون تنها زمانی ریست می‌شود که واقعاً گیاهی برای قلاب کردن وجود داشته باشد!
        if (targetPlant != null) {
            if (isPlantDirectlyNextToMe(targetPlant)) {
                throwAndDestroyPlant(targetPlant);
            } else if (isGridToTheRightEmpty(targetPlant)) {
                pullPlantOneGridForward(targetPlant);
            }
            resetCooldown();
        }
    }

    @Override
    public void move() {}

    @Override
    public boolean canMove() { return false; }

    // متدهای تعامل با سرویس نقشه و کامبت
    private Object findTargetPlantInRow() { return null; }
    private boolean isPlantDirectlyNextToMe(Object plant) { return false; }
    private boolean isGridToTheRightEmpty(Object plant) { return false; }
    private void pullPlantOneGridForward(Object plant) {}
    private void throwAndDestroyPlant(Object plant) {}

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }
}
