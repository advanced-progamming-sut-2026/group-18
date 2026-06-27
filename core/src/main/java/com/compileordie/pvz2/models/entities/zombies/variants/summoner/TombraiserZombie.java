package com.compileordie.pvz2.models.entities.zombies.variants.summoner;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class TombraiserZombie extends SummonerZombie {

    private final int numberOfTombsToSpawn; // طبق داک مقدار پیش‌فرض 2 است
    private final double timeBetweenRaisings;

    public TombraiserZombie(int health, double speed, int attackPower, int row, double startX,
                            double x, double y, int xSpeed, int ySpeed,
                            int numberOfTombsToSpawn, double timeBetweenRaisings) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, timeBetweenRaisings);
        this.numberOfTombsToSpawn = numberOfTombsToSpawn;
        this.timeBetweenRaisings = timeBetweenRaisings;
    }

    @Override
    public void move() {
        // حرکت عادی رو به جلو به سمت خانه بازیکن
        this.positionX -= this.currentSpeed;
    }

    @Override
    public void summon() {
        resetSummonCooldown();
    }

    @Override
    public void resetSummonCooldown() {
        this.summonCooldown = this.timeBetweenRaisings;
    }

    /**
     * داک: هر چند ثانیه یک بار، دو استخوان از دهان خود درمی‌آورد و پرتاب می‌کند.
     */
    public int launchBone() {
        if (!canSummon()) return 0;

        this.summon(); // اجرای پروسه احضار و ریست تایمر
        return this.numberOfTombsToSpawn;
    }

    /**
     * متد ساخت قبر (همان ساخت قبر مشخص شده در UML)
     */
    public Tomb spawnTomb(int tombHealth, int targetRow, double targetX) {
        return new Tomb(tombHealth, targetRow, targetX);
    }

    /**
     * پیاده‌سازی متد انتزاعی takeDamage برای رفع خطای کامپایل جاوا
     * این زامبی برخلاف دبه‌ای یا غارنشین، زره یا وسیله محافظ ندارد و دمیج مستقیماً از خونش کم می‌شود
     */
    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        this.health -= amount;
        if (this.health < 0) {
            this.health = 0;
        }
    }
}
