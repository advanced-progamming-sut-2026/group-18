package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.GooBoulderProjectile;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.user.Player;

public class GooPuddleEffect implements PlantFoodEffectStrategy {

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        try {
            int tickDamage = 6 + plant.getPoisonDmgTickBonus();

            // 1. Spawn the Giant Boulder
            Projectile boulder = new GooBoulderProjectile(plant.getX(), plant.getY(), 6.0, 600, tickDamage);
            boulder.setSourcePlantType(PlantType.GOO_PEASHOOTER);
            board.getActiveProjectiles().add(boulder);

            // 2. Coat the entire lane in front of the plant with the Poison Carpet!
            int plantRow = (int) Math.floor((plant.getY() - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
            int plantCol = (int) Math.floor((plant.getX() - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH);

            int limit = Math.min(plantCol + 6, board.totalCols);
            int startCol = plantCol + 1;

            for (int c = startCol; c < limit; c++) {
                Tile t = board.getTile(plantRow, c);
                if (t != null) {
                    t.puddleTimer = 300.0; // 10 seconds of active puddle!
                    t.puddleDamage = tickDamage;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Do NOT call plant.resetFeed() here! Let the graphics engine do it when the animation finishes!
    }
}
