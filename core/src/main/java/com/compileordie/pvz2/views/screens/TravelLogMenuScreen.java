package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.compileordie.pvz2.controllers.menus.progression.TravelLogMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.missions.quests.Quest;
import com.compileordie.pvz2.models.missions.quests.QuestCategory;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.customelements.CurrencyHud;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.skin.BorderedTable;

import java.util.List;

public class TravelLogMenuScreen extends MenuScreen {
    private Table questListTable;
    private QuestCategory currentCategory = QuestCategory.DAILY; // Default tab

    @Override
    public void showCore() {
        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        // Main background board
        BorderedTable board = new BorderedTable();
        stack.add(board);

        Label title = new Label("Travel Log", skin, "big_outline");
        board.add(title).padBottom(15).center().row();

        // 1. Setup Tab Navigation
        setupTabs(board);

        // 2. Setup Scrollable Quest List
        setupQuestScroll(board);

        // 3. Setup Persistent Overlays (Top HUD & Bottom Nav)
        setupTopHud();
        setupBottomNav();

        // 4. Initial Population
        refreshQuestList();
    }

    private void setupTabs(BorderedTable board) {
        Table tabsTable = new Table();
        for (QuestCategory cat : QuestCategory.values()) {
            TextButton tabBtn = new TextButton(cat.name(), skin, "default");
            tabBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    currentCategory = cat;
                    refreshQuestList();
                }
            });
            tabsTable.add(tabBtn).padRight(10);
        }

        TextButton minigamesBtn = new TextButton("MINIGAMES", skin, "default");
        minigamesBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.GAME);
            }
        });
        tabsTable.add(minigamesBtn).padRight(10);

        board.add(tabsTable).padBottom(20).row();
    }

    private void setupQuestScroll(BorderedTable board) {
        questListTable = new Table();
        questListTable.top();

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle(
            skin.get(ScrollPane.ScrollPaneStyle.class)
        );

        if (scrollStyle.vScrollKnob != null) {
            scrollStyle.vScrollKnob = skin.newDrawable(scrollStyle.vScrollKnob, Color.GOLDENROD);
        }

        ScrollPane scrollPane = new ScrollPane(questListTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        board.add(scrollPane).width(800).height(400).pad(10).row();
    }

    private void setupTopHud() {
        Table hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top().left();

        CurrencyHud currencyHud = new CurrencyHud(skin, stage, textureBank);
        currencyHud.setBackground((com.badlogic.gdx.scenes.scene2d.utils.Drawable) null);
        hudTable.add(currencyHud).pad(30).left();
        stage.addActor(hudTable);
    }

    private void setupBottomNav() {
        Table bottomNavTable = new Table();
        bottomNavTable.setFillParent(true);
        bottomNavTable.bottom().left();

        ImageButton backBtn = new ImageButton(skin, "generic_close_circle");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.GAME);
            }
        });

        bottomNavTable.add(backBtn).size(64, 64).pad(35);
        stage.addActor(bottomNavTable);
    }

    private void refreshQuestList() {
        questListTable.clearChildren();

        Result<List<Quest>> result = TravelLogMenuController.getQuestsByCategory(currentCategory);
        if (!result.isSuccess) {
            ToastManager.showError(result.errorMessage);
            return;
        }

        List<Quest> quests = result.data;
        boolean hasVisibleQuests = false;

        for (Quest quest : quests) {
            if (AppModel.player.claimedQuests.contains(quest.id)) {
                continue; // Hide already claimed quests
            }
            hasVisibleQuests = true;
            questListTable.add(createQuestCard(quest)).width(760).padBottom(15).row();
        }

        if (!hasVisibleQuests) {
            Label empty = new Label("No active quests available in this category.", skin, "medium");
            empty.setColor(Color.GRAY);
            questListTable.add(empty).padTop(50).center();
        }
    }

    private Table createQuestCard(Quest quest) {
        Table card = new Table();
        card.setBackground(skin.newDrawable("image_ui_dialog_asset_inner_bkgd_10", new Color(0.89f, 0.79f, 0.61f, 1f)));
        card.pad(15);
        Image icon = new Image(textureBank.region(getRewardIcon(quest.rewardType)));
        icon.setScaling(Scaling.fit);
        card.add(icon).size(64, 64).padRight(20);
        Table details = new Table();
        details.top().left();
        Label titleLbl = new Label(quest.title, skin, "medium_outline");
        titleLbl.setColor(Color.GOLD);
        details.add(titleLbl).left().padBottom(5).row();
        Label descLbl = new Label(quest.description, skin, "default");
        descLbl.setWrap(true);
        details.add(descLbl).width(450).left().padBottom(10).row();
        Label rewardLbl = new Label("Rewards: " + quest.rewardAmount + " " + quest.rewardType, skin, "default");
        rewardLbl.setColor(Color.LIME);
        details.add(rewardLbl).left().padBottom(10).row();
        int current = AppModel.player.questProgress.getOrDefault(quest.id, 0);
        ProgressBar progress = new ProgressBar(0, quest.targetAmount, 1, false, skin, "xp_green");
        progress.setValue(Math.min(current, quest.targetAmount));
        Table progressTable = new Table();
        progressTable.add(progress).width(300).padRight(10);
        Label progressLbl = new Label(current + " / " + quest.targetAmount, skin, "default");
        progressTable.add(progressLbl);
        details.add(progressTable).left();
        card.add(details).expandX().fillX();
        boolean isCompleted = current >= quest.targetAmount;
        TextButton actionBtn = new TextButton(isCompleted ? "CLAIM" : "PLAY", skin, isCompleted ? "green" : "default");
        actionBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isCompleted) {
                    Result<String> claimRes = TravelLogMenuController.claimReward(quest.id);
                    if (claimRes.isSuccess) {
                        ToastManager.showSuccess(claimRes.data);
                        refreshQuestList(); // Re-render the list to remove the claimed quest
                    } else {
                        ToastManager.showError(claimRes.errorMessage);
                    }
                } else {
                    ScreenManager.setMenuScreen(ScreenType.GAME); // Navigate to level map
                }
            }
        });

        card.add(actionBtn).size(120, 50).padLeft(15);
        return card;
    }

    private String getRewardIcon(String type) {
        String upper = type.toUpperCase();
        if (upper.contains("COIN")) return "IMAGE_UI_QUESTS_COIN_ICON";
        if (upper.contains("DIAMOND") || upper.contains("GEM")) return "IMAGE_UI_QUESTS_GEM_ICON";
        return "IMAGE_UI_SUNFLOWER"; // Fallback for seed packets
    }
}
