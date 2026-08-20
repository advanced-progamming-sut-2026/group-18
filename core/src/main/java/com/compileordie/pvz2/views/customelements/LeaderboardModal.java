package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.AuthDatabase;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.user.authentication.UserRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderboardModal extends BaseModal {
    private final Skin skin;
    private Table dataTable;
    private Table headerTable;
    private List<Player> cachedPlayers;

    // Sort State
    private String currentSortParameter = "highest score";
    private boolean isAscending = false;

    public LeaderboardModal(Skin skin) {
        super("Global Leaderboard", skin);
        this.skin = skin;

        initComponents();
        buildLayout();

        // Load data and trigger the first render
        loadAllPlayers();
        refreshLeaderboardUI();
    }

    private void initComponents() {
        headerTable = new Table();
        dataTable = new Table();
        dataTable.top();

        // Modal Close Button
        TextButton closeBtn = new TextButton("Close", skin, "green");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide(); // Provided by BaseModal
            }
        });

        // Add to the inherited buttonTable from BaseModal
        buttonTable.add(closeBtn).size(150, 50);
    }

    private void buildLayout() {
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle(
            skin.get(ScrollPane.ScrollPaneStyle.class)
        );
        scrollStyle.vScrollKnob = skin.newDrawable(scrollStyle.vScrollKnob, Color.GOLDENROD);

        ScrollPane scrollPane = new ScrollPane(dataTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        // Add headers and scroll pane to the inherited bodyTable from BaseModal
        bodyTable.add(headerTable).padBottom(10).row();
        bodyTable.add(scrollPane).width(780).height(350).row();
    }

    /**
     * Replicates the Phase 1 Controller logic to fetch all registered users from the Database
     */
    private void loadAllPlayers() {
        cachedPlayers = new ArrayList<>();
        AuthDatabase authDb = new AuthDatabase();

        for (UserRegistry registry : authDb.load()) {
            Player player = new UserDatabase(registry.getUsername()).load();
            if (player != null) {
                cachedPlayers.add(player);
            }
        }
    }

    /**
     * Sorts the cached players based on the current parameter and populates the ScrollPane
     */
    private void refreshLeaderboardUI() {
        rebuildHeaders();
        dataTable.clearChildren();

        if (cachedPlayers.isEmpty()) {
            Label emptyLbl = new Label("The leaderboard is currently empty.", skin, "medium_outline");
            emptyLbl.setColor(Color.GRAY);
            dataTable.add(emptyLbl).padTop(50).center();
            return;
        }

        Comparator<Player> comparator = getPlayerComparator();

        cachedPlayers.sort(comparator);

        for (Player p : cachedPlayers) {
            boolean isMe = AppModel.player != null && p.username.equals(AppModel.player.username);
            Color rowColor = isMe ? Color.GOLD : Color.WHITE;

            Label userLbl = new Label(p.username, skin, "medium_outline");
            Label scoreLbl = new Label(String.valueOf(p.bestScore), skin, "medium_outline");
            Label miniLbl = new Label(String.valueOf(p.completedMiniGames), skin, "medium_outline");
            Label dailyLbl = new Label(String.valueOf(p.completedTotalDailyQuests), skin, "medium_outline");
            Label nonDailyLbl = new Label(String.valueOf(p.completedTotalNonDailyQuests), skin, "medium_outline");

            Label[] labels = {userLbl, scoreLbl, miniLbl, dailyLbl, nonDailyLbl};
            for (Label lbl : labels) {
                lbl.setColor(rowColor);
                lbl.setAlignment(Align.center);
            }

            // Adjusted widths to fit securely inside the 780px ScrollPane
            dataTable.add(userLbl).width(160).padBottom(15);
            dataTable.add(scoreLbl).width(120).padBottom(15);
            dataTable.add(miniLbl).width(140).padBottom(15);
            dataTable.add(dailyLbl).width(160).padBottom(15);
            dataTable.add(nonDailyLbl).width(160).padBottom(15).row();
        }
    }

    private Comparator<Player> getPlayerComparator() {
        Comparator<Player> comparator = switch (currentSortParameter) {
            case "username" -> Comparator.comparing(p -> p.username.toLowerCase());
            case "minigames" -> Comparator.comparingInt(p -> p.completedMiniGames);
            case "daily-quests" -> Comparator.comparingInt(p -> p.completedTotalDailyQuests);
            case "non-daily-quests" -> Comparator.comparingInt(p -> p.completedTotalNonDailyQuests);
            case "highest score" -> Comparator.comparingInt(p -> p.bestScore);
            default -> Comparator.comparingInt(p -> p.bestScore);
        };

        if (!isAscending) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    /**
     * Builds the interactive column headers and appends sorting arrows (▲ / ▼) to the active column
     */
    private void rebuildHeaders() {
        headerTable.clearChildren();

        // Match the adjusted widths from the data rows
        headerTable.add(createSortButton("Username", "username")).width(160).padRight(5);
        headerTable.add(createSortButton("Highest Score", "highest score")).width(120).padRight(5);
        headerTable.add(createSortButton("Minigames", "minigames")).width(140).padRight(5);
        headerTable.add(createSortButton("Daily Quests", "daily-quests")).width(160).padRight(5);
        headerTable.add(createSortButton("Story Quests", "non-daily-quests")).width(160);
    }

    private TextButton createSortButton(String baseText, String sortKey) {
        String displayText = baseText;
        if (currentSortParameter.equals(sortKey)) {
            displayText += isAscending ? " ▲" : " ▼";
        }

        TextButton btn = new TextButton(displayText, skin, "default");
        btn.getLabel().setFontScale(0.75f); // Shrink text to ensure headers fit on smaller screens

        if (currentSortParameter.equals(sortKey)) {
            btn.setColor(Color.LIGHT_GRAY);
        }

        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (currentSortParameter.equals(sortKey)) {
                    isAscending = !isAscending;
                } else {
                    currentSortParameter = sortKey;
                    isAscending = false;
                }
                refreshLeaderboardUI();
            }
        });

        return btn;
    }
}
