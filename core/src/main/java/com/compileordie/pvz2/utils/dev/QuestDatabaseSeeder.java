package com.compileordie.pvz2.utils.dev;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.missions.quests.*;

import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class QuestDatabaseSeeder {
    public static ArrayList<Quest> getQuests() {
        ArrayList<Quest> quests = new ArrayList<>();

        // 1. Daily Sun Catcher (Low Priority)
        IntStream.of(3000, 4000, 5000).forEach(sun ->
            quests.add(new BasicEventQuest("daily_sun_" + sun,
                "Daily Sun Catcher", "Collect " + sun + " sun units in a single day.",
                QuestCategory.DAILY, 1, sun, "COINS", sun / 100, QuestEvent.SUN_COLLECTED))
        );

        // 2. Chapter Hunter (Critical Priority)
        Stream.of("ANCIENT_EGYPT", "DARK_AGES", "BIG_WAVE_BEACH", "FROSTBITE_CAVES", "MINIGAME").forEach(chapter ->
            quests.add(new SpecificEventQuest("main_chapter_" + chapter.toLowerCase(),
                "Chapter Hunter", "Defeat 50 zombies from the " + chapter + " world.",
                QuestCategory.CRITICAL, 4, 50, "RANDOM_SEED", 10, QuestEvent.ZOMBIE_KILLED, chapter))
        );

        // 3. Pro Plant Player (Low Priority)
        Stream.of("PEASHOOTER", "CABBAGE_PULT", "KERNEL_PULT").forEach(plant ->
            quests.add(new SpecificEventQuest("daily_pro_" + plant.toLowerCase(),
                "Pro Plant Player", "Kill 10 zombies using only " + plant + ".",
                QuestCategory.DAILY, 1, 10, "RANDOM_PLANT", 1, QuestEvent.ZOMBIE_KILLED_BY_PLANT, plant))
        );

        // 4. Only Cactus (Medium Priority - Diamond Reward)
        quests.add(new SpecificEventQuest("daily_cactus_only",
            "Only Cactus", "Kill 10 zombies using only Cactus.",
            QuestCategory.DAILY, 2, 10, "DIAMONDS", 20, QuestEvent.ZOMBIE_KILLED_BY_PLANT, "CACTUS"));

        // 5. Thrifty Herbivore (Critical Priority)
        IntStream.rangeClosed(0, 5).forEach(n ->
            quests.add(new LevelConditionQuest("main_thrifty_" + n,
                "Thrifty Herbivore", "Win a level without losing more than " + n + " plants.",
                QuestCategory.CRITICAL, 4, "RANDOM_SEED", 20 - n, "PLANTS_LOST:<=" + n))
        );

        // 6. Defense Master (Epic Priority)
        quests.add(new LevelConditionQuest("epic_defense_master",
            "Defense Master", "Complete a level with exactly zero sun.",
            QuestCategory.EPIC, 3, "DIAMONDS", 200, "REMAINING_SUN:0"));

        // 7. Quick Action (Critical Priority)
        quests.add(new BasicEventQuest("main_quick_action",
            "Quick Action", "Kill 10 zombies in less than 30 seconds from the start of the first wave.",
            QuestCategory.CRITICAL, 4, 10, "COINS", 500, QuestEvent.ZOMBIE_KILLED_QUICKLY));

        // 8. Master Demolisher (Low Priority)
        quests.add(new BasicEventQuest("daily_demolisher",
            "Master Demolisher", "Use 3 explosive plants in a single level.",
            QuestCategory.DAILY, 1, 3, "COINS", 100, QuestEvent.EXPLOSIVE_PLANTED));

        // 9. Symmetry & What OCD? (Medium Priority)
        quests.add(new LevelConditionQuest("daily_symmetry",
            "Symmetry", "The lawn must end up symmetrical.",
            QuestCategory.DAILY, 2, "COINS", 500, "SYMMETRIC:TRUE"));
        quests.add(new LevelConditionQuest("daily_anti_symmetry",
            "What OCD?", "Win a level such that there is no symmetry on the lawn (except for the middle row).",
            QuestCategory.DAILY, 2, "COINS", 800, "ANTI_SYMMETRIC:TRUE"));

        // 10. Family Quests (Medium Priority)
        Stream.of("PEA", "MINT", "ARMAMINT").forEach(family -> {
            quests.add(new LevelConditionQuest("daily_family_slayer_" + family.toLowerCase(),
                "Family Slayer", "Use only " + family + " family plants to kill zombies.",
                QuestCategory.DAILY, 2, "COINS", 1000, "ONLY_FAMILY:" + family));
            quests.add(new LevelConditionQuest("daily_family_limit_" + family.toLowerCase(),
                "Flourishing in Limits", "Do not use any plants from the " + family + " family.",
                QuestCategory.DAILY, 2, "DIAMONDS", 100, "NO_FAMILY:" + family));
        });

        // 11. Night or Morning (Epic Priority)
        quests.add(new LevelConditionQuest("epic_night_morning",
            "Night or Morning", "Complete a daytime level using night plants (mushrooms).",
            QuestCategory.EPIC, 3, "DIAMONDS", 20, "DAY_WITH_NIGHT:TRUE"));

        // 12. Win After Win (Medium Priority)
        quests.add(new StreakQuest("daily_streak",
            "Win After Win", "Win 5 levels in a row on the highest difficulty.",
            QuestCategory.DAILY, 2, 5, "COINS", 5000, "DIFFICULTY:5"));

        // 13. Almost Victorious (Low Priority)
        quests.add(new BasicEventQuest("daily_almost_vic",
            "Almost Victorious", "Kill 10 zombies in the first column of a row that has no lawn mower.",
            QuestCategory.DAILY, 1, 10, "COINS", 300, QuestEvent.ZOMBIE_KILLED_NO_MOWER_FIRST_COL));

        // 14. Cloudy Day (Medium Priority)
        quests.add(new LevelConditionQuest("daily_cloudy",
            "Cloudy Day", "Complete a level using only 3 sun-producing plants.",
            QuestCategory.DAILY, 2, "DIAMONDS", 10, "SUN_PLANTS_USED:<=3"));

        // 15. Column and Row Restrictions (Medium Priority)
        IntStream.of(1, 2, 9).forEach(col ->
            quests.add(new LevelConditionQuest("daily_no_col_" + col,
                "One Less Column", "Win a level without planting in column " + col + ".",
                QuestCategory.DAILY, 2, "DIAMONDS", 10, "EMPTY_COL:" + col))
        );
        IntStream.of(1, 3, 5).forEach(row ->
            quests.add(new LevelConditionQuest("daily_no_row_" + row,
                "Defenseless Row", "Win a level without planting in row " + row + ".",
                QuestCategory.DAILY, 2, "DIAMONDS", 20, "EMPTY_ROW:" + row))
        );
        quests.add(new LevelConditionQuest("daily_no_cross_3_3",
            "Defenseless Cross", "Win a level where column 3 and row 3 are empty.",
            QuestCategory.DAILY, 2, "DIAMONDS", 25, "EMPTY_CROSS:3,3"));

        // 16. Mowing Time (Epic Priority)
        IntStream.of(10, 20, 30, 40, 50).forEach(n ->
            quests.add(new SpecificEventQuest("epic_mowing_" + n,
                "Mowing Time", "Kill at least " + n + " zombies with lawn mowers.",
                QuestCategory.EPIC, 3, n, "DIAMONDS", n, QuestEvent.ZOMBIE_KILLED_BY_PLANT, "MOWER"))
        );

        return quests;
    }

    public static void seed() {
        FileHandle file = Gdx.files.local("assets/" + Constants.Paths.Assets.Quests.DATABASE);
        file.writeString(new Json().prettyPrint(getQuests(), 0)
            .replace("\t", "  ")
            .replace("\n", "\n  ")
            .replace("\n  ]", "\n]"), false);
        System.out.println("XXXXXXXXXXX");
    }
}
