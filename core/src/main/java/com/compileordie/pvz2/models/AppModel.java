package com.compileordie.pvz2.models;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.GameSession;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.user.Player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;


public class AppModel {
    public static boolean isRunning = true;
    public static boolean isOnline = true;          // Tracks active socket availability
    public static Player player = null;
    public static ChapterType currentChapter = null;
    public static LevelID currentLevel = null;
    public static GameSession gameSession = null;
    public static HashMap<PlantType, Boolean> selectionDeck = new HashMap<>();
    public static Boolean wonLastGame = null;
    public static boolean isReceiverClient = false;
    public static String opponentUsername = null;

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

    public static void clearSessionData() {
        currentChapter = null;
        currentLevel = null;
        gameSession = null;
        selectionDeck = new HashMap<>();
    }
}
