package com.compileordie.pvz2.models.missions.quests;

import com.compileordie.pvz2.models.user.Player;

public abstract class Quest {
    public String id;
    public String title;
    public String description;
    public QuestCategory category;
    public int priority;
    public int targetAmount;
    public String rewardType;
    public int rewardAmount;

    public Quest() {
    }

    protected Quest(String id,
                    String title,
                    String description,
                    QuestCategory category,
                    int priority,
                    int targetAmount,
                    String rewardType,
                    int rewardAmount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.targetAmount = targetAmount;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
    }

    /**
     * @param event   The category of the event (e.g., ZOMBIE_KILLED)
     * @param amount  The numerical value to add
     * @param context Additional string data (e.g., "CONEHEAD", "PEASHOOTER", "CHAPTER_1"). Can be null.
     * @param player  The active player state
     * @return The exact progress amount to add to this quest.
     */
    public abstract int evaluateProgress(QuestEvent event, int amount, String context, Player player);
}
