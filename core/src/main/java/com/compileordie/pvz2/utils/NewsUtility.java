package com.compileordie.pvz2.utils;

import com.compileordie.pvz2.models.missions.News;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;

import java.util.ArrayList;

public class NewsUtility {
    private NewsUtility() {
        // Prevent instantiation
    }

    /**
     * Adds a new news item to the player's profile and saves it to the JSON file.
     */
    public static void addNews(String username, News newsItem) {
        Player player = new UserDatabase(username).load();
        if (player.news == null) {
            player.news = new ArrayList<>();
        }
        player.news.add(newsItem);
        save(player);
    }

    /**
     * Removes a specific news item from the player's profile and updates the JSON file.
     */
    public static void removeNews(String username, String newsTitle) {
        Player player = new UserDatabase(username).load();
        if (player.news != null && !player.news.isEmpty()) {
            boolean removed = false;
            for (News news : player.news) {
                if (news.title.equals(newsTitle)) {
                    player.news.remove(news);
                    removed = true;
                    break;
                }
            }
            if (removed) {
                save(player);
            }
        }
    }

    /**
     * Clears all news items from the player's profile and updates the JSON file.
     */
    public static void clearAllNews(String username) {
        Player player = new UserDatabase(username).load();
        if (player.news != null && !player.news.isEmpty()) {
            player.news.clear();
            save(player);
        }
    }

    /**
     * Centralized save call utilizing the existing UserDatabase abstraction.
     */
    private static void save(Player player) {
        new UserDatabase(player.username).save(player);
    }
}
