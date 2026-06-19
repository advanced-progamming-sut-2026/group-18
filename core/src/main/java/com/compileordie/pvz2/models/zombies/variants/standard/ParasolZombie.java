package com.compileordie.pvz2.models.zombies.variants.standard;

public class ParasolZombie extends StandardZombie {
    private boolean umbrellaActive;

    public ParasolZombie(int health, double speed, int attackPower, int row, double startX, int initialArmor) {
        super(health, speed, attackPower, row, startX, initialArmor);
        this.umbrellaActive = true;
    }

    public void deflectLobberProjectile() {
        if (umbrellaActive) {
            // TODO : منطق انحراف پرتابه
        }
    }

    public void openUmbrella() {
        this.umbrellaActive = true;
    }

    public void closeUmbrella() {
        this.umbrellaActive = false;
    }
}
