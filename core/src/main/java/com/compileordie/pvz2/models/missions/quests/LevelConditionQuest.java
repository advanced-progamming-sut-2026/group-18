package com.compileordie.pvz2.models.missions.quests;

import com.compileordie.pvz2.models.user.Player;

public class LevelConditionQuest extends Quest {
    public String requiredContext; // e.g., "SYMMETRIC:TRUE" or "PLANTS_LOST:0"

    public LevelConditionQuest() {
        super();
    }

    public LevelConditionQuest(String id, String title, String description, QuestCategory category, String rewardType, int rewardAmount, String requiredContext) {
        super(id, title, description, category, 1, rewardType, rewardAmount); // Target is always 1 for a level clear
        this.requiredContext = requiredContext;
    }

    @Override
    public int evaluateProgress(QuestEvent event, int amount, String context, Player player) {
        // If the level clears, and the broadcasted string contains our specific requirement, grant the win!
        if (event == QuestEvent.LEVEL_CLEARED && context != null && context.contains(this.requiredContext)) {
            return amount;
        }
        return 0;
    }
}
