package com.compileordie.pvz2.models.user;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
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

public class Player implements Json.Serializable {
    public String username = "undefined";
    public String nickname = "undefined";
    public String email = "undefined@undefined.com";
    public Gender gender = Gender.MALE;
    public String passwordHash =
        "$argon2id$v=19$m=20480,t=2,p=1$P+7t3z9MTbQYJxkoJVY/7w$1ZN99CUv/LXi8S/eBiVIEL7teLDb4xsD27pKokhdxKw";
    public String securityQuestion = "answer for x = 33 + 77";
    public String securityAnswer = "110";
    public ArrayList<PlantType> unlockedPlants = new ArrayList<>();
    public ArrayList<ZombieType> unlockedZombies = new ArrayList<>();
    public ArrayList<LevelID> unlockedLevelIDs = new ArrayList<>();
    public ArrayList<Pot> greenhousePots = new ArrayList<>();
    public ArrayList<News> news = new ArrayList<>();
    public HashMap<PlantType, Integer> seedPackets = new HashMap<>();
    public HashMap<PlantType, Integer> plantLevels = new HashMap<>();
    public HashMap<PlantType, Boolean> plantBoosts = new HashMap<>();
    public HashMap<String, Integer> questProgress = new HashMap<>();
    public HashSet<String> claimedQuests = new HashSet<>();
    public DailyOffer dailyOffer = null;
    public int playedGames = 0;
    public int completedMiniGames = 0;
    public int completedTotalDailyQuests = 0;
    public int completedTotalNonDailyQuests = 0;
    public int bestScore = 0;
    public int plantFoodCount = 0;
    public int coins = 0;
    public int diamonds = 0;
    public float playtime = 0f;
    public int difficultyLevel = 3;
    public int gameSpeedCoefficient = 1;
    public boolean showGridBox = false;
    public boolean debugMode = false;

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
        // Call the setup method for BRAND-NEW players
        initializeMissingData();
        // Setup new-player-only things (like unlocking starting levels)
        for (LevelID levelID : LevelID.values()) {
            if (levelID.chapterType == ChapterType.MINIGAME) {
                unlockedLevelIDs.add(levelID);
            }
        }
        unlockedLevelIDs.add(LevelID.values()[0]);
    }

    private void initializeMissingData() {
        // Ensures maps have all current enum values safely
        for (PlantType plantType : PlantType.values()) {
            seedPackets.putIfAbsent(plantType, 0);
            plantLevels.putIfAbsent(plantType, 1);
            plantBoosts.putIfAbsent(plantType, false);
        }

        // Pre-populate the greenhouse
        if (greenhousePots.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                this.greenhousePots.add(new Pot());
            }
        }

        // Core plants failsafe
        if (!unlockedPlants.contains(PlantType.PEASHOOTER)) unlockedPlants.add(PlantType.PEASHOOTER);
        if (!unlockedPlants.contains(PlantType.SUNFLOWER)) unlockedPlants.add(PlantType.SUNFLOWER);
        if (!unlockedPlants.contains(PlantType.WALL_NUT)) unlockedPlants.add(PlantType.WALL_NUT);
        if (!unlockedPlants.contains(PlantType.POTATO_MINE)) unlockedPlants.add(PlantType.POTATO_MINE);
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

    @Override
    public void write(Json json) {
        json.writeFields(this);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        // Step A: Load the user's actual save data and overwrite the defaults
        json.readFields(this, jsonData);
        // Step B: Patch missing data!
        initializeMissingData();
    }
}
