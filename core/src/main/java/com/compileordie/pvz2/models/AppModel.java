package com.compileordie.pvz2.models;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.GameSession;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.user.Player;


import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public class AppModel {
    public static Queue<String> beforePrompts = new LinkedList<>();
    public static Queue<String> afterPrompts = new LinkedList<>();
    public static boolean isRunning = true;
    public static Player player = null;
    public static ChapterType currentChapter = null;
    public static LevelID currentLevel = null;
    public static GameSession gameSession = null;
    public static Map<PlantType, Boolean> selectionDeck = new EnumMap<>(PlantType.class);



    private AppModel() {
    }

    public static void stop() {
        isRunning = false;
    }

    public static boolean isLoggedOut() {
        return player == null;
    }

    public static void clearPlayer() {
        player = null;
    }

    public static boolean hasBeforePrompt() {
        return !beforePrompts.isEmpty();
    }

    public static String getBeforePrompt() {
        return beforePrompts.poll();
    }

    public static void addBeforePrompt(String prompt) {
        beforePrompts.add(prompt);
    }

    public static boolean hasAfterPrompt() {
        return !afterPrompts.isEmpty();
    }

    public static String getAfterPrompt() {
        return afterPrompts.poll();
    }

    public static void addAfterPrompt(String prompt) {
        afterPrompts.add(prompt);
    }

    public static void clearSessionData() {
        currentChapter = null;
        currentLevel = null;
        gameSession = null;
        selectionDeck = new EnumMap<>(PlantType.class);
    }
}
