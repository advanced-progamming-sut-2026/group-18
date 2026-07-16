package com.compileordie.pvz2.models.missions.quests;

public enum QuestEvent {
    ZOMBIE_KILLED,
    ZOMBIE_KILLED_BY_PLANT,
    ZOMBIE_KILLED_QUICKLY,
    ZOMBIE_KILLED_NO_MOWER_FIRST_COL,
    PLANT_PLANTED,
    EXPLOSIVE_PLANTED,
    SUN_COLLECTED,
    MARIGOLD_HARVESTED,
    LEVEL_CLEARED,
    LEVEL_FAILED // Used to reset streak-based quests
}
