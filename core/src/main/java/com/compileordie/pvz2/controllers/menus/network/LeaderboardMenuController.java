package com.compileordie.pvz2.controllers.menus.network;

import com.compileordie.pvz2.models.repositories.databases.AuthDatabase;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.user.authentication.UserRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderboardMenuController {
    // We cache the current sort parameter to maintain state while viewing
    private static String currentSortParameter = "score";
    private static boolean isAscending = false;

    private LeaderboardMenuController() {
    }

    private static List<Player> loadAllPlayers() {
        List<Player> players = new ArrayList<>();
        AuthDatabase authDb = new AuthDatabase();

        for (UserRegistry registry : authDb.load()) {
            Player player = new UserDatabase(registry.getUsername()).load();
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    public static String showLeaderboard() {
        List<Player> players = loadAllPlayers();

        if (players.isEmpty()) {
            return "The leaderboard is currently empty.";
        }

        // Apply sorting based on the currently selected parameter
        Comparator<Player> comparator = getPlayerComparator();

        players.sort(comparator);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-10s | %-12s | %-16s | %-15s%n",
            "Username", "Score", "Minigames", "Daily Quests", "Non-Daily Quests"));
        sb.repeat("-", 81).append(System.lineSeparator());

        for (Player p : players) {
            sb.append(String.format("%-15s | %-10d | %-12d | %-16d | %-15d%n",
                p.username,
                p.bestScore,
                p.completedMiniGames,
                p.completedTotalDailyQuests,
                p.completedTotalNonDailyQuests));
        }

        return sb.toString().trim();
    }

    private static Comparator<Player> getPlayerComparator() {
        Comparator<Player> comparator = switch (currentSortParameter.toLowerCase()) {
            case "minigames" -> Comparator.comparingInt(p -> p.completedMiniGames);
            case "daily-quests" -> Comparator.comparingInt(p -> p.completedTotalDailyQuests);
            case "non-daily-quests" -> Comparator.comparingInt(p -> p.completedTotalNonDailyQuests);
            case "score" -> Comparator.comparingInt(p -> p.bestScore);
            default -> Comparator.comparingInt(p -> p.bestScore); // Fallback
        };

        if (!isAscending) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    public static String sortLeaderboard(String parameter) {
        // Toggle ascending/descending if the same parameter is chosen twice in a row
        if (currentSortParameter.equalsIgnoreCase(parameter)) {
            isAscending = !isAscending;
        } else {
            currentSortParameter = parameter;
            isAscending = false; // Default to highest-first for new parameters
        }

        return "Leaderboard sorted by '" + parameter + "' in " + (isAscending ? "ascending" : "descending")
            + " order.\n" + showLeaderboard();
    }
}
