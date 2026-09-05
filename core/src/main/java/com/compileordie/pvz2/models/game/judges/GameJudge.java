package com.compileordie.pvz2.models.game.judges;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.PlantSpawner;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.levels.LevelType;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public class GameJudge {
    // Every "family" the QuestDatabaseSeeder currently builds Family Slayer /
    // Flourishing in Limits quests for. Kept in sync with QuestDatabaseSeeder by hand,
    // same as the PLANTS_LOST/DIFFICULTY ranges below already were.
    private static final String[] PLANT_FAMILIES = {"PEA", "MINT", "ARMAMINT"};

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

        if (AppModel.isReceiverClient) return GameFlow.CONTINUE;

        if (winCondition.evaluate(gameBoard)) {
            // --- QUEST INJECTION: MATCHING QuestDatabaseSeeder EXACTLY ---
            StringBuilder stats = new StringBuilder();

            // 1. Remaining Sun (e.g. REMAINING_SUN:0 for Defense Master)
            if (gameBoard.economyManager != null) {
                stats.append(Constants.QuestCallbacks.REMAINING_SUN).append(":")
                    .append(gameBoard.economyManager.sunAmount).append(",");
            }
            // 2. Thrifty Herbivore (PLANTS_LOST:<=n tags)
            int lost = gameBoard.lostPlants;
            for (int n = lost; n <= 5; n++) {
                stats.append(Constants.QuestCallbacks.PLANTS_LOST).append(":<=").append(n).append(",");
            }
            // 3. Difficulty Streaks (DIFFICULTY:5)
            if (AppModel.player != null) {
                stats.append(Constants.QuestCallbacks.DIFFICULTY).append(":")
                    .append(AppModel.player.difficultyLevel).append(",");
            }
            // 4. Cloudy Day (SUN_PLANTS_USED:<=n tags), same "append every satisfied threshold"
            //    trick as Thrifty Herbivore above.
            int sunPlantsUsed = gameBoard.sunProducingPlantsPlanted;
            for (int n = sunPlantsUsed; n <= 5; n++) {
                stats.append(Constants.QuestCallbacks.SUN_PLANTS_USED).append(":<=").append(n).append(",");
            }
            // 5. Symmetry & What OCD? (lawn mirrored top-to-bottom, middle row excluded automatically)
            boolean symmetric = isBoardSymmetric();
            stats.append(Constants.QuestCallbacks.SYMMETRIC).append(":").append(symmetric ? "TRUE" : "FALSE")
                .append(",");
            stats.append(Constants.QuestCallbacks.ANTI_SYMMETRIC).append(":").append(!symmetric ? "TRUE" : "FALSE")
                .append(",");
            // 6. Night or Morning (a night/mushroom plant was used on an otherwise daytime level)
            boolean isDayLevel = AppModel.currentLevel != null && !AppModel.currentLevel.isNightLevel();
            boolean dayWithNight = isDayLevel && gameBoard.nightPlantPlanted;
            stats.append(Constants.QuestCallbacks.DAY_WITH_NIGHT).append(":").append(dayWithNight ? "TRUE" : "FALSE")
                .append(",");
            // 7. Family Slayer & Flourishing in Limits
            for (String family : PLANT_FAMILIES) {
                if (onlyKilledWithFamily(family)) {
                    stats.append(Constants.QuestCallbacks.ONLY_FAMILY).append(":").append(family).append(",");
                }
                if (neverPlantedFamily(family)) {
                    stats.append(Constants.QuestCallbacks.NO_FAMILY).append(":").append(family).append(",");
                }
            }
            // 8. One Less Column / Defenseless Row / Defenseless Cross
            //    (1-indexed, matching gameBoard.plantedColumns/plantedRows)
            for (int col = 1; col <= gameBoard.totalCols; col++) {
                if (!gameBoard.plantedColumns.contains(col)) {
                    stats.append(Constants.QuestCallbacks.EMPTY_COL).append(":").append(col).append(",");
                }
            }
            for (int row = 1; row <= gameBoard.totalRows; row++) {
                if (!gameBoard.plantedRows.contains(row)) {
                    stats.append(Constants.QuestCallbacks.EMPTY_ROW).append(":").append(row).append(",");
                }
            }
            for (int col = 1; col <= gameBoard.totalCols; col++) {
                if (gameBoard.plantedColumns.contains(col)) continue;
                for (int row = 1; row <= gameBoard.totalRows; row++) {
                    if (gameBoard.plantedRows.contains(row)) continue;
                    stats.append(Constants.QuestCallbacks.EMPTY_CROSS).append(":")
                        .append(col).append(",").append(row).append(",");
                }
            }

            // Dispatch to QuestManager
            QuestManager.dispatch(QuestEvent.LEVEL_CLEARED, 1, stats.toString());
            // -------------------------------------------------------------

            if (AppModel.currentLevel.levelType == LevelType.MINIGAME) {
                AppModel.player.completedMiniGames++;
            }
            return GameFlow.WIN;

        } else if (lossCondition.evaluate(gameBoard)) {
            // Resets streak quests
            QuestManager.dispatch(QuestEvent.LEVEL_FAILED, 1, null);

            return GameFlow.LOSS;
        } else {
            return GameFlow.CONTINUE;
        }
    }

    /**
     * The lawn is "symmetric" when every row mirrors its counterpart (row i vs. totalRows-1-i)
     * in terms of which columns have a plant on them. A middle row (odd row count) has no
     * counterpart and is naturally excluded, matching "What OCD?"'s "except for the middle row".
     */
    private boolean isBoardSymmetric() {
        int rows = gameBoard.totalRows;
        for (int i = 0; i < rows / 2; i++) {
            if (!rowsMirror(i, rows - 1 - i)) return false;
        }
        return true;
    }

    private boolean rowsMirror(int rowA, int rowB) {
        for (int col = 0; col < gameBoard.totalCols; col++) {
            Tile a = gameBoard.getTile(rowA, col);
            Tile b = gameBoard.getTile(rowB, col);
            boolean aHasPlant = a != null && a.plant != null;
            boolean bHasPlant = b != null && b.plant != null;
            if (aHasPlant != bHasPlant) return false;
        }
        return true;
    }

    /** True if at least one kill happened this level, and every single one came from `family`. */
    private boolean onlyKilledWithFamily(String family) {
        if (gameBoard.killContexts.isEmpty()) return false;
        for (String context : gameBoard.killContexts) {
            PlantType killer = parsePlantType(context);
            if (killer == null || !PlantSpawner.matchesFamily(killer, family)) {
                return false;
            }
        }
        return true;
    }

    /** True if nothing planted this level belongs to `family`. */
    private boolean neverPlantedFamily(String family) {
        for (PlantType planted : gameBoard.plantedTypes) {
            if (PlantSpawner.matchesFamily(planted, family)) return false;
        }
        return true;
    }

    /** Recovers the PlantType from a dispatched ZOMBIE_KILLED_BY_PLANT context (e.g. "PEASHOOTER").
     *  Returns null for non-plant contexts like "MOWER". */
    private PlantType parsePlantType(String context) {
        if (context == null) return null;
        try {
            return PlantType.valueOf(context.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
