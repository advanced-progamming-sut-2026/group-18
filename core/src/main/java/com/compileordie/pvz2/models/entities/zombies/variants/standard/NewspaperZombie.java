package com.compileordie.pvz2.models.entities.zombies.variants.standard;

public class NewspaperZombie extends StandardZombie {
    private boolean isEnraged;

    public NewspaperZombie(int health, double speed, int attackPower, int row, double startX, int initialArmor, double x, double y, int xSpeed, int ySpeed) {
        super(health, speed, attackPower, row, startX, initialArmor, x, y, xSpeed, ySpeed);
        this.isEnraged = false;
    }

    @Override
    public int takeArmorDamage(int amount) {
        if (hasArmor()) {
            this.armorHealth -= amount;
            if (this.armorHealth <= 0) {
                int overflow = -this.armorHealth;
                removeArmor();
                if (!isEnraged) {
                    enterEnrageMode();
                }
                return overflow;
            }
            return 0;
        }
        return amount;
    }

    @Override
    public void enterEnrageMode() {
        this.isEnraged = true;
        recalculateSpeed(); // اعمال آنی سرعت خشم
    }

    // فیکس حیاتی: بازنویسی متد بازخوانی سرعت برای جلوگیری از ریست شدن مالتیپلیر سرعت در هر تیک
    @Override
    public void recalculateSpeed() {
        super.recalculateSpeed();
        if (isEnraged) {
            this.currentSpeed *= 2.5;
        }
    }
}
