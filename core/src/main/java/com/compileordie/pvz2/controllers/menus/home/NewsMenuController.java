package com.compileordie.pvz2.controllers.menus.home;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.missions.News;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.StringJoiner;

public class NewsMenuController {
    private NewsMenuController() {
    }

    public static String exitMenu() {
        return AppController.changeMenu(Menu.MAIN);
    }

    public static String newsShowUnread() {
        Player player = AppModel.player;

        if (player.news == null || player.news.isEmpty()) {
            return "No news available.";
        }

        StringJoiner joiner = new StringJoiner(System.lineSeparator() + "---" + System.lineSeparator());
        boolean hasUnread = false;

        for (News newsItem : player.news) {
            if (!newsItem.isRead) {
                joiner.add(newsItem.toString());
                newsItem.isRead = true; // Mark as read according to documentation
                hasUnread = true;
            }
        }

        if (!hasUnread) {
            return "No unread news.";
        }

        new UserDatabase(player.username).save(player);

        return joiner.toString();
    }

    public static String newsShowAll() {
        Player player = AppModel.player;

        if (player.news == null || player.news.isEmpty()) {
            return "No news available.";
        }

        StringJoiner joiner = new StringJoiner(System.lineSeparator() + "---" + System.lineSeparator());
        for (News newsItem : player.news) {
            joiner.add(newsItem.toString());
        }

        return joiner.toString();
    }
}
