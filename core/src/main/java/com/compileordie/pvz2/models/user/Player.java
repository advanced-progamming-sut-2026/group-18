package com.compileordie.pvz2.models.user;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.missions.News;
import com.compileordie.pvz2.models.missions.Pot;
import com.compileordie.pvz2.models.missions.shop.DailyOffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Player {
    public String username;
    public String nickname;
    public String email;
    public Gender gender;
    public String passwordHash;
    public String securityQuestion;
    public String securityAnswer;
    public ArrayList<PlantType> unlockedPlants;
    public ArrayList<ZombieType> unlockedZombies;
    public ArrayList<LevelID> unlockedLevelIDs;
    public ArrayList<Pot> greenhousePots;
    public ArrayList<News> news;
    public HashMap<PlantType, Integer> seedPackets;
    public HashMap<PlantType, Integer> plantLevels;
    public HashMap<PlantType, Boolean> plantBoosts;
    public HashMap<String, Integer> questProgress;
    public HashSet<String> claimedQuests;
    public DailyOffer dailyOffer;
    public int completedMiniGames;
    public int completedTotalDailyQuests;
    public int completedTotalNonDailyQuests;
    public int bestScore;
    public int difficultyLevel;
    public int plantFoodCount;
    public int coins;
    public int diamonds;
    public float playtime;

    public Player() {
    }

    public Player(String username,
                  String passwordHash,
                  String nickname,
                  String email,
                  Gender gender,
                  String securityQuestion,
                  String securityAnswer
    ) {
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.passwordHash = passwordHash;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.unlockedPlants = new ArrayList<>();
        this.unlockedZombies = new ArrayList<>();
        this.unlockedLevelIDs = new ArrayList<>();
        this.greenhousePots = new ArrayList<>();
        this.news = new ArrayList<>();
        this.seedPackets = new HashMap<>();
        this.plantLevels = new HashMap<>();
        this.plantBoosts = new HashMap<>();
        this.questProgress = new HashMap<>();
        this.claimedQuests = new HashSet<>();
        this.dailyOffer = null;
        this.completedMiniGames = 0;
        this.completedTotalDailyQuests = 0;
        this.completedTotalNonDailyQuests = 0;
        this.bestScore = 0;
        this.difficultyLevel = 3;
        this.plantFoodCount = 0;
        this.coins = 0;
        this.diamonds = 0;
        this.playtime = 0f;
        unlockedPlants.add(PlantType.PEASHOOTER);
        unlockedPlants.add(PlantType.SUNFLOWER);
        unlockedPlants.add(PlantType.WALL_NUT);
        unlockedPlants.add(PlantType.POTATO_MINE);
        for (PlantType plantType : PlantType.values()) {
            seedPackets.put(plantType, 0);
            plantLevels.put(plantType, 1);
            plantBoosts.put(plantType, false);
        }
        for (LevelID levelID : LevelID.values()) {
            if (levelID.chapterType == ChapterType.MINIGAME) {
                unlockedLevelIDs.add(levelID);
            }
        }
        unlockedLevelIDs.add(LevelID.values()[0]);
        for (int i = 0; i < 5; i++) {
            this.greenhousePots.add(new Pot());
        }
    }

    public boolean isSecurityAnswerCorrect(String answer) {
        return securityAnswer.equalsIgnoreCase(answer);
    }

    public ArrayList<LevelID> getUnlockedLevels() {
        return unlockedLevelIDs
            .stream()
            .filter(levelID -> levelID.chapterType != ChapterType.MINIGAME)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<ChapterType> getUnlockedChapters() {
        return getUnlockedLevels()
            .stream()
            .map(levelID -> levelID.chapterType)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public float getDLIncrease() {
        return difficultyLevel / 3f;
    }

    public float getDLDecrease() {
        return 3f / difficultyLevel;
    }

    public void spendDiamonds(int amount) {
        this.diamonds = Math.max(0, this.diamonds - amount);
    }

    public void addDiamonds(int amount) {
        this.diamonds += amount;
    }

    public void spendCoins(int amount) {
        this.coins = Math.max(0, this.coins - amount);
    }

    public void addCoins(int amount) {
        this.coins += amount;
    }

    public void consumePlantFood() {
        this.plantFoodCount = Math.max(0, this.plantFoodCount - 1);
    }

    public int getUnreadNewsCount() {
        int counter = 0;
        for (News news : news) {
            if (!news.isRead) counter++;
        }
        return counter;
    }
}
