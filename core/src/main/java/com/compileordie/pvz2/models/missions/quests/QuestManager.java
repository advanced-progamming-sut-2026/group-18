package com.compileordie.pvz2.models.missions.quests;

import com.badlogic.gdx.utils.TimeUtils;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.QuestDatabase;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class QuestManager {
    public static final ArrayList<Quest> QUESTS = new QuestDatabase().load();
    private static final SimpleDateFormat DAILY_FORMATTER = new SimpleDateFormat("yyyy/MM/dd");

    public static void checkDailyReset(Player player) {
        String today = DAILY_FORMATTER.format(new Date(TimeUtils.millis()));

        // If it's a new day, wipe only the progress of DAILY quests.
        if (player.dailyOffer == null || !today.equals(player.dailyOffer.offerDate)) {
            boolean changed = false;
            for (Quest quest : QUESTS) {
                if (quest.category == QuestCategory.DAILY) {
                    if (player.questProgress.containsKey(quest.id) || player.claimedQuests.contains(quest.id)) {
                        player.questProgress.remove(quest.id);
                        player.claimedQuests.remove(quest.id);
                        changed = true;
                    }
                }
            }
            if (changed) new UserDatabase(player.username).save(player);
        }
    }

    // THIS IS THE OBSERVER ENDPOINT. Call this from anywhere in the game
    // (e.g., zombie.die() -> QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED, 1); )
    public static void dispatch(QuestEvent event, int amount, String context) {
        Player player = AppModel.player;
        if (player == null) return;

        checkDailyReset(player);
        boolean progressMade = false;

        for (Quest quest : QUESTS) {
            // Only evaluate if the quest hasn't been fully claimed yet
            if (!player.claimedQuests.contains(quest.id)) {

                // The quest decides for itself if it cares about this event
                int progressToAdd = quest.evaluateProgress(event, amount, context, player);

                if (progressToAdd != 0) {
                    int current = player.questProgress.getOrDefault(quest.id, 0);
                    // Clamps the value between 0 and the target amount
                    int newAmount = Math.clamp(current + progressToAdd, 0, quest.targetAmount);

                    if (newAmount != current) {
                        player.questProgress.put(quest.id, newAmount);
                        progressMade = true;
                    }
                }
            }
        }

        // Commit to database immediately so progress isn't lost on crash
        if (progressMade) {
            new UserDatabase(player.username).save(player);
        }
    }

    public static Quest getQuestById(String id) {
        for (Quest quest : QUESTS) {
            if (quest.id.equalsIgnoreCase(id)) return quest;
        }
        return null;
    }
}
