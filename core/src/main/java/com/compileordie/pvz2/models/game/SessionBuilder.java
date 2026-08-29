package com.compileordie.pvz2.models.game;

import com.badlogic.gdx.math.MathUtils;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.PlantSpawner;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.TombType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.board.TileType;
import com.compileordie.pvz2.models.game.economy.EconomyType;
import com.compileordie.pvz2.models.game.judges.LossCondition;
import com.compileordie.pvz2.models.game.judges.WinCondition;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.Vase;
import com.compileordie.pvz2.models.game.waves.WaveType;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SessionBuilder {
    private static EconomyType getEconomyType(LevelID levelID) {
        if (Set.of(LevelID.CONVEYOR_BELT, LevelID.WALNUT_BOWLING).contains(levelID)) {
            return EconomyType.CONVEYOR_BELT;
        }
        if (levelID == LevelID.VASE_BREAKER) {
            return EconomyType.VASE_BREAKER;
        }
        if (Set.of(LevelID.NIGHT_OPS, LevelID.WALNUT_BOWLING, LevelID.BEGHOULED)
            .contains(levelID)) {
            return EconomyType.NIGHT;
        }
        if (levelID == LevelID.PLANT_WHAT_YOU_GET) {
            return EconomyType.PLANT_WHAT_YOU_GET;
        }
        if (levelID == LevelID.I_ZOMBIE) {
            return EconomyType.I_ZOMBIE;
        }
        return EconomyType.STANDARD;
    }

    private static WaveType getWaveType(LevelID levelID) {
        if (levelID == LevelID.ZOMBOTANY) {
            return WaveType.ZOMBOTANY;
        }
        if (Set.of(LevelID.VASE_BREAKER, LevelID.I_ZOMBIE).contains(levelID)) {
            return WaveType.NO_WAVES;
        }
        if (levelID.chapterType == ChapterType.ANCIENT_EGYPT) {
            return WaveType.ANCIENT_EGYPT;
        }
        if (levelID.chapterType == ChapterType.DARK_AGES) {
            return WaveType.DARK_AGES;
        }
        if (levelID.chapterType == ChapterType.BIG_WAVE_BEACH) {
            return WaveType.BIG_WAVE_BEACH;
        }
        if (levelID.chapterType == ChapterType.FROSTBITE_CAVES) {
            return WaveType.FROSTBITE_CAVE;
        }
        return WaveType.NORMAL;
    }

    private static WinCondition getWinCondition(LevelID levelID) {
        if (levelID == LevelID.TIMED_WAR) return WinCondition.TIMED_WAR;
        if (levelID == LevelID.VASE_BREAKER) return WinCondition.VASE_BREAKER;
        if (levelID == LevelID.I_ZOMBIE) return WinCondition.I_ZOMBIE;
        if (levelID == LevelID.BEGHOULED) return WinCondition.BEGHOULED;
        return WinCondition.STANDARD;
    }

    private static LossCondition getLossCondition(LevelID levelID) {
        if (levelID == LevelID.SAVE_OUR_SEEDS) return LossCondition.SAVE_OUR_SEEDS;
        if (levelID == LevelID.TIMED_WAR) return LossCondition.TIMED_WAR;
        if (levelID == LevelID.DEAD_LINE) return LossCondition.DEAD_LINE;
        if (levelID == LevelID.LOVE_YOUR_PLANTS) return LossCondition.LOVE_YOUR_PLANTS;
        if (levelID == LevelID.I_ZOMBIE) return LossCondition.I_ZOMBIE;
        return LossCondition.STANDARD;
    }

    private static void populateTiles(LevelID levelID, GameSession gameSession) {
        GameBoard gameBoard = gameSession.gameBoard;
        List<Tile> tiles = gameBoard.getAllTiles();
        if (levelID.chapterType == ChapterType.ANCIENT_EGYPT) {
            for (Tile tile : tiles) {
                tile.type = TileType.ANCIENT_EGYPT;
                if (tile.column >= 3 && new Random().nextInt(100) < 7) {
                    tile.obstacle = new Tomb(700,
                        tile.row,
                        tile.column,
                        (tile.column + 0.5f) * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X,
                        (tile.row + 0.5f) * Constants.Game.TILE_HEIGHT + Constants.Game.PADDING_Y);
                }
            }
        } else if (levelID.chapterType == ChapterType.FROSTBITE_CAVES) {
            for (Tile tile : tiles) {
                tile.type = TileType.FROSTBITE_CAVE;
                if (tile.row < Constants.Game.BOARD_ROWS - 1 && new Random().nextInt(100) < 3) {
                    tile.type = TileType.SLIPPERY_UP;
                }
                if (tile.row > 0 && new Random().nextInt(100) < 3) {
                    tile.type = TileType.SLIPPERY_DOWN;
                }
            }
        } else if (levelID.chapterType == ChapterType.BIG_WAVE_BEACH) {
            for (Tile tile : tiles) {
                tile.type = TileType.BIG_WAVE_BEACH;
                if (tile.column >= 3 && new Random().nextInt(100) < 5) tile.type = TileType.SHALLOW_BEACH;
            }
        } else if (levelID.chapterType == ChapterType.DARK_AGES) {
            for (Tile tile : tiles) {
                tile.type = TileType.DARK_AGES;
                Tomb tomb = new Tomb(700,
                    tile.row,
                    tile.column,
                    (tile.column + 0.5f) * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X,
                    (tile.row + 0.5f) * Constants.Game.TILE_HEIGHT + Constants.Game.PADDING_Y);
                int randomness = new Random().nextInt(100);
                if (randomness < 20)
                    tomb.type = TombType.SUN;
                else if (randomness < 40)
                    tomb.type = TombType.PLANT_FOOD;
                if (tile.column >= 3 && new Random().nextInt(100) < 10) {
                    tile.obstacle = tomb;
                }
            }
        } else {
            for (Tile tile : tiles) {
                tile.type = TileType.NORMAL;
            }
        }
    }

    private static void addObjects(LevelID levelID, GameSession gameSession) {
        GameBoard gameBoard = gameSession.gameBoard;
        List<Tile> tiles = gameBoard.getAllTiles();
        if (levelID == LevelID.SAVE_OUR_SEEDS) {
            for (Tile tile : tiles) {
                if (tile.column < 7 && new Random().nextInt(100) < 10) {
                    PlantType plantType = PlantType.values()[MathUtils.random(PlantType.values().length - 1)];
                    PlantSpawner.spawn(plantType,
                        (tile.column + 0.5f) * Constants.Game.TILE_WIDTH,
                        (tile.row + 0.5f) * Constants.Game.TILE_HEIGHT,
                        false,
                        true);
                }
            }
        } else if (levelID == LevelID.VASE_BREAKER) {
            for (Tile tile : tiles) {
                if (tile.column >= 3) {
                    int random = new Random().nextInt(100);
                    if (random < 10) {
                        gameBoard.vases.add(new Vase(gameBoard, tile.row, tile.column));
                    } else if (random < 55) {
                        gameBoard.vases.add(new Vase(gameBoard,
                            tile.row,
                            tile.column,
                            PlantType.values()[MathUtils.random(PlantType.values().length - 1)]));
                    } else {
                        gameBoard.vases.add(new Vase(gameBoard,
                            tile.row,
                            tile.column,
                            ZombieType.values()[MathUtils.random(ZombieType.values().length - 1)]));
                    }
                }
            }
        } else if (levelID == LevelID.I_ZOMBIE) {
            for (Tile tile : tiles) {
                if (tile.column < 6) {
                    PlantType plantType = PlantType.values()[MathUtils.random(PlantType.values().length - 1)];
                    PlantSpawner.spawn(plantType,
                        (tile.column + 0.5f) * Constants.Game.TILE_WIDTH,
                        (tile.row + 0.5f) * Constants.Game.TILE_HEIGHT,
                        false,
                        false);
                }
            }
        }
    }

    public static GameSession create(LevelID levelID, Map<PlantType, Boolean> selectionDeck) {
        GameSession gameSession = new GameSession(levelID,
            getEconomyType(levelID),
            getWaveType(levelID),
            getWinCondition(levelID),
            getLossCondition(levelID),
            selectionDeck);
        populateTiles(levelID, gameSession);
        addObjects(levelID, gameSession);
        return gameSession;
    }
}
