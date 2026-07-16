package com.compileordie.pvz2.models.missions.quests;

import com.compileordie.pvz2.models.user.Player;

public class BasicEventQuest extends Quest {
    private final QuestEvent targetEvent;

    public BasicEventQuest(String id, String title, String description, QuestCategory category, int targetAmount, String rewardType, int rewardAmount, QuestEvent targetEvent) {
        super(id, title, description, category, targetAmount, rewardType, rewardAmount);
        this.targetEvent = targetEvent;
    }

    @Override
    public int evaluateProgress(QuestEvent event, int amount, String context, Player player) {
        // Only grant progress if the dispatched event matches what this quest is looking for
        return (this.targetEvent == event) ? amount : 0;
    }
}
