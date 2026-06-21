package com.compileordie.pvz2.views.menus.network;

import com.compileordie.pvz2.controllers.menus.network.LeaderboardMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class LeaderboardMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_EXIT.matches(command)) {
            return LeaderboardMenuController.exitMenu();
        }
        if (Command.SHOW_LEADERBOARD.matches(command)) {
            return LeaderboardMenuController.showLeaderboard();
        }
        if (Command.LEADERBOARD_SORT.matches(command)) {
            String parameter = Command.LEADERBOARD_SORT.getGroup(command, "parameter");
            return LeaderboardMenuController.sortLeaderboard(parameter);
        }
        return null;
    }
}
