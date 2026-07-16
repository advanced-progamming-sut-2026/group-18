package com.compileordie.pvz2.models.missions.quests;

import com.compileordie.pvz2.models.user.Player;

public class SpecificEventQuest extends Quest {
    public QuestEvent targetEvent;
    public String targetContext;

    public SpecificEventQuest() {
        super();
    }

    public SpecificEventQuest(String id, String title, String description, QuestCategory category, int targetAmount, String rewardType, int rewardAmount, QuestEvent targetEvent, String targetContext) {
        super(id, title, description, category, targetAmount, rewardType, rewardAmount);
        this.targetEvent = targetEvent;
        this.targetContext = targetContext.toUpperCase();
    }

    @Override
    public int evaluateProgress(QuestEvent event, int amount, String context, Player player) {
        if (this.targetEvent == event && context != null && context.toUpperCase().equals(this.targetContext)) {
            return amount;
        }
        return 0;
    }
}
