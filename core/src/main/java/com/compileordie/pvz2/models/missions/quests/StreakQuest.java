package com.compileordie.pvz2.models.missions.quests;

import com.compileordie.pvz2.models.user.Player;

public class StreakQuest extends Quest {
    public String requiredContext;

    public StreakQuest() {
        super();
    }

    public StreakQuest(String id,
                       String title,
                       String description,
                       QuestCategory category,
                       int priority,
                       int targetAmount,
                       String rewardType,
                       int rewardAmount,
                       String requiredContext) {
        super(id, title, description, category, priority, targetAmount, rewardType, rewardAmount);
        this.requiredContext = requiredContext;
    }

    @Override
    public int evaluateProgress(QuestEvent event, int amount, String context, Player player) {
        if (event == QuestEvent.LEVEL_FAILED) {
            return -9999; // Trigger a complete progress wipe
        } else if (event == QuestEvent.LEVEL_CLEARED && context != null && context.contains(this.requiredContext)) {
            return amount;
        }
        return 0;
    }
}
