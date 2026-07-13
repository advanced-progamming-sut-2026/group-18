package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.models.entities.plants.variants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.DodoRiderZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.SnorkelZombie;

public enum TileType {
    UNINITIALIZED(false) {
        @Override
        public void tick(int ticks, GameBoard gameBoard) {
        }
    },
    ANCIENT_EGYPT(true) {
        @Override
        public void tick(int ticks, GameBoard gameBoard) {
        }
    },
    FROSTBITE_CAVE(true) {
        @Override
        public void tick(int ticks, GameBoard gameBoard) {
        }
    },
    SLIPPERY_UP(false) {
        @Override
        public void tick(int ticks, GameBoard gameBoard) {
            handleSlipperyMovement(gameBoard, -1);
        }
    },
    SLIPPERY_DOWN(false) {
        @Override
        public void tick(int ticks, GameBoard gameBoard) {
            handleSlipperyMovement(gameBoard, 1);
        }
    },
    BIG_WAVE_BEACH(true) {
        @Override
        public void tick(int ticks, GameBoard gameBoard) {
        }
    },
    DEEP_BEACH(false) {
        @Override
        public void tick(int ticks, GameBoard gameBoard) {
            handleOceanTileLogic(gameBoard);
        }
    },
    SHALLOW_BEACH(true) {
        @Override
        public void tick(int ticks, GameBoard gameBoard) {
            // Shallow beaches behave like regular water when flooded
            handleOceanTileLogic(gameBoard);
            // NOTE: Zombie spawning pools check this type at the start of a wave
        }
    },
    DARK_AGES(true) {
        @Override
        public void tick(int ticks, GameBoard gameBoard) {
        }
    },
    NECROMANCY(true) {
        @Override
        public void tick(int ticks, GameBoard gameBoard) {
        }
    },
    SAVE_SEED(true) {
        @Override
        public void tick(int ticks, GameBoard gameBoard) {
        }
    };

    public final boolean isPlantable;

    TileType(boolean isPlantable) {
        this.isPlantable = isPlantable;
    }

    public abstract void tick(int ticks, GameBoard gameBoard);

    /**
     * Helper to process slippery tile shifts for moving zombies.
     *
     * @param rowOffset -1 for UP, +1 for DOWN
     */
    protected void handleSlipperyMovement(GameBoard gameBoard, int rowOffset) {
        for (int r = 0; r < gameBoard.totalRows; r++) {
            Lane lane = gameBoard.getLane(r);

            for (int i = lane.zombies.size() - 1; i >= 0; i--) {
                Zombie zombie = lane.zombies.get(i);

                // Convert zombie decimal X position to grid coordinates (assuming columns are width 1.0)
                int currentTileX = zombie.getCurrentTileCoordinates()[0];

                // If the zombie walks onto a slippery tile in this row
                if (gameBoard.getLane(r).tiles.stream().anyMatch(t -> t.index == currentTileX && t.type == this)) {

                    // Dodo Rider skips slippery tiles entirely
                    if (zombie instanceof DodoRiderZombie) {
                        continue;
                    }

                    int targetRow = r + rowOffset;
                    if (targetRow >= 0 && targetRow < gameBoard.totalRows) {
                        // NOTE: Might as well use zombie manager here.
                        lane.zombies.remove(i);
                        zombie.setCurrentRow(targetRow);
                        gameBoard.getLane(targetRow).zombies.add(zombie);
                    }
                }
            }
        }
    }

    /**
     * Enforces active water rules for Big Wave Beach tiles.
     */
    protected void handleOceanTileLogic(GameBoard gameBoard) {
        for (int r = 0; r < gameBoard.totalRows; r++) {
            Lane lane = gameBoard.getLane(r);

            // 1. Enforce water-based plant drowning rules
            for (Tile tile : lane.tiles) {
                Plant plant = tile.plant;
                if (tile.type == this && plant != null) {
                    // Check if the plant requires a Lily Pad or isn't inherently aquatic
                    // (Assuming there's an isAquatic() or checking mechanism to Plant variants)
                    /*if (plant.type != PlantType.LILY_PAD && plant.type != PlantType.TANGLE_KELP) OR
                    if (!plant.isAquatic) {
                        plant.die();
                        tile.plant = null;
                        AppModel.addAfterPrompt("Plant at (" + tile.index + ", " + r + ") drowned in the ocean water.");
                    }*/
                    // TODO: Concrete implementation of the drowning logic here.
                }
            }

            // 2. Manage Snorkel Zombie submersion states
            for (Zombie zombie : lane.zombies) {
                int currentTileX = zombie.getCurrentTileCoordinates()[0];
                if (lane.tiles.stream().anyMatch(t -> t.index == currentTileX && t.type == this)) {
                    if (zombie instanceof SnorkelZombie snorkel) {
                        if (snorkel.isEating()) {
                            snorkel.surface(); // Forces it up so regular shooters can hit it
                        } else {
                            snorkel.dive(); // Submerges it back underwater, gaining shooter immunity
                        }
                    }
                }
            }
        }
    }
}
