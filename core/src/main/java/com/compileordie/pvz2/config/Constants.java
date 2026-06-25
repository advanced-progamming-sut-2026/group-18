package com.compileordie.pvz2.config;

public final class Constants {
    private Constants() {
    }

    public static final class Paths {
        public static final String SECURITY_QUESTIONS = "security_questions.json";
        public static final String SAVES = "saves/";
        public static final String PREFERENCES = SAVES + "preferences.json";
        public static final String AUTH = SAVES + "auth.json";
        public static final String USERS = SAVES + "users/";
    }

    public static final class ArgonHashing {
        public static final int ITERATIONS = 2;
        public static final int MEMORY = 20480;
        public static final int PARALLELISM = 1;
    }
}
