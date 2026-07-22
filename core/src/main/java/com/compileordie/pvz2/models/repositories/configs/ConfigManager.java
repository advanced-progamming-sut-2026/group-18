package com.compileordie.pvz2.models.repositories.configs;

import com.compileordie.pvz2.config.Constants;

public final class ConfigManager {
    private static GameplayConfig gameplay;
    private static SecurityQuestionConfig securityQuestion;
    private static EconomyConfig economy;
    private static ZombiesConfig zombies;

    private ConfigManager() {
    }

    public static void init() {
        gameplay = ConfigLoader.load(Constants.Paths.Configs.GAMEPLAY, GameplayConfig.class);
        securityQuestion = ConfigLoader.load(Constants.Paths.Configs.SECURITY_QUESTIONS, SecurityQuestionConfig.class);
        economy = ConfigLoader.load(Constants.Paths.Configs.ECONOMY, EconomyConfig.class);
        zombies = ConfigLoader.load(Constants.Paths.Configs.ZOMBIES, ZombiesConfig.class);
    }

    public static GameplayConfig gameplay() {
        return gameplay;
    }

    public static SecurityQuestionConfig securityQuestion() {
        return securityQuestion;
    }

    public static EconomyConfig economy() {
        return economy;
    }

    public static ZombiesConfig zombies() {
        return zombies;
    }
}
