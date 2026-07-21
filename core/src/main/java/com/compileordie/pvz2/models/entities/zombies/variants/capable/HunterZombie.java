package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class HunterZombie extends CapableZombie {
    public static final int waveCost = 500;
    public static final float abilityCooldown = 0.7f;
    public static final double abilityRange = Constants.Game.TILE_WIDTH * 1.5;
    private boolean shouldAttack = false;
    private boolean shouldShut = false;
    private double timer = 0;
    private int shutCounter = 0;

    public HunterZombie(double health, double speed, int attackPower, int row, double startX,
                        double x, double y,
                        double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.HUNTER_ZOMBIE);
    }

    @Override
    public void move(int ticks){
        if (shouldAttack) return;
        super.move(ticks);
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;
        //---
        float dt = 1 * Constants.Game.TIME_COEFFICIENT;
        if (shouldAttack) {
            timer += dt;
            //---
            if (shutCounter == 3){
                if (timer >= abilityCooldown) {
                    shutCounter = 0;
                    shouldAttack = false;
                    shouldShut = false;
                    timer = 0;
                }
            }
            else if (timer >= abilityCooldown){
                shouldShut = true;
                shutCounter ++;
                timer = 0;
            }
        }
    }


    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }
    public void setShouldAttack(boolean a) { shouldAttack = a; }
    public boolean getShouldAttack() { return shouldAttack; }
    public void setShouldShut(boolean a) { shouldShut = a; }
    public boolean getShouldShut() { return shouldShut; }
}
