package com.compileordie.pvz2.models.entities.zombies.services.manager;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.game.economy.Sun; // خورشیدهای رها شده روی زمین
import com.compileordie.pvz2.models.entities.LawnMower; // ماشین‌های چمن‌زنی

import java.util.List;
import java.util.Queue;

public interface ZombieTickContext {
    double getDelta();
    GameBoard getGameMap();
    List<Zombie> getActiveZombies();
    List<Plant> getActivePlants();
    List<Sun> getSunsOnGround();
}
