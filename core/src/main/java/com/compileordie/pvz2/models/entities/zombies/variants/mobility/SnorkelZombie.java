package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.TileType;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public class SnorkelZombie extends Zombie {

    public static final int WAVE_COST = 200;
    private MovementState state;

    public SnorkelZombie(double health, double speed, int attackPower, int row, double startX,
                         double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.SNORKEL_ZOMBIE);
        state = MovementState.WALKING;
    }

    public static boolean isOceanTile(TileType type) {
        return type == TileType.SHALLOW_BEACH;
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

    /** برای این‌که View (GameScreen) بتونه حالت فعلی رو بخونه، مثلا برای افکت
     * بصری زیر آب - قبلا هیچ getter‌ای نبود. */
    public MovementState getState() {
        return this.state;
    }
    // =================================================================================================================


    @Override
    public void tick() {
        if (isEating) surface();
        super.tick();
    }


    @Override
    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        if (isDead()) return;
        boolean wasFrozenByIceBlock = isFrozenByIce;
        amount = absorbIceDamage(amount);
        if (wasFrozenByIceBlock && amount <= 0) return;
        if (damageType!=DamageType.POISON) takedDamage = true;
        if (damageType == DamageType.FIRE){
            this.removeStatusEffect(EffectType.FROZEN);
            this.removeStatusEffect(EffectType.CHILLED);
        }
        if (state == MovementState.UNDERWATER_NOT_SURFACED
            && !(damageType == DamageType.LOBBER || damageType == DamageType.EXPLOSIVE))
            return;
        this.health -= amount;
        if (health <= 0){
            health = 0;
            if (damageType==DamageType.EXPLOSIVE) killByExplosive = true;
            if (damageType==DamageType.LawnMower){
                QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, "MOWER");
            }
            else{
                if (plantType!=null) QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, plantType.name());
            }
        }
    }
}
