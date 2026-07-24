package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;
import com.compileordie.pvz2.models.user.Player;

public class BurstSunEffect implements PlantFoodEffectStrategy {
    private final int sunAmount;

    public BurstSunEffect(int sunAmount) {
        this.sunAmount = sunAmount;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        Sun massiveSun = new Sun(plant.getX() + 0.5, plant.getY(), sunAmount, SunType.NORMAL);
        board.getEconomyManager().addSun(massiveSun);
    }
}
