package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.customelements.ChangePasswordModal;
import com.compileordie.pvz2.views.customelements.EditProfileModal;
import pvz.skin.BorderedTable;

public class ProfileMenuScreen extends MenuScreen {
    private Player player;
    private Stack stack;
    private BorderedTable profileBoard;
    private Table statsTable;
    private Table actionsTable;
    private Table hudTable;
    private TextButton editBtn;
    private TextButton passBtn;
    private ImageButton backBtn;
    private Label title;

    @Override
    public void showCore() {
        player = AppModel.player;

        initComponents();
        buildLayout();
        setupListeners();

        // Populate the stats table after initialization
        refreshStatsUI();
    }

    private void initComponents() {
        stack = new Stack();
        profileBoard = new BorderedTable();
        statsTable = new Table();
        actionsTable = new Table();
        hudTable = new Table();

        title = new Label("Player Profile", skin, "big_outline");

        editBtn = new TextButton("Edit Profile", skin, "default");
        passBtn = new TextButton("Change Password", skin, "brown");
        backBtn = new ImageButton(skin, "generic_close_circle");
    }

    private void buildLayout() {
        // --- BASE STACK ---
        stack.setFillParent(true);
        stage.addActor(stack);

        // --- CENTER CONTENT ---
        stack.add(profileBoard);
        profileBoard.add(title).colspan(2).padBottom(50).center().row();

        actionsTable.add(editBtn).size(200, 50).padBottom(15).row();
        actionsTable.add(passBtn).size(200, 50).row();

        profileBoard.add(statsTable).padRight(120).top();
        profileBoard.add(actionsTable).top().padTop(40);

        // --- TOP HUD ---
        hudTable.setFillParent(true);
        stack.add(hudTable);

        hudTable.top().left();
        hudTable.add(backBtn).size(64, 64).pad(25);
    }

    private void setupListeners() {
        editBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Pass a callback to refresh the stats UI automatically when saved
                EditProfileModal modal = new EditProfileModal(skin, player, () -> refreshStatsUI());
                modal.show(stage);
            }
        });

        passBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ChangePasswordModal modal = new ChangePasswordModal(skin);
                modal.show(stage);
            }
        });

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.MAIN);
            }
        });
    }

    private void refreshStatsUI() {
        statsTable.clearChildren();
        statsTable.top().left();

        addStatRow("Username:", player.username);
        addStatRow("Nickname:", player.nickname);
        addStatRow("Email:", player.email);
        addStatRow("Games Played:", String.valueOf(player.playedGames));
        addStatRow("Coins:", String.valueOf(player.coins));
        addStatRow("Diamonds:", String.valueOf(player.diamonds));
        addStatRow("Levels Passed:", String.valueOf(player.getUnlockedLevels().size() - 1));
        addStatRow("Highest Meow Point:", String.valueOf(player.bestScore));
    }

    private void addStatRow(String labelText, String valueText) {
        Label key = new Label(labelText, skin, "medium_outline");

        Label value = new Label(valueText, skin, "medium_outline");
        value.setColor(Color.WHITE);

        statsTable.add(key).left().padRight(40).padBottom(25);
        statsTable.add(value).left().padBottom(25).row();
    }
}
