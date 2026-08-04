package com.compileordie.pvz2.views.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Command {
    // Common
    MENU_ENTER("menu\\s+enter\\s+(?<name>.+)"),
    MENU_SHOW_CURRENT("menu\\s+show\\s+current"),
    MENU_EXIT("menu\\s+exit"),
    // Signup Menu
    REGISTER("register\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)\\s+(?<passwordConfirm>\\S+)\\s+-n\\s+" +
        "\"(?<nickname>[^\"]+)\"\\s+-e\\s+(?<email>\\S+)\\s+-g\\s+(?<gender>\\S+)"),
    PICK_QUESTION("pick\\s+question\\s+-q\\s+(?<number>\\S+)\\s+-a\\s+(?<answer>\\S+)\\s+-c\\s+(?<answerConfirm>\\S+)"),
    // Login Menu
    LOGIN("login\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)(?:\\s+(?<stay>-stay-logged-in))?"),
    FORGET_PASSWORD("forget\\s+password\\s+-u\\s+(?<username>\\S+)\\s+-e\\s+(?<email>\\S+)"),
    ANSWER("answer\\s+-a\\s+(?<answer>\\S+)"),
    // Main Menu
    LOGOUT("menu\\s+logout"),
    // Game Menu
    ENTER_CHAPTER("menu\\s+enter\\s+chapter\\s+-c\\s+(?<name>.+)"),
    ENTER_LEVEL("menu\\s+enter\\s+level\\s+-c\\s+(?<name>.+)"),
    MENU_GREENHOUSE("menu\\s+greenhouse"),
    MENU_TRAVEL_LOG("menu\\s+travel-log"),
    MENU_LEADERBOARD("menu\\s+leaderboard"),
    COIN_WALLET("menu\\s+coin-wallet"),
    GEM_WALLET("menu\\s+gem-wallet"),
    CHEAT_ADD("menu\\s+cheat\\s+add\\s+(?<count>\\S+)\\s+(?<type>\\S+)"),
    // Settings Menu
    CHANGE_DIFFICULTY("menu\\s+settings\\s+change-difficulty\\s+-l\\s+(?<level>\\S+)"),
    // New Menu
    NEWS_SHOW_UNREAD("menu\\s+news\\s+show-unread"),
    NEWS_SHOW_ALL("menu\\s+news\\s+show-all"),
    // Profile Menu
    CHANGE_USERNAME("menu\\s+profile\\s+change-username\\s+-u\\s+(?<username>\\S+)"),
    CHANGE_NICKNAME("menu\\s+profile\\s+change-nickname\\s+-u\\s+(?<nickname>\\S+)"),
    CHANGE_EMAIL("menu\\s+profile\\s+change-email\\s+-e\\s+(?<email>\\S+)"),
    CHANGE_PASSWORD("menu\\s+profile\\s+change-password\\s+-p\\s+(?<newPassword>\\S+)\\s+-o\\s+(?<oldPassword>\\S+)"),
    SHOW_INFO("menu\\s+profile\\s+show-info"),
    // Collection Menu
    COLLECTION_SHOW_PLANTS("menu\\s+collection\\s+show-plants"),
    COLLECTION_SHOW_ALL_PLANTS("menu\\s+collection\\s+show-all-plants"),
    COLLECTION_SHOW_ZOMBIES("menu\\s+collection\\s+show-zombies"),
    COLLECTION_SHOW_ALL_ZOMBIES("menu\\s+collection\\s+show-all-zombies"),
    COLLECTION_SHOW_PLANT("menu\\s+collection\\s+show-plant\\s+-p\\s+(?<name>.+)"),
    COLLECTION_SHOW_ZOMBIE("menu\\s+collection\\s+show-zombie\\s+-z\\s+(?<name>.+)"),
    COLLECTION_UPGRADE_PLANT("menu\\s+collection\\s+upgrade-plant\\s+-p\\s+(?<name>.+)"),
    COLLECTION_PURCHASE_PLANT("menu\\s+collection\\s+purchase-plant\\s+-p\\s+(?<name>.+)"),
    // Greenhouse Menu
    SHOW_GREENHOUSE("show\\s+greenhouse"),
    GREENHOUSE_PLANT_POT("plant\\s+pot\\s+at\\s+\\((?<x>\\S+),\\s+(?<y>\\S+)\\)"),
    GREENHOUSE_COLLECT("collect\\s+\\((?<x>\\S+),\\s+(?<y>\\S+)\\)"),
    GREENHOUSE_GROW("grow\\s+\\((?<x>\\S+),\\s+(?<y>\\S+)\\)"),
    GREENHOUSE_ENTER_SHOP("enter\\s+shop"),
    // Travel Log Menu
    TRAVEL_LOG_PAGE("travel\\s+log\\s+page\\s+(?<name>.+)"),
    TRAVEL_LOG_CLAIM("travel\\s+log\\s+claim\\s+(?<id>.+)"),
    // Leaderboard Menu
    SHOW_LEADERBOARD("show\\s+leaderboard"),
    LEADERBOARD_SORT("sort\\s+by\\s+(?<parameter>.+)"),
    // Shop Menu
    SHOP_LIST("shop\\s+list"),
    SHOP_DAILY("shop\\s+daily"),
    SHOP_BUY("shop\\s+buy\\s+-i\\s+(?<id>\\S+)\\s+-n\\s+(?<count>\\S+)(?:\\s+-t\\s+(?<type>\\S+))?"),
    // Plant Selection Menu
    SELECTION_SHOW_ALL_PLANTS("show\\s+all\\s+plants"),
    SELECTION_SHOW_AVAILABLE_PLANTS("show\\s+available\\s+plants"),
    SELECTION_ADD_PLANT("add\\s+plant\\s+-t\\s+(?<type>.+)"),
    SELECTION_REMOVE_PLANT("remove\\s+plant\\s+-t\\s+(?<type>.+)"),
    SELECTION_BOOST_PLANT("boost\\s+plant\\s+-t\\s+(?<type>.+)"),
    SELECTION_START_GAME("start\\s+game"),
    SELECTION_CANCEL_GAME("cancel\\s+game"),
    // Game Session Menu
    ADVANCE_TIME("advance time -t (?<count>\\S+) ticks"),
    COLLECT_SUN("collect sun -l \\((?<x>\\S+),\\s+(?<y>\\S+)\\)"),
    SHOW_SUN_AMOUNT("show sun amount"),
    CHEAT_ADD_SUN("cheat add -n (?<count>\\S+) suns"),
    RELEASE_THE_NUKE("release the nuke"),
    PLANT_PLANT("plant plant -t (?<type>.+) -l \\((?<x>\\S+),\\s+(?<y>\\S+)\\)"),
    CHEAT_REMOVE_COOLDOWN("cheat remove-cooldown"),
    PLUCK_PLANT("pluck plant -l \\((?<x>\\S+),\\s+(?<y>\\S+)\\)"),
    FEED_PLANT("feed plant -l \\((?<x>\\S+),\\s+(?<y>\\S+)\\)"),
    CHEAT_ADD_PLANT_FOOD("cheat add-plant-food"),
    SHOW_MAP("show map"),
    SHOW_PLANTS_STATUS("show plants status"),
    SHOW_TILES_STATUS("show tile status -l \\((?<x>\\S+),\\s+(?<y>\\S+)\\)"),
    START_ZOMBIE_WAVES("start zombie waves"),
    ZOMBIE_INFO("zombies info"),
    SPAWN_ZOMBIE("cheat spawn-zombie -t (?<type>\\S+) -l \\((?<x>\\S+),\\s+(?<y>\\S+)\\)"),
    BREAK_VASE("break vase -l \\((?<x>\\S+),\\s+(?<y>\\S+)\\)"),
    QUIT_GAME("quit\\s+game");

    private final String regex;
    private final Pattern pattern;

    Command(String regex) {
        this.regex = "^" + regex + "$";
        this.pattern = Pattern.compile(this.regex);
    }

    public boolean matches(String command) {
        return command.matches(regex);
    }

    public String getGroup(String input, String groupName) {
        Matcher matcher = this.pattern.matcher(input);
        if (matcher.find()) {
            if (matcher.group(groupName) != null) {
                return matcher.group(groupName).trim();
            } else {
                return null;
            }
        }
        return null;
    }
}
