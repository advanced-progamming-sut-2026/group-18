package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class OctopusZombie extends CapableZombie {

    public OctopusZombie(int health, double speed, int attackPower, int row, double startX,
                         double abilityCooldown, int abilityRange, double delta, double x, double y,
                         int xSpeed, int ySpeed) {
        // ابیلیتی رنج می‌تواند کل سطر را پوشش دهد تا گیاهان هدف را ببیند
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);
    }

    // ** مرتبط با سرویس خاص **
    @Override
    public void useAbility() {
        // سرویس بازی در متد tick چک می‌کند که اگر کول‌داون صفر بود و گیاهی در سطر حضور داشت، این متد را صدا بزند
        throwOctopusProjectile();
        resetCooldown();
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
    private void throwOctopusProjectile() {
        // بدنه این متد در لایه سرویس پرتابه‌ها (ProjectileService) پیاده‌سازی می‌شود.
        // سرویس یک پرتابه از نوع اختاپوس به سمت گیاه نگون‌بخت شلیک می‌کند.
    }
}
