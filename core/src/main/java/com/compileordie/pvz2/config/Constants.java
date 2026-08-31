package com.compileordie.pvz2.config;

public final class Constants {
    private Constants() {
    }

    public static final class Paths {
        public static final class Saves {
            public static final String ROOT = "../saves/";
            public static final String PREFERENCES = ROOT + "preferences.json";
            public static final String AUTH = ROOT + "auth.json";
            public static final String USERS = ROOT + "users/";
            public static final String ACCOUNTS = ROOT + "accounts.tsv";
        }

        public static final class Configs {
            public static final String ROOT = "configs/";
            public static final String GAMEPLAY = ROOT + "gameplay.json";
            public static final String ECONOMY = ROOT + "economy.json";
            public static final String ZOMBIES = ROOT + "zombie.json";
            public static final String SECURITY_QUESTIONS = ROOT + "security_questions.json";
            public static final String PLANTS = ROOT + "main_plantscsv.csv";
        }

        public static final class Assets {
            public static final class Quests {
                public static final String ROOT = "quests/";
                public static final String DATABASE = ROOT + "quests.json";
            }
        }
    }

    public static final class ArgonHashing {
        public static final int ITERATIONS = 2;
        public static final int MEMORY = 20480;
        public static final int PARALLELISM = 1;
    }

    public static final class Game {
        public static final int BOARD_ROWS = 5;
        public static final int BOARD_COLS = 9;
        public static final float GRAVITY_COEFFICIENT = 1f;
        public static final float TIME_COEFFICIENT = 0.05f;
        public static final float TILE_WIDTH = 1.045f;
        public static final float TILE_HEIGHT = 1.3f;
        public static final float LANE_LENGTH = 18f;
        public static final float PROSPECTOR_BOOM_X = 6.5f;
        public static final float EAT_HOME_X = 4.8f;
        public static final float DEADLINE_X = 8.6f;
        public static final float BRAINS_X = 5.6f;
        public static final float X_OF_MOWER = 5f;
        public static final float PADDING_X = 6.47f;
        public static final float PADDING_Y = 1.05f;
        public static final float PADDING_X_REALITY = 6.47f;
        public static final float PADDING_Y_REALITY = 1.6f;
        public static final float PADDING_FOR_MOWER = 1f;
        public static final int MAX_SELECTION_SIZE = 8;
        public static final int MIN_SELECTION_SIZE = 3;
    }

    public static final class UI {
        public static final int DEFAULT_WIDTH = 1920;
        public static final int DEFAULT_HEIGHT = 1080;
        public static final float UPP = 1.5f;
        public static final float METER_TO_PIX = 105f;
        public static final float BOTTOM_LINE_PIX = 190f;
        public static final float BOTTOM_LINE_METER = 1.8f;
    }

    public static final class QuestCallbacks {
        // Booleans (Append ":TRUE" or ":FALSE")
        public static final String SYMMETRIC = "SYMMETRIC";
        public static final String ANTI_SYMMETRIC = "ANTI_SYMMETRIC";
        public static final String DAY_WITH_NIGHT = "DAY_WITH_NIGHT";

        // Numerical Limits (Append ":<amount>")
        public static final String REMAINING_SUN = "REMAINING_SUN";
        public static final String PLANTS_LOST = "PLANTS_LOST";
        public static final String SUN_PLANTS_USED = "SUN_PLANTS_USED";
        public static final String DIFFICULTY = "DIFFICULTY";

        // Grid Positions (Append ":<number>")
        public static final String EMPTY_COL = "EMPTY_COL";
        public static final String EMPTY_ROW = "EMPTY_ROW";
        public static final String EMPTY_CROSS = "EMPTY_CROSS"; // Append ":<col>,<row>"

        // Plant Families (Append ":<FamilyName>")
        public static final String ONLY_FAMILY = "ONLY_FAMILY";
        public static final String NO_FAMILY = "NO_FAMILY";
    }

    // ==========================================
    // تنظیمات پارامتریک زامبی ایمپ (Imp Zombie Constants)
    // ==========================================
    public static final class Imp {
        // شتاب جاذبه زمین اختصاصی برای پرتاب ایمپ (پیکسل بر مجذور ثانیه)
        // این مقدار در شتاب جاذبه عمومی بازی ضرب می‌شود تا کنترل فیزیک پرواز دست خودمان باشد
        public static final double LAUNCH_GRAVITY = 9.81 * 80.0 * Game.GRAVITY_COEFFICIENT;

        // سرعت اولیه پرتاب افقی (حرکت به سمت چپ در صفحه نمایش)
        public static final double DEFAULT_LAUNCH_SPEED_X = 150.0;

        // سرعت اولیه پرتاب عمودی (پرتاب به سمت بالا - جهت مثبت Y)
        public static final double DEFAULT_LAUNCH_SPEED_Y = 300.0;

        // تلرانس فرود ایمپ روی چمن (فاصله مجاز تا سطح زمین برای ثبت فرود موفق)
        public static final double LANDING_TOLERANCE = 2.0;

        // حداکثر زمان مجاز پرواز در فضا (یک مکانیزم پیشگیرانه برای جلوگیری از باگ پرواز بی‌نهایت)
        public static final double MAX_FLIGHT_DURATION_SECONDS = 5.0;
    }
}
