package com.compileordie.pvz2.controllers.menus.home;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.missions.News;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;

import java.util.List;

public class NewsMenuController {
    private NewsMenuController() {
    }

    public static Result<List<News>> getAllNews() {
        Player player = AppModel.player;
        if (player.news == null || player.news.isEmpty()) {
            return Result.failure("No news available");
        }
        return Result.success(player.news);
    }

    public static Result<Void> markAllAsRead() {
        Player player = AppModel.player;
        boolean hasUnread = false;

        if (player.news != null) {
            for (News newsItem : player.news) {
                if (!newsItem.isRead) {
                    newsItem.isRead = true; // Mark as read according to documentation
                    hasUnread = true;
                }
            }
        }

        if (hasUnread) {
            new UserDatabase(player.username).save(player);
        }

        return Result.success();
    }
}
