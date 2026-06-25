package com.compileordie.pvz2.models;

import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class AppModel {
    private static boolean appIsRunning = true;
    private static Menu menu = Menu.SIGNUP;
    private static Player player = null;
    private static final Queue<String> beforePrompts = new LinkedList<>();
    private static final Queue<String> afterPrompts = new LinkedList<>();

    private AppModel() {
    }

    public static boolean isRunning() {
        return appIsRunning;
    }

    public static void stop() {
        appIsRunning = false;
    }

    public static Menu getMenu() {
        return menu;
    }

    public static void setMenu(Menu menu) {
        AppModel.menu = menu;
    }

    public static Player getPlayer() {
        return player;
    }

    public static boolean hasPlayer() {
        return player != null;
    }

    public static void setPlayer(Player player) {
        AppModel.player = player;
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

    public static void addAllBeforePrompt(Collection<? extends String> prompts) {
        beforePrompts.addAll(prompts);
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

    public static void addAllAfterPrompt(Collection<? extends String> prompts) {
        afterPrompts.addAll(prompts);
    }
}
