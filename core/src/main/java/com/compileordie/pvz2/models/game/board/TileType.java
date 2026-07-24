package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.DodoRiderZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.SnorkelZombie;

public enum TileType {
    UNINITIALIZED(false) {
        @Override
        public void tick(int ticks, Tile self, GameBoard gameBoard) {
        }
    },
    ANCIENT_EGYPT(true) {
        @Override
        public void tick(int ticks, Tile self, GameBoard gameBoard) {
        }
    },
    FROSTBITE_CAVE(true) {
        @Override
        public void tick(int ticks, Tile self, GameBoard gameBoard) {
        }
    },
    SLIPPERY_UP(false) {
        @Override
        public void tick(int ticks, Tile self, GameBoard gameBoard) {
            handleSlipperyMovement(self, gameBoard, -1);
        }
    },
    SLIPPERY_DOWN(false) {
        @Override
        public void tick(int ticks, Tile self, GameBoard gameBoard) {
            handleSlipperyMovement(self, gameBoard, 1);
        }
    },
    BIG_WAVE_BEACH(true) {
        @Override
        public void tick(int ticks, Tile self, GameBoard gameBoard) {
        }
    },
    DEEP_BEACH(false) {
        @Override
        public void tick(int ticks, Tile self, GameBoard gameBoard) {
            handleOceanTileLogic(self, gameBoard);
        }
    },
    SHALLOW_BEACH(true) {
        @Override
        public void tick(int ticks, Tile self, GameBoard gameBoard) {
            // Shallow beaches behave like regular water when flooded
            if (self.isUnderWater()) handleOceanTileLogic(self, gameBoard);
        }
    },
    DARK_AGES(true) {
        @Override
        public void tick(int ticks, Tile self, GameBoard gameBoard) {
        }
    },
    NECROMANCY(true) {
        @Override
        public void tick(int ticks, Tile self, GameBoard gameBoard) {
        }
    },
    SAVE_SEED(true) {
        @Override
        public void tick(int ticks, Tile self, GameBoard gameBoard) {
        }
    };

    public final boolean isPlantable;

    TileType(boolean isPlantable) {
        this.isPlantable = isPlantable;
    }

    public abstract void tick(int ticks, Tile self, GameBoard gameBoard);

    /**
     * Helper to process slippery tile shifts for moving zombies.
     *
     * @param rowOffset -1 for UP, +1 for DOWN
     */
    protected void handleSlipperyMovement(Tile self, GameBoard gameBoard, int rowOffset) {
        // Directly pull row from the tile parameter
        int r = self.row;
        Lane lane = gameBoard.getLane(r);

        // Safely iterate backwards through the row's zombies
        for (int i = lane.zombies.size() - 1; i >= 0; i--) {
            Zombie zombie = lane.zombies.get(i);

            // Convert zombie decimal X position to grid coordinates (assuming columns are width 1.0)
            int currentTileX = zombie.getTileColumn();

            // If the zombie walks onto THIS specific tile instance's column
            if (self.column == currentTileX) {

                // Dodo Rider skips slippery tiles entirely
                if (zombie instanceof DodoRiderZombie) {
                    continue;
                }

                int targetRow = r + rowOffset;
                if (targetRow >= 0 && targetRow < gameBoard.totalRows) {
                    lane.zombies.remove(i);
                    zombie.setCurrentRow(targetRow);
                    gameBoard.getLane(targetRow).zombies.add(zombie);
                }
            }
        }
    }

    /**
     * Enforces active water rules for Big Wave Beach tiles.
     */
    protected void handleOceanTileLogic(Tile self, GameBoard gameBoard) {
        Lane lane = gameBoard.getLane(self.row);

        // 1. Enforce water-based plant drowning rules for THIS tile
        Plant plant = self.plant;
        if (plant != null) {
            // Check if the plant requires a Lily Pad or isn't inherently aquatic
            // (Assuming there's an isAquatic() or checking mechanism to Plant variants)
            /*if (plant.type != PlantType.LILY_PAD && plant.type != PlantType.TANGLE_KELP) OR
            if (!plant.isAquatic) {
                plant.die();
                self.plant = null;
                AppModel.addAfterPrompt("Plant at (" + self.column + ", " + self.row + ") drowned in the ocean water.");
            }*/
            // TODO: Concrete implementation of the drowning logic here.
        }
    }
}
