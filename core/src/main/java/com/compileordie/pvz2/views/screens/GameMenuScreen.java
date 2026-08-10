package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.controllers.menus.game.GameMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import pvz.skin.BorderedTable;

public class GameMenuScreen extends MenuScreen {

    @Override
    public void showCore() {
        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(20);
        stage.addActor(root);

        // Top HUD: Wallets and Quick Links
        Table hud = new Table();
        hud.add(new Label("Coins: " + AppModel.player.coins, skin, "medium")).padRight(20);
        hud.add(new Label("Gems: " + AppModel.player.diamonds, skin, "medium")).padRight(40);

        TextButton collectionBtn = new TextButton("Collection", skin, "default");
        hud.add(collectionBtn).width(120).padRight(10);
        root.add(hud).expandX().right().row();

        // Level Selection Panel
        BorderedTable panel = new BorderedTable();
        root.add(panel).expand().center();

        panel.add(new Label("Select Level", skin, "big")).colspan(2).padBottom(30).row();

        // Example: Hardcoding Level 1 of Ancient Egypt for prototyping
        TextButton lvl1Btn = new TextButton("Ancient Egypt - Lvl 1", skin, "green_small");
        panel.add(lvl1Btn).size(250, 60).padBottom(15).row();

        lvl1Btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameMenuController.enterChapter(ChapterType.ANCIENT_EGYPT.name());
                GameMenuController.enterLevel(LevelID.STANDARD_ANCIENT_EGYPT.name());
            }
        });

        TextButton backBtn = new TextButton("Back", skin, "brown");
        panel.add(backBtn).size(150, 50).padTop(20);

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameMenuController.exitMenu();
            }
        });
    }
}
