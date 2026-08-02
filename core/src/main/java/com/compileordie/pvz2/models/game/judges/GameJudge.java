package com.compileordie.pvz2.models.game.judges;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

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
        float deadline = lossCondition == LossCondition.DEAD_LINE ?
            ConfigManager.gameplay().deadlineShift - 0.1f : -0.1f;

        for (Lane lane : gameBoard.lanes) {
            for (Zombie zombie : lane.zombies) {
                if (zombie.getX() < deadline) {
                    lane.isLost = true;
                    break;
                }
            }
        }

        if (winCondition.evaluate(gameBoard)) {
            AppModel.addAfterPrompt("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");

            // --- QUEST INJECTION: MATCHING QuestDatabaseSeeder EXACTLY ---
            StringBuilder stats = new StringBuilder();

            // 1. Remaining Sun (e.g. REMAINING_SUN:0 for Defense Master)
            if (gameBoard.economyManager != null) {
                stats.append("REMAINING_SUN:").append(gameBoard.economyManager.sunAmount).append(",");
            }

            // 2. Thrifty Herbivore (PLANTS_LOST<=n tags)
            int lost = gameBoard.lostPlants;
            for (int n = lost; n <= 5; n++) {
                stats.append("PLANTS_LOST:<=").append(n).append(",");
            }

            // 3. Difficulty Streaks (DIFFICULTY:5)
            if (AppModel.player != null) {
                stats.append("DIFFICULTY:").append(AppModel.player.difficultyLevel).append(",");
            }

            // Dispatch to QuestManager
            QuestManager.dispatch(QuestEvent.LEVEL_CLEARED, 1, stats.toString());
            // -------------------------------------------------------------

            return GameFlow.WIN;

        } else if (lossCondition.evaluate(gameBoard)) {
            AppModel.addAfterPrompt("The zombie ate your brain; LOSER!!!");

            // Resets streak quests
            QuestManager.dispatch(QuestEvent.LEVEL_FAILED, 1, null);

            return GameFlow.LOSS;
        } else {
            return GameFlow.CONTINUE;
        }
    }
}
