package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;//package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;
//
//public class IceBlock extends MovableObject {
//
//    private final int row;
//    private double positionX;
//    private double positionY;
//
//    public IceBlock(double barrelHealth, int row, double positionX, double positionY) {
//        super(barrelHealth, positionX, positionY);
//        this.row = row;
//        this.positionX = positionX;
//        this.positionY = positionY;
//    }
//
//    /**
//     * آپدیت موقعیت بلوک‌های یخ وقتی زامبی آن‌ها را هل می‌دهد
//     */
//    public void updatePosition(double newX) {
//        this.positionX = newX;
//    }
//
//    @Override
//    public void takeDamage(int amount) {
//        if (this.isDestroyed) return;
//
//        this.health -= amount;
//        if (this.health <= 0) {
//            this.health = 0;
//            this.isDestroyed = true;
//        }
//    }
//
//    /**
//     * بلوک یخ سد راه تیرهاست، اما زامبی‌های هم‌جبهه می‌توانند از آن عبور کنند
//     */
//    public boolean isPassableByZombies() {
//        return true;
//    }
//
//    public int getRow() {
//        return row;
//    }
//
//    public double getPositionX() {
//        return positionX;
//    }
//}
