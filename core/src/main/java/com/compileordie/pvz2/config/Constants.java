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
            public static final String ZOMBIES = ROOT + "zombie.json";
            public static final String SECURITY_QUESTIONS = ROOT + "security_questions.json";
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
        public static final float TILE_SIZE = 1f;
        public static final float GRAVITY_COEFFICIENT = 1f;
        public static final float TIME_COEFFICIENT = 0.1f;

        // TODO : complete these
        public static final float TILE_WIDTH = 0.0f;
        public static final float TILE_HEIGHT = 0.0f;
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
