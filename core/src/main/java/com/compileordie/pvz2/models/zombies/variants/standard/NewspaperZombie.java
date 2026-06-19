package com.compileordie.pvz2.models.zombies.variants.standard;

public class NewspaperZombie extends StandardZombie {
    private int newspaperHealth;
    private boolean isEnraged;

    public NewspaperZombie(int health, double speed, int attackPower, int row, double startX, int initialArmor) {
        super(health, speed, attackPower, row, startX, initialArmor);
        this.isEnraged = false;
    }

    @Override
    public int takeArmorDamage(int amount) {
        if (hasArmor()) {
            this.armorHealth -= amount;
            if (this.armorHealth <= 0) {
                removeArmor();
                if (!hasArmor() && !isEnraged) {
                    enterEnrageMode();
                }
                return (-armorHealth);
            }
            return 0;
        }
        return amount;
    }

    @Override
    public void enterEnrageMode() {
        this.isEnraged = true;
        this.currentSpeed = this.movementSpeed * 2.5;
    }

}
