package com.compileordie.pvz2.models.user;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.levels.types.ChapterType;
import com.compileordie.pvz2.models.levels.types.LevelId;
import com.compileordie.pvz2.models.minigames.MiniGameType;
import com.compileordie.pvz2.models.missions.News;
import com.compileordie.pvz2.models.missions.Pot;
import com.compileordie.pvz2.models.missions.shop.DailyOffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
    public ArrayList<ChapterType> unlockedChapters;
    public ArrayList<LevelId> unlockedLevels;
    public ArrayList<MiniGameType> unlockedMiniGames;
    public ArrayList<Pot> greenhousePots;
    public ArrayList<News> news;
    public Map<PlantType, Integer> seedPackets;
    public Map<PlantType, Integer> plantLevels;
    public Map<PlantType, Boolean> plantBoosts;
    public Map<String, Integer> questProgress;
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
        this.unlockedChapters = new ArrayList<>();
        this.unlockedLevels = new ArrayList<>();
        this.unlockedMiniGames = new ArrayList<>();
        this.greenhousePots = new ArrayList<>();
        this.news = new ArrayList<>();
        this.dailyOffer = null;
        this.completedMiniGames = 0;
        this.completedTotalDailyQuests = 0;
        this.completedTotalNonDailyQuests = 0;
        this.bestScore = 0;
        this.difficultyLevel = 3;
        this.plantFoodCount = 0;
        this.coins = 0;
        this.diamonds = 0;
        this.seedPackets = new HashMap<>();
        this.plantLevels = new HashMap<>();
        this.plantBoosts = new HashMap<>();
        for (PlantType plantType : PlantType.values()) {
            seedPackets.put(plantType, 0);
            plantLevels.put(plantType, 1);
            plantBoosts.put(plantType, false);
        }
        this.questProgress = new HashMap<>();
        this.claimedQuests = new HashSet<>();

        // Initialize Row 1 (first 5 pots) as unlocked per the design requirements
        for (int i = 0; i < 5; i++) {
            this.greenhousePots.add(new Pot());
        }
    }

    public boolean isSecurityAnswerCorrect(String answer) {
        return securityAnswer.equalsIgnoreCase(answer);
    }

    public float getDLIncrease() {
        return difficultyLevel / 3f;
    }

    public float getDLDecrease() {
        return 3f / difficultyLevel;
    }
}
