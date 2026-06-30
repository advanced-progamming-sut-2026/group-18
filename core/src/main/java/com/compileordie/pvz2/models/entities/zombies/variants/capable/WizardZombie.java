package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import java.util.ArrayList;
import java.util.List;

public class WizardZombie extends CapableZombie {
    private final List<Object> hexedPlants; // لیست گیاهانی که توسط این جادوگر خاص طلسم شده‌اند

    public WizardZombie(int health, double speed, int attackPower, int row, double startX,
                        double abilityCooldown, int abilityRange, double delta, double x, double y,
                        int xSpeed, int ySpeed) {
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);
        this.hexedPlants = new ArrayList<>();
        setDelta(delta);
    }

    // ** مرتبط با سرویس خاص **
    @Override
    public void useAbility() {
        // لایه سرویس یک گیاه عادی تصادفی را در کل زمین پیدا کرده و به متد transformPlantToCat پاس می‌دهد
        resetCooldown();
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;
    }

    /**
     * تبدیل گیاه هدف به گربه و ثبت در لیست طلسم‌های این جادوگر
     */
    public void transformPlantToCat(Object plant) {
        if (plant != null) {
            this.hexedPlants.add(plant);
            // داک: گیاهانی که گربه شدند، نه حمله میکنند نه خورده میشوند
            setPlantAsCat(plant, true);
        }
    }

    /**
     * داک: در صورت رسیدن به یک گیاه، او را نمیخورد. بلکه به گربه تبدیل میکند
     */
    public void handlePlantCollision(Object plant) {
        transformPlantToCat(plant);
    }

    /**
     * داک: گیاهانی که گربه شدند، تا زمان کشته شدن جادوگری که آنها را تلسم کرده گربه میماند و بعد آن به حالت عادی بازمیگردند
     */
    @Override
    public void handleDeath() {
        for (Object plant : hexedPlants) {
            setPlantAsCat(plant, false); // بازگرداندن گیاه به حالت عادی در لایه سرویس
        }
        hexedPlants.clear();
        super.handleDeath();
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    // ** مرتبط با سرویس خاص **
    private void setPlantAsCat(Object plant, boolean isCat) {
        // این متد ویژگی‌های گیاه را در لایه مدل/سرویس تغییر می‌دهد:
        // اگر isCat درست باشد: قابلیت حمله گیاه غیرفعال شده و فلگ قابل خوردن بودن آن false می‌شود تا زامبی‌ها از آن رد شوند.
        // اگر isCat غلط باشد: گیاه کاملاً به رفتار عادی خود برمی‌گردد.
    }
}
