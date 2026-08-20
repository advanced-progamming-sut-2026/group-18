package com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

import java.awt.*;

public class EgyptZomboss extends Zombie {
    public double health = 15000;
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

    public boolean boom = false;
    public double boomTimer = 0;
    public double timerBoom = 2;
    public double timeBoom = 20;
    public int r = -1;
    public int c = -1;



    public EgyptZomboss() {
        super(15000, 0, 0, 2, 15, 15, Constants.Game.PADDING_Y_REALITY+0.1+2*Constants.Game.TILE_HEIGHT, 0, 0, ZombieType.ZOMBOSS_IN_EGYPT);
        baseX = getX();
        rowDown = 2;
        rowUp = 3;
        AppModel.gameSession.gameBoard.lanes.get(rowDown).zombies.add(this);
        AppModel.gameSession.gameBoard.lanes.get(rowUp).zombies.add(this);
    }

    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        health -= amount;
        if (health<=0){
            health = 0;
            death = true;
            die();
        }
    }


    public void tick(){
        double dt = Constants.Game.TIME_COEFFICIENT;
        this.setY(Constants.Game.PADDING_Y_REALITY+0.1+rowDown*Constants.Game.TILE_HEIGHT);
        super.move(1);
        if (getX()<=Constants.Game.PADDING_X+1){
            setXSpeed(-speed);
        }
        if (getX()>=baseX && getXSpeed() < 0){
            smash = false;
            idle = true;
            setXSpeed(0);
        }
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
                }
                else if (rowUp==4){
                    rowDown--;
                    rowUp--;
                }
                else {
                    if (Math.random() <= 0.5){
                        rowUp++;
                        rowDown++;
                    }
                    else{
                        rowUp --;
                        rowDown --;
                    }
                }
            }
        }

        //------------

        if (timerSmash >= timeSmash){
            timerSmash = 0;
            if (!stun && !spawnZombies){
                setXSpeed(speed);
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
