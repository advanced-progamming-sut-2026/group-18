package com.compileordie.pvz2.views.menus.home;

import com.compileordie.pvz2.controllers.menus.home.NewsMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class NewsMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_ENTER.matches(command)) {
            return NewsMenuController.exitMenu();
        }
        if (Command.NEWS_SHOW_UNREAD.matches(command)) {
            return NewsMenuController.newsShowUnread();
        }
        if (Command.NEWS_SHOW_ALL.matches(command)) {
            return NewsMenuController.newsShowAll();
        }
        return null;
    }
}
