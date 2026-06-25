package com.compileordie.pvz2.models.user;

import com.compileordie.pvz2.models.databases.SecurityQuestionDatabase;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.greenhouse.Pot;
import com.compileordie.pvz2.models.levels.types.ChapterType;
import com.compileordie.pvz2.models.levels.types.LevelId;
import com.compileordie.pvz2.models.minigames.MiniGameType;

import java.util.ArrayList;

public class Player {
    private String username;
    private String nickname;
    private String email;
    private Gender gender;
    private String passwordHash;
    private int securityQuestion;
    private String securityAnswer;
    private ArrayList<PlantType> unlockedPlants;
    private ArrayList<ZombieType> unlockedZombies;
    private ArrayList<ChapterType> unlockedChapters;
    private ArrayList<LevelId> unlockedLevels;
    private ArrayList<MiniGameType> unlockedMiniGames;
    private ArrayList<Pot> greenhousePots;
    private int completedMiniGames;
    private int completedTotalDailyQuests;
    private int completedTotalNonDailyQuests;
    private int bestScore;

    public Player() {
    }

    public Player(String username, String passwordHash, String nickname, String email, Gender gender,
                  int securityQuestion, String securityAnswer) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.unlockedPlants = new ArrayList<>();
        this.unlockedZombies = new ArrayList<>();
        this.unlockedChapters = new ArrayList<>();
        this.unlockedLevels = new ArrayList<>();
        this.unlockedMiniGames = new ArrayList<>();
        this.greenhousePots = new ArrayList<>();
        this.completedMiniGames = 0;
        this.completedTotalDailyQuests = 0;
        this.completedTotalNonDailyQuests = 0;
        this.bestScore = 0;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getSecurityQuestion() {
        return new SecurityQuestionDatabase().loadOne(securityQuestion).getText();
    }

    public boolean isSecurityAnswerCorrect(String answer) {
        return securityAnswer.equalsIgnoreCase(answer);
    }

    public ArrayList<PlantType> getUnlockedPlants() {
        return unlockedPlants;
    }

    public void setUnlockedPlants(ArrayList<PlantType> unlockedPlants) {
        this.unlockedPlants = unlockedPlants;
    }

    public ArrayList<ZombieType> getUnlockedZombies() {
        return unlockedZombies;
    }

    public void setUnlockedZombies(ArrayList<ZombieType> unlockedZombies) {
        this.unlockedZombies = unlockedZombies;
    }

    public ArrayList<ChapterType> getUnlockedChapters() {
        return unlockedChapters;
    }

    public void setUnlockedChapters(ArrayList<ChapterType> unlockedChapters) {
        this.unlockedChapters = unlockedChapters;
    }

    public ArrayList<LevelId> getUnlockedLevels() {
        return unlockedLevels;
    }

    public void setUnlockedLevels(ArrayList<LevelId> unlockedLevels) {
        this.unlockedLevels = unlockedLevels;
    }

    public ArrayList<MiniGameType> getUnlockedMiniGames() {
        return unlockedMiniGames;
    }

    public void setUnlockedMiniGames(ArrayList<MiniGameType> unlockedMiniGames) {
        this.unlockedMiniGames = unlockedMiniGames;
    }

    public ArrayList<Pot> getGreenhousePots() {
        return greenhousePots;
    }

    public void setGreenhousePots(ArrayList<Pot> greenhousePots) {
        this.greenhousePots = greenhousePots;
    }

    public int getCompletedMiniGames() {
        return completedMiniGames;
    }

    public void setCompletedMiniGames(int completedMiniGames) {
        this.completedMiniGames = completedMiniGames;
    }

    public int getCompletedTotalDailyQuests() {
        return completedTotalDailyQuests;
    }

    public void setCompletedTotalDailyQuests(int completedTotalDailyQuests) {
        this.completedTotalDailyQuests = completedTotalDailyQuests;
    }

    public int getCompletedTotalNonDailyQuests() {
        return completedTotalNonDailyQuests;
    }

    public void setCompletedTotalNonDailyQuests(int completedTotalNonDailyQuests) {
        this.completedTotalNonDailyQuests = completedTotalNonDailyQuests;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }
}
