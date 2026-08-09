package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.TileType;

public class SnorkelZombie extends Zombie {

    public static final int WAVE_COST = 200;
    private MovementState state;

    public SnorkelZombie(double health, double speed, int attackPower, int row, double startX,
                         double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.SNORKEL_ZOMBIE);
        state = MovementState.WALKING;
    }

    public static boolean isOceanTile(TileType type) {
        if (type == TileType.DEEP_BEACH || type == TileType.SHALLOW_BEACH) return true;
        return false;
    }


    // =================================================================================================================
    // از این سه‌گانه برای هندل کردن استیت غواص در هررر تیک باید استفاده بشه + موقعی که میرسه به گیاه برای خوردن
    // =================================================================================================================
    public void underwater() {
        this.state = MovementState.UNDERWATER_NOT_SURFACED;
    }

    public void surface() {
        this.state = MovementState.UNDERWATER_SURFACED;
    }

    public void walk() {
        this.state = MovementState.WALKING;
    }
    // =================================================================================================================


    @Override
    public void tick() {
        if (isEating) surface();
        super.tick();
    }


    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        if (damageType == DamageType.FIRE){
            this.removeFrozen();
        }
        if (state == MovementState.UNDERWATER_NOT_SURFACED
            && !(damageType == DamageType.LOBBER || damageType == DamageType.EXPLOSIVE))
            return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }
}
