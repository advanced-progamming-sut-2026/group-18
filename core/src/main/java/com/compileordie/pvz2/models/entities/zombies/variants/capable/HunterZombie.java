package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class HunterZombie extends CapableZombie {

    public HunterZombie(int health, double speed, int attackPower, int row, double startX,
                        double abilityCooldown, int abilityRange, double delta, double x, double y,
                        double xSpeed, double ySpeed) {
        // abilityRange می‌تواند کل سطر را پوشش دهد تا نزدیک‌ترین گیاه را ببیند
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed, ZombieType.HUNTER_ZOMBIE);
        setDelta(delta);
    }

    // ** مرتبط با سرویس خاص **
    @Override
    public void useAbility() {
        // سرویس در متد tick والد (CapableZombie) چک می‌کند که اگر زمان cooldown پر شده بود
        // و گیاهی در سطر زامبی وجود داشت، این متد را صدا بزند.
        throwIceProjectile();
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
    private void throwIceProjectile() {
        // بدنه این متد در لایه سرویس پرتابه‌ها (ProjectileService) پیاده‌سازی می‌شود.
        // سرویس یک پرتابه یخی تولید کرده و به سمت نزدیک‌ترین گیاه در همین سطر شلیک می‌کند.
    }
}
