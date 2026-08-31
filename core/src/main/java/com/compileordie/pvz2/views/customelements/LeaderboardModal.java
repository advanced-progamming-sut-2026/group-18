package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.network.AuthClient;
import com.compileordie.pvz2.network.NetworkSession;
import com.compileordie.pvz2.network.PlayerSerializer;
import com.compileordie.pvz2.network.protocol.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * فاز ۳: قبلا این مودال با AuthDatabase (رجیستری محلی) کار می‌کرد که در دنیای آنلاین همیشه
 * خالی است (SignupMenuController دیگر چیزی در آن نمی‌نویسد)، پس لیدربورد همیشه "خالی" نشان
 * داده می‌شد. حالا طبق سند فاز ۳ ("اطلاعات لیدربورد باید از دادههای ذخیرهشده کاربران در سرور
 * دریافت شود و با تغییر اطلاعات کاربران بهروزرسانی شود") هر بار این مودال باز می‌شود (یا دکمه‌ی
 * Refresh زده می‌شود)، لیست زنده‌ی همه‌ی حساب‌ها را از سرور (LEADERBOARD_REQUEST) می‌گیرد.
 */
public class LeaderboardModal extends BaseModal {
    private final Skin skin;
    private Table dataTable;
    private Table headerTable;
    private List<Player> cachedPlayers;
    private String loadError;

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

        TextButton refreshBtn = new TextButton("Refresh", skin, "default");
        refreshBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                loadAllPlayers();
                refreshLeaderboardUI();
            }
        });

        // Modal Close Button
        TextButton closeBtn = new TextButton("Close", skin, "green");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide(); // Provided by BaseModal
            }
        });

        // Add to the inherited buttonTable from BaseModal
        buttonTable.add(refreshBtn).size(150, 50).padRight(10);
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
     * فاز ۳: به‌جای خواندن رجیستری محلی، از سرور می‌خواهد. توجه: این فراخوانی synchronous
     * است (دقیقا هم‌رده‌ی همان الگوی موجود در LoginMenuController/SignupMenuController در
     * این پروژه) - یعنی باز کردن مودال ممکن است تا چند صدم ثانیه صبر کند تا جواب سرور برسد.
     * برای این فاز و این حجم پروژه (بدون هیچ ترد اضافه/callback جدید) کافی و قابل قبول است.
     */
    private void loadAllPlayers() {
        cachedPlayers = new ArrayList<>();
        loadError = null;

        Message result;
        try {
            AuthClient authClient = NetworkSession.ensureConnected();
            result = authClient.fetchLeaderboard();
        } catch (IOException e) {
            loadError = "Could not connect to the server.";
            return;
        }

        if (result.getType() != com.compileordie.pvz2.network.protocol.MessageType.LEADERBOARD_RESULT) {
            loadError = "The server could not load the leaderboard right now.";
            return;
        }

        int count = parseIntOrZero(result.get("count"));
        for (int i = 0; i < count; i++) {
            String username = result.get("user_" + i);
            String data = result.get("data_" + i);
            if (username == null || data == null || data.isEmpty()) {
                continue; // یعنی این کاربر ثبت‌نام کرده ولی هنوز هیچ PLAYER_STATE_PUSH ای انجام نداده
            }
            Player player = PlayerSerializer.deserialize(data);
            if (player != null) {
                player.username = username;
                cachedPlayers.add(player);
            }
        }
    }

    private static int parseIntOrZero(String value) {
        try {
            return value == null ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Sorts the cached players based on the current parameter and populates the ScrollPane
     */
    private void refreshLeaderboardUI() {
        rebuildHeaders();
        dataTable.clearChildren();

        if (loadError != null) {
            Label errorLbl = new Label(loadError, skin, "medium_outline");
            errorLbl.setColor(Color.SALMON);
            dataTable.add(errorLbl).padTop(50).center();
            return;
        }

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
