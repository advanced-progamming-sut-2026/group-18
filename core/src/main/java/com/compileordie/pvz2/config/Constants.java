package com.compileordie.pvz2.config;

public final class Constants {
    private Constants() {
    }

    public static final class Paths {
        public static final class Saves {
            public static final String ROOT = "saves/";
            public static final String PREFERENCES = ROOT + "preferences.json";
            public static final String AUTH = ROOT + "auth.json";
            public static final String USERS = ROOT + "users/";
        }

        public static final class Configs {
            public static final String ROOT = "configs/";
            public static final String GAMEPLAY = ROOT + "gameplay.json";
            public static final String ECONOMY = ROOT + "economy.json";
            public static final String SECURITY_QUESTIONS = ROOT + "security_questions.json";
        }
    }

    public static final class ArgonHashing {
        public static final int ITERATIONS = 2;
        public static final int MEMORY = 20480;
        public static final int PARALLELISM = 1;
    }

    public static final class Game {
        public static final int boardRows = 5;
        public static final int boardCols = 9;
        public static final int tileSize = 1;
    }
}
