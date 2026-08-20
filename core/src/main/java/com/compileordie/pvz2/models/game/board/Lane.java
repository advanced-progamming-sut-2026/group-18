package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.LawnMower;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Lane {
    public GameBoard gameBoard;
    public int row;
    public int tileCount;
    public ArrayList<Zombie> zombies;
    public ArrayList<Tile> tiles;
    public LawnMower lawnMower;
    public boolean isLost;
    // 👈 لیست قبرهای این لاین (علاوه بر tile.obstacle) تا هم منطق (مثلا شمردن
    // تعداد قبرهای یک لاین) و هم لایه‌ی رندر بتونن مستقیم و سریع بهشون دسترسی
    // داشته باشن - دقیقا هم‌الگو با zombies/lawnMower.
    public ArrayList<Tomb> tombs;

    public Lane(GameBoard gameBoard, int row, int tileCount) {
        this.gameBoard = gameBoard;
        this.row = row;
        this.zombies = new ArrayList<>();
        this.tiles = new ArrayList<>();
        this.tombs = new ArrayList<>();
        this.tileCount = tileCount;
        for (int i = 0; i < tileCount; i++) {
            tiles.add(new Tile(gameBoard, row, i, TileType.UNINITIALIZED, null, null));
        }
        this.isLost = false;
        this.lawnMower = new LawnMower(this);
    }

    public void tick(int ticks) {
        for (Tile tile : tiles) {
            tile.tick(ticks);
        }
        lawnMower.tick(ticks);

        for (int i = zombies.size() - 1; i >= 0; i--) {
            if (!zombies.get(i).isAlive()) zombies.remove(i);
        }

        // 👈 پاک‌سازی کامل قبرهای نابودشده: هم از منطق (این لیست + tile.obstacle)
        // هم به‌طور غیرمستقیم از گرافیک، چون GameScreen فقط همین لیست رو رندر
        // می‌کنه - وقتی از اینجا حذف بشه، فریم بعد دیگه رندر نمی‌شه.
        for (int i = tombs.size() - 1; i >= 0; i--) {
            Tomb tomb = tombs.get(i);
            if (tomb.isDestroyed()) {
                Tile tile = tiles.get(tomb.getCol());
                if (tile.obstacle == tomb) {
                    tile.obstacle = null;
                }
                tombs.remove(i);
            }
        }
    }

    public ArrayList<Plant> getAllPlants() {
        return tiles.stream()
            .filter(tile -> tile.plant != null)
            .map(tile -> tile.plant)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public float getLength() {
        return tileCount * Constants.Game.TILE_SIZE;
    }
}
