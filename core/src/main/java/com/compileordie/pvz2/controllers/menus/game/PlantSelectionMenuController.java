package com.compileordie.pvz2.controllers.menus.game;

import com.compileordie.pvz2.controllers.menus.progression.CollectionMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.SessionBuilder;
import com.compileordie.pvz2.models.game.economy.PlantCard;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;

import java.util.List;

public class PlantSelectionMenuController {
    private PlantSelectionMenuController() {
    }

    public static Result<String> upgradePlant(PlantType plantType) {
        return CollectionMenuController.upgradePlant(plantType);
    }

    public static Result<Void> initializeGame(List<PlantCard> selectedCards) {
        int boostedCount = 0;
        for (PlantCard card : selectedCards) {
            if (card.isBoosted) boostedCount++;
        }

        int totalDiamondCost = boostedCount * 2;
        if (AppModel.player.diamonds < totalDiamondCost) {
            return Result.failure(
                "Insufficient diamonds! You need " + totalDiamondCost + " diamonds to bring "
                    + boostedCount + " boosted plant" + (boostedCount == 1 ? "" : "s")
            );
        }

        // 1. Deduct diamonds and consume one-time boosts
        AppModel.player.spendDiamonds(totalDiamondCost);
        for (PlantCard card : selectedCards) {
            if (card.isBoosted) {
                AppModel.player.plantBoosts.put(card.plantType, false);
            }
        }

        // 2. Save player changes to disk
        new UserDatabase(AppModel.player.username).save(AppModel.player);

        // 3. Populate selection deck
        AppModel.selectionDeck.clear();
        for (PlantCard card : selectedCards) {
            AppModel.selectionDeck.put(card.plantType, card.isBoosted);
        }

        // 3. Initiate the game session
        AppModel.gameSession = SessionBuilder.create(AppModel.currentLevel, AppModel.selectionDeck);
        return Result.success();
    }
}
