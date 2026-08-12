//package com.compileordie.pvz2.models.entities.zombies.variants.capable;
//
//import com.compileordie.pvz2.config.Constants;
//import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
//import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
//
//public class JesterZombie extends CapableZombie {
//    public static final int WAVE_COST = 450;
//    private static final double SPIN_DURATION = 3.0;
////    private final double spinningSpeedMultiplier = 1.6;
//    private boolean isSpinning = false;
//    private double spinTimer = 0;
//
//    public JesterZombie(double health, double speed, int attackPower, int row, double startX,
//                        double x, double y,
//                        double xSpeed, double ySpeed) {
//        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.JESTER_ZOMBIE);
//    }
//
//    @Override
//    public void tick() {
//        super.tick();
//        if (isDead()) return;
//
//        float dt = 1 * Constants.Game.TIME_COEFFICIENT;
//        if (isSpinning) {
//            spinTimer += dt;
//            if (spinTimer >= SPIN_DURATION) {
//                stopSpinning();
//            }
//        }
//    }
//
//    public void startSpinning() {
//        this.isSpinning = true;
//        this.spinTimer = 0;
//    }
//
//    public void stopSpinning() {
//        this.isSpinning = false;
//        this.spinTimer = 0;
//    }
//
//    public boolean isSpinning() {
//        return isSpinning;
//    }
//
//    public boolean reflectProjectileFlag(DamageType type) {
//        return isSpinning && (type == DamageType.NORMAL || type == DamageType.ICE);
//    }
//
//
//    @Override
//    public void takeDamage(double amount, DamageType damageType) {
//        if (isDead()) return;
//
//        // عدم دریافت آسیب تیرهای مستقیم حین چرخش
//        if (isSpinning && (damageType == DamageType.NORMAL || damageType == DamageType.ICE)) {
//            return;
//        }
//
//        this.health -= amount;
//        if (this.health <= 0) {
//            this.health = 0;
//            handleDeath();
//        }
//    }
//}
