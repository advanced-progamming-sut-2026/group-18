package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.SessionBuilder;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;

public class MainMenuScreen extends MenuScreen {

    @Override
    public void showCore() {
        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        Table mainOptions = new Table();
        mainOptions.center();
        stack.add(mainOptions);

        // Project Title
        Label title = new Label("Compile or Die!", skin, "big_outline");
        mainOptions.add(title).padBottom(50).row();

        TextButton playBtn = new TextButton("PLAY", skin, "green");
        mainOptions.add(playBtn).size(200, 70).padBottom(20).row();

        playBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // 1. Assign a default level to bypass the Game Menu
                AppModel.currentChapter = ChapterType.ANCIENT_EGYPT;
                AppModel.currentLevel = LevelID.STANDARD_ANCIENT_EGYPT;

                // 2. Add all unlocked plants to bypass the Plant Selection Menu
                AppModel.selectionDeck.clear();
                if (AppModel.player.unlockedPlants != null) {
                    for (PlantType type : AppModel.player.unlockedPlants) {
                        AppModel.selectionDeck.put(type, false);
                    }
                }

                // 3. Initialize the game session directly
                AppModel.gameSession = SessionBuilder.create(AppModel.currentLevel, AppModel.selectionDeck);

                // 4. Transition straight to the Game Session screen
                ScreenManager.setMenuScreen(ScreenType.GAME_SESSION);
                System.out.println("Bypassed menus. Entering Game Session directly.");
            }
        });

        Table peripheralNav = new Table();
        peripheralNav.bottom().pad(20);
        stack.add(peripheralNav);

        TextButton settingsBtn = new TextButton("Settings", skin, "default");
        TextButton profileBtn = new TextButton("Profile", skin, "default");
        TextButton newsBtn = new TextButton("News", skin, "default");
        TextButton logoutBtn = new TextButton("Logout", skin, "brown");

        peripheralNav.add(settingsBtn).width(120).padRight(10);
        peripheralNav.add(profileBtn).width(120).padRight(10);
        peripheralNav.add(newsBtn).width(120).padRight(10);
        peripheralNav.add(logoutBtn).width(120);

        logoutBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                throw new IllegalStateException("Why did you have to touch me?");
                // MainMenuController.logoutUser();
            }
        });
    }
}
