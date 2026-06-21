package com.compileordie.pvz2.views.helpers;

import com.compileordie.pvz2.views.menus.auth.LoginMenuView;
import com.compileordie.pvz2.views.menus.auth.ProfileMenuView;
import com.compileordie.pvz2.views.menus.auth.SignupMenuView;
import com.compileordie.pvz2.views.menus.game.GameMenuView;
import com.compileordie.pvz2.views.menus.game.GameSessionMenuView;
import com.compileordie.pvz2.views.menus.game.PlantSelectionMenuView;
import com.compileordie.pvz2.views.menus.home.MainMenuView;
import com.compileordie.pvz2.views.menus.home.NewsMenuView;
import com.compileordie.pvz2.views.menus.home.SettingsMenuView;
import com.compileordie.pvz2.views.menus.network.LeaderboardMenuView;
import com.compileordie.pvz2.views.menus.network.NetworkMenuView;
import com.compileordie.pvz2.views.menus.progression.CollectionMenuView;
import com.compileordie.pvz2.views.menus.progression.GreenhouseMenuView;
import com.compileordie.pvz2.views.menus.progression.ShopMenuView;
import com.compileordie.pvz2.views.menus.progression.TravelLogMenuView;

public enum Menu {
    SIGNUP(new SignupMenuView()),
    LOGIN(new LoginMenuView()),
    MAIN(new MainMenuView()),
    GAME(new GameMenuView()),
    SETTINGS(new SettingsMenuView()),
    NETWORK(new NetworkMenuView()),
    NEWS(new NewsMenuView()),
    PROFILE(new ProfileMenuView()),
    COLLECTION(new CollectionMenuView()),
    GREENHOUSE(new GreenhouseMenuView()),
    TRAVEL_LOG(new TravelLogMenuView()),
    LEADERBOARD(new LeaderboardMenuView()),
    SHOP(new ShopMenuView()),
    PLANT_SELECTION(new PlantSelectionMenuView()),
    GAME_SESSION(new GameSessionMenuView());

    private final MenuView menu;

    Menu(MenuView menu) {
        this.menu = menu;
    }

    public String getName() {
        return this.name().replace("_", " ").toLowerCase();
    }

    public static Menu getByName(String name) {
        for (Menu menu: Menu.values()) {
            if (menu.getName().equalsIgnoreCase(name)) {
                return menu;
            }
        }
        return null;
    }

    public String handleCommand(String command) {
        return this.menu.handleCommand(command);
    }
}
