package com.compileordie.pvz2.views;

import com.badlogic.gdx.Screen;
import com.compileordie.pvz2.Main;
import com.compileordie.pvz2.views.game.GameScreen;
import com.compileordie.pvz2.views.screens.GameMenuScreen;
import com.compileordie.pvz2.views.screens.MainMenuScreen;
import com.compileordie.pvz2.views.screens.PlantSelectionScreen;
import com.compileordie.pvz2.views.screens.SignupMenuScreen;

public class ScreenManager {
    private static Main main;
    @SuppressWarnings("GDXJavaStaticResource")
    private static GameScreen currentGameScreen;

    private ScreenManager() {
    }

    public static void init(Main main) {
        ScreenManager.main = main;
    }

    public static void setMenuScreen(ScreenType type) {
        Screen screen = switch (type) {
            case SIGNUP -> new SignupMenuScreen();
            case LOGIN -> null;
            case MAIN -> new MainMenuScreen();
            case GAME -> new GameMenuScreen();
            case SETTINGS -> null;
            case NETWORK -> null;
            case NEWS -> null;
            case PROFILE -> null;
            case COLLECTION -> null;
            case GREENHOUSE -> null;
            case TRAVEL_LOG -> null;
            case LEADERBOARD -> null;
            case SHOP -> null;
            case PLANT_SELECTION -> new PlantSelectionScreen();
            case GAME_SESSION -> new GameScreen();
        };

        main.setScreen(screen);
    }
}
