package com.compileordie.pvz2.views;

import com.badlogic.gdx.Screen;
import com.compileordie.pvz2.Main;
import com.compileordie.pvz2.views.game.GameScreen;
import com.compileordie.pvz2.views.screens.*;

public class ScreenManager {
    private static Main main;

    private ScreenManager() {
    }

    public static void init(Main main) {
        ScreenManager.main = main;
    }

    public static void setMenuScreen(ScreenType type) {
        Screen screen = switch (type) {
            case SIGNUP -> new SignupMenuScreen();
            case LOGIN -> new LoginMenuScreen();
            case MAIN -> new MainMenuScreen();
            case GAME -> new GameMenuScreen();
            case NETWORK -> new NetworkMenuScreen();
            case PROFILE -> new ProfileMenuScreen();
            case COLLECTION -> new CollectionMenuScreen();
            case GREENHOUSE -> new GreenhouseMenuScreen();
            case TRAVEL_LOG -> new TravelLogMenuScreen();
            case SHOP -> new ShopMenuScreen();
            case GAME_SESSION -> new GameScreen();
        };

        main.setScreen(screen);
    }
}
