package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class ArmorBuffEffect implements PlantFoodEffectStrategy {
    private final int bonusArmorHp;

    public ArmorBuffEffect(int bonusArmorHp) {
        this.bonusArmorHp = bonusArmorHp;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        plant.setCurrentHp(plant.getBaseHp() + bonusArmorHp);
    }
}
