package com.compileordie.pvz2.models.game.judges;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

// Clean imports for the Quest System
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public class GameJudge {
    public GameBoard gameBoard;
    public WinCondition winCondition;
    public LossCondition lossCondition;

    public GameJudge(GameBoard gameBoard, WinCondition winCondition, LossCondition lossCondition) {
        this.gameBoard = gameBoard;
        this.winCondition = winCondition;
        this.lossCondition = lossCondition;
    }

    public GameFlow judge() {
        for (Lane lane : gameBoard.lanes) {
            for (Zombie zombie : lane.zombies) {
                if (zombie.succeeded) {
                    lane.isLost = true;
                    break;
                }
            }
        }

        if (winCondition.evaluate(gameBoard)) {
            AppModel.addAfterPrompt("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");

            // --- QUEST INJECTION: LEVEL CLEARED & STREAKS ---
            // Build the giant statistics string for the LevelConditionQuest and StreakQuest
            StringBuilder stats = new StringBuilder();

            if (gameBoard.economyManager != null) {
                stats.append("REMAINING_SUN:").append(gameBoard.economyManager.sunAmount).append(",");
            }

            stats.append("PLANTS_LOST:").append(gameBoard.lostPlants).append(",");

            if (AppModel.player != null) {
                // Assuming difficulty level is stored on the player object for streaks
                stats.append("DIFFICULTY:").append(AppModel.player.difficultyLevel).append(",");
            }

            // Dispatch the massive string to the Quest System cleanly!
            QuestManager.dispatch(QuestEvent.LEVEL_CLEARED, 1, stats.toString());
            // ------------------------------------------------

            return GameFlow.WIN;

        } else if (lossCondition.evaluate(gameBoard)) {
            AppModel.addAfterPrompt("The zombie ate your brain; LOSER!!!");

            // --- QUEST INJECTION: LEVEL FAILED (Resets Streaks) ---
            QuestManager.dispatch(QuestEvent.LEVEL_FAILED, 1, null);
            // ------------------------------------------------------

            return GameFlow.LOSS;
        } else {
            return GameFlow.CONTINUE;
        }
    }
}
