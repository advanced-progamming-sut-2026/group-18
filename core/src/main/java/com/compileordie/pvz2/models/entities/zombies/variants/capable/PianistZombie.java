package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class PianistZombie extends CapableZombie {
    private boolean isPlaying;
    private double shuffleTimer;
    private final double shuffleCooldown; // دریافت زمان‌بندی جابه‌جایی از سازنده برای عدم استفاده از عدد فرضی

    public PianistZombie(int health, double speed, int attackPower, int row, double startX,
                         double abilityCooldown, int abilityRange, double delta, double x, double y,
                         int xSpeed, int ySpeed, double shuffleCooldown) {
        super(health, speed, attackPower, row, startX, abilityCooldown, abilityRange, delta, x, y, xSpeed, ySpeed);
        this.isPlaying = true;
        this.shuffleTimer = 0;
        this.shuffleCooldown = shuffleCooldown;
    }

    @Override
    public void useAbility() {
        // توانایی پیانیست مداوم و محیطی است و لایه زمانی آن در tick مدیریت می‌شود
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        // داک: پیانو می‌نوازد و حین نواختن پیانو، زامبی‌ها هر چند ثانیه یک بار سطر خود را به صورت تصادفی با همسایه جابه‌جا می‌کنند
        if (isPlaying) {
            this.shuffleTimer += this.delta;
            if (this.shuffleTimer >= this.shuffleCooldown) {
                triggerZombiesRowShuffle();
                this.shuffleTimer = 0;
            }
        }
    }

    /**
     * داک: در صورت برخورد او با گیاهان، گیاهان از بین میروند
     */
    public void handlePlantCollision(Object plant) {
        if (plant != null) {
            destroyPlantInstantly(plant);
        }
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    @Override
    public void handleDeath() {
        this.isPlaying = false;
        super.handleDeath();
    }

    // ** مرتبط با سرویس خاص **
    private void destroyPlantInstantly(Object plant) {
        // لایه سرویس گیاه برخورد کرده را بدون فرآیند جویدن، فوراً نابود و از نقشه حذف می‌کند
    }

    // ** مرتبط با سرویس خاص **
    private void triggerZombiesRowShuffle() {
        // لایه سرویس تمام زامبی‌های زنده دیگر روی زمین را بررسی کرده و سطر آن‌ها را با یکی از سطرهای همسایه (با رعایت مرزهای بالا و پایین نقشه) جابه‌جا می‌کند
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }
}
