package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.controllers.menus.home.NewsMenuController;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.missions.News;

import java.util.List;

public class NewsModal extends BaseModal {
    public NewsModal(Skin skin) {
        super("News & Updates", skin);

        // 1. Container for all news items
        Table newsListTable = new Table();
        newsListTable.top().pad(10);

        // 2. Fetch data via the controller
        Result<List<News>> newsResult = NewsMenuController.getAllNews();

        if (newsResult.isSuccess) {
            java.util.List<News> newsList = newsResult.data;
            // Iterate backwards to show the newest news at the top
            for (int i = newsList.size() - 1; i >= 0; i--) {
                newsListTable.add(createNewsCard(newsList.get(i), skin)).fillX().expandX().padBottom(15).row();
            }
        } else {
            Label emptyLabel = new Label(newsResult.errorMessage, skin, "medium");
            emptyLabel.setColor(Color.GRAY);
            newsListTable.add(emptyLabel).top();
        }

        // 3. Wrap the list in a ScrollPane
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle(
            skin.get(ScrollPane.ScrollPaneStyle.class)
        );
        // Tint the vertical scroll knob so it stands out against the background
        scrollStyle.vScrollKnob = skin.newDrawable(scrollStyle.vScrollKnob, Color.GOLDENROD);
        ScrollPane scrollPane = new ScrollPane(newsListTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // Vertical scrolling only

        // 4. Populate inherited body/button tables
        bodyTable.add(scrollPane).width(450).height(300).padTop(10);

        TextButton closeBtn = new TextButton("Close", skin, "green");
        buttonTable.add(closeBtn).size(150, 50);

        // 5. Action Listeners
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                NewsMenuController.markAllAsRead(); // Save state on exit
                hide();
            }
        });
    }

    private Table createNewsCard(News newsItem, Skin skin) {
        Table card = new Table();
        card.setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        card.pad(15);

        // Header: Title + "NEW!" indicator (if unread)
        Table header = new Table();
        if (newsItem.title != null) {
            Label titleLabel = new Label(newsItem.title, skin, "medium");
            titleLabel.setColor(Color.GOLD);
            header.add(titleLabel).left().expandX();
        }

        if (!newsItem.isRead) {
            Label newTag = new Label("NEW!", skin, "medium");
            newTag.setColor(Color.CORAL);
            header.add(newTag).right();
        }

        card.add(header).fillX().expandX().padBottom(5).row();

        // Details
        Label detailsLabel = new Label(newsItem.details, skin, "default");
        detailsLabel.setWrap(true);
        card.add(detailsLabel).fillX().expandX().padBottom(10).row();

        // Footer: Date
        Label dateLabel = new Label(newsItem.datetimeToString(), skin, "default");
        dateLabel.setColor(Color.LIGHT_GRAY);
        dateLabel.setFontScale(0.8f);
        card.add(dateLabel).right();

        return card;
    }
}
