package com.compileordie.pvz2.models;

import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.LinkedList;
import java.util.Queue;

public class AppModel {
    public static boolean isRunning = true;
    public static Menu menu = Menu.SIGNUP;
    public static Player player = null;
    public static final Queue<String> beforePrompts = new LinkedList<>();
    public static final Queue<String> afterPrompts = new LinkedList<>();

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
}
