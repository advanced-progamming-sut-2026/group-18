package com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class DarkZomboss extends Zombie {
    public double health = 2000;
    public boolean death = false;
    public int rowUp;
    public int rowDown;
    public double baseX;
    public double speed = 3;
    public boolean idle = true;
    // ---------------------------

    public boolean stun = false;
    public double stunTimer = 0;
    public double targetHP = health * 2/3;

    public boolean spawnZombies = false;
    public double spawnTimer = 0;
    public double timerSpawn = 0;
    public double timeSpawn = 23;

    public boolean tran = false;
    public double timerTran = 12;
    public double timeTran = 14;

    public boolean smash = false;
    public double timerSmash = 21;
    public double timeSmash = 27;
    public double smashTimer = 0;

    public boolean boom = false;
    public double boomTimer = 0;
    public double timerBoom = 2;
    public double timeBoom = 20;
    public int r1 = -1;
    public int c1 = -1;
    public int r2 = -1;
    public int c2 = -1;



    public DarkZomboss(GameBoard gameBoard) {
        super(2000,
            0,
            0,
            2,
            15,
            15,
            Constants.Game.PADDING_Y_REALITY + 0.1 + 2 * Constants.Game.TILE_HEIGHT,
            0,
            0,
            ZombieType.ZOMBOSS_IN_DARK);
        baseX = getX();
        rowDown = 2;
        rowUp = 3;
        gameBoard.lanes.get(rowDown).zombies.add(this);
        gameBoard.lanes.get(rowUp).zombies.add(this);
    }

    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        health -= amount;
        if (health<=0){
            health = 0;
            death = true;
            die();
        }
    }

    /**
     * زامباس هم‌زمان دو ردیف (rowDown و rowUp) رو اشغال می‌کنه، پس برخلاف
     * زامبی‌های معمولی، برای هر دوتا این ردیف باید true برگرده - نه فقط
     * currentRow. این باعث می‌شه گیاهانِ هر دو لاین بتونن ببیننش و بهش شلیک
     * کنن، صرف‌نظر از این‌که الان کدوم لاینو بیشتر "اصلی" حساب می‌کنیم.
     */
    @Override
    public boolean occupiesRow(int row) {
        return row == rowUp || row == rowDown;
    }

    @Override
    public int closestRowTo(int referenceRow) {
        return Math.abs(rowUp - referenceRow) <= Math.abs(rowDown - referenceRow) ? rowUp : rowDown;
    }


    public void tick(){
        double dt = Constants.Game.TIME_COEFFICIENT;
        this.setY(Constants.Game.PADDING_Y_REALITY+0.1+rowDown*Constants.Game.TILE_HEIGHT);
        // با rowDown سینک می‌شه تا جاهایی که فقط یه عدد ردیف (نه تست عضویت)
        // از getCurrentRow() می‌خوان (مثل GooBoulderProjectile/IceProjectile)
        // یه مقدار واقعی و به‌روز بگیرن، نه مقدار ثابت اولیه‌ی constructor.
        this.setCurrentRow(rowDown);
        super.move(1);
        timerTran += dt;
        timerSmash += dt;
        timerSpawn += dt;
        timerBoom += dt;

        if (health<=targetHP && health>0){
            targetHP -= health/3;
            stun = true;
        }

        if (timerSpawn >= timeSpawn){
            timerSpawn = 0;
            if (!stun) spawnZombies = true;
        }

        if (timerTran >= timeTran){
            timerTran = 0;
            if (!stun && !spawnZombies) {
                if (rowDown==0){
                    rowDown++;
                    rowUp++;
                    AppModel.gameSession.gameBoard.lanes.get(rowDown-1).zombies.remove(this);
                    AppModel.gameSession.gameBoard.lanes.get(rowUp).zombies.add(this);
                }
                else if (rowUp==4){
                    rowDown--;
                    rowUp--;
                    AppModel.gameSession.gameBoard.lanes.get(rowUp+1).zombies.remove(this);
                    AppModel.gameSession.gameBoard.lanes.get(rowDown).zombies.add(this);
                }
                else {
                    if (Math.random() <= 0.5){
                        rowUp++;
                        rowDown++;
                        AppModel.gameSession.gameBoard.lanes.get(rowDown-1).zombies.remove(this);
                        AppModel.gameSession.gameBoard.lanes.get(rowUp).zombies.add(this);
                    }
                    else{
                        rowUp --;
                        rowDown --;
                        AppModel.gameSession.gameBoard.lanes.get(rowUp+1).zombies.remove(this);
                        AppModel.gameSession.gameBoard.lanes.get(rowDown).zombies.add(this);
                    }
                }
            }
            //---
        }

        //------------

        if (timerSmash >= timeSmash){
            timerSmash = 0;
            if (!stun && !spawnZombies){
                smash = true;
            }
        }

        if (timerBoom >= timeBoom){
            timerBoom = 0;
            if (!stun && ! spawnZombies && ! smash){
                boom = true;
            }
        }

        if (!stun && !smash && !spawnZombies && !boom) idle = true;
        else idle = false;



    }
}
