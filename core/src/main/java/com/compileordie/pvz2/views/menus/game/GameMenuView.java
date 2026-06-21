package com.compileordie.pvz2.views.menus.game;

import com.compileordie.pvz2.controllers.menus.game.GameMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class GameMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_ENTER.matches(command)) {
            String name = Command.MENU_ENTER.getGroup(command, "name");
            return GameMenuController.enterMenu(name);
        }
        if (Command.MENU_EXIT.matches(command)) {
            return GameMenuController.exitMenu();
        }
        if (Command.ENTER_CHAPTER.matches(command)) {
            String name = Command.ENTER_CHAPTER.getGroup(command, "name");
            return GameMenuController.enterChapter(name);
        }
        if (Command.MENU_GREENHOUSE.matches(command)) {
            return GameMenuController.enterGreenhouse();
        }
        if (Command.MENU_TRAVEL_LOG.matches(command)) {
            return GameMenuController.enterTravelLog();
        }
        if (Command.MENU_LEADERBOARD.matches(command)) {
            return GameMenuController.enterLeaderboard();
        }
        if (Command.COIN_WALLET.matches(command)) {
            return GameMenuController.showCoinWallet();
        }
        if (Command.GEM_WALLET.matches(command)) {
            return GameMenuController.showGemWallet();
        }
        if (Command.CHEAT_ADD.matches(command)) {
            String count = Command.CHEAT_ADD.getGroup(command, "count");
            String type = Command.CHEAT_ADD.getGroup(command, "type");
            return GameMenuController.cheatAdd(count, type);
        }
        return null;
    }
}
