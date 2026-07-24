//package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;
//
//import com.compileordie.pvz2.config.Constants;
//import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
//import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
//
//public class ArcadeZombie extends VehicleZombie {
//    public static final int waveCost = 600;
//    public ArcadeZombie(int health, double speed, int attackPower, int row, double startX,
//                        double x, double y, double xSpeed, double ySpeed, double delta, int bucketHeadHealth) {
//        // ابتدا فرستادن اطلاعات به کلاس والد همراه با ساخت آبجکت آرکید ماشین با مختصات زامبی
//        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed,
//            new ArcadeMachine(bucketHeadHealth, x, y), ZombieType.ARCADE_ZOMBIE);
//    }
//
//    public void pushMachine(int ticks) {
//        super.pushVehicle(ticks);
//        if (this.vehicle instanceof ArcadeMachine) {
//            ((ArcadeMachine) this.vehicle).updatePosition(this.getX(), this.getY());
//        }
//    }
//
//    @Override
//    public void move(int ticks) {
//        if (!isVehicleDestroyed()) {
//            pushMachine(ticks);
//        } else {
//            float dt = ticks * Constants.Game.TIME_COEFFICIENT;
//            setX(getX() - getXSpeed() * dt);
//        }
//    }
//
//
//    public boolean onCollisionDetected() {
//        if (!isVehicleDestroyed()) {
//            pushMachine(1);
//            return true; // سیگنال لایه سرویس برای نابودی آنی گیاه برخورد کرده
//        } else {
//            startEating(); // دستگاه شکسته، پس مثل زامبی عادی ایستاده و می‌جود
//            return false;
//        }
//    }
//
//    public void onCabinetBreak(double damageAmount) {
//        if (this.vehicle != null) {
//            this.vehicle.takeDamage(damageAmount);
//        }
//    }
//
//    @Override
//    public void takeDamage(double amount, DamageType damageType) {
//        if (isDead()) return;
//
//        // تا زمان سلامت دستگاه، کل دمیج‌ها به عنوان سپر جذب کابین آرکید می‌شوند
//        if (!isVehicleDestroyed()) {
//            onCabinetBreak(amount);
//        } else {
//            this.health -= amount;
//        }
//
//        if (this.health < 0) this.health = 0;
//    }
//}
