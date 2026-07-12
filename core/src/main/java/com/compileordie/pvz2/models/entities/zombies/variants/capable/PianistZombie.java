package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class PianistZombie extends CapableZombie {
    private boolean isPlaying;

    public PianistZombie(int health, double speed, int attackPower, int row, double startX,
                         double abilityCooldown, int abilityRange, double delta, double x, double y,
                         int xSpeed, int ySpeed) {
        // فیکس: از ابیلیتی کول‌داون کلاس مادر به عنوان زمان‌بندی جابه‌جایی زامبی‌ها (Shuffle Cooldown) استفاده می‌کنیم
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);
        this.isPlaying = true;
    }

    @Override
    public void useAbility() {
        // فیکس معماری: سرویس بازی وقتی ببیند زمان توانایی رسیده (تایمر کلاس مادر صفر شده) این متد را صدا می‌زند
        if (isPlaying) {
            triggerZombiesRowShuffle();
            resetCooldown(); // تایمر برای شافل بعدی ریست می‌شود
        }
    }

    public void handlePlantCollision(Object plant) {
        if (plant != null) {
            destroyPlantInstantly(plant);
        }
    }

    private void destroyPlantInstantly(Object plant) {}
    private void triggerZombiesRowShuffle() {}

    @Override
    public void handleDeath() {
        this.isPlaying = false;
        super.handleDeath();
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }
}
