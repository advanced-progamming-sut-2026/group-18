package com.compileordie.pvz2.views.menus.game;

import com.compileordie.pvz2.controllers.menus.game.GameSessionMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class GameSessionMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.ADVANCE_TIME.matches(command)) {
            String count = Command.ADVANCE_TIME.getGroup(command, "count");
            return GameSessionMenuController.advanceTime(count);
        }
        if (Command.COLLECT_SUN.matches(command)) {
            String x = Command.COLLECT_SUN.getGroup(command, "x");
            String y = Command.COLLECT_SUN.getGroup(command, "y");
            return GameSessionMenuController.collectSun(x, y);
        }
        if (Command.SHOW_SUN_AMOUNT.matches(command)) {
            return GameSessionMenuController.showSunAmount();
        }
        if (Command.CHEAT_ADD_SUN.matches(command)) {
            String count = Command.CHEAT_ADD_SUN.getGroup(command, "count");
            return GameSessionMenuController.cheatAddSun(count);
        }
        if (Command.RELEASE_THE_NUKE.matches(command)) {
            return GameSessionMenuController.releaseTheNuke();
        }
        if (Command.PLANT_PLANT.matches(command)) {
            String type = Command.PLANT_PLANT.getGroup(command, "type");
            String x = Command.PLANT_PLANT.getGroup(command, "x");
            String y = Command.PLANT_PLANT.getGroup(command, "y");
            return GameSessionMenuController.plantPlant(type, x, y);
        }
        if (Command.CHEAT_REMOVE_COOLDOWN.matches(command)) {
            return GameSessionMenuController.cheatRemoveCooldown();
        }
        if (Command.PLUCK_PLANT.matches(command)) {
            String x = Command.PLUCK_PLANT.getGroup(command, "x");
            String y = Command.PLUCK_PLANT.getGroup(command, "y");
            return GameSessionMenuController.pluckPlant(x, y);
        }
        if (Command.FEED_PLANT.matches(command)) {
            String x = Command.FEED_PLANT.getGroup(command, "x");
            String y = Command.FEED_PLANT.getGroup(command, "y");
            return GameSessionMenuController.feedPlant(x, y);
        }
        if (Command.CHEAT_ADD_PLANT_FOOD.matches(command)) {
            return GameSessionMenuController.cheatAddPlantFood();
        }
        if (Command.SHOW_MAP.matches(command)) {
            return GameSessionMenuController.showMap();
        }
        if (Command.SHOW_PLANTS_STATUS.matches(command)) {
            return GameSessionMenuController.showPlantsStatus();
        }
        if (Command.SHOW_TILES_STATUS.matches(command)) {
            String x = Command.SHOW_TILES_STATUS.getGroup(command, "x");
            String y = Command.SHOW_TILES_STATUS.getGroup(command, "y");
            return GameSessionMenuController.showTilesStatus(x, y);
        }
        if (Command.START_ZOMBIE_WAVES.matches(command)) {
            return GameSessionMenuController.startZombieWaves();
        }
        if (Command.ZOMBIE_INFO.matches(command)) {
            return GameSessionMenuController.zombieInfo();
        }
        if (Command.SPAWN_ZOMBIE.matches(command)) {
            String type = Command.SPAWN_ZOMBIE.getGroup(command, "type");
            String x = Command.SPAWN_ZOMBIE.getGroup(command, "x");
            String y = Command.SPAWN_ZOMBIE.getGroup(command, "y");
            return GameSessionMenuController.spawnZombie(type, x, y);
        }
        if (Command.QUIT_GAME.matches(command)) {
            return GameSessionMenuController.quitGame();
        }
        return null;
    }
}
