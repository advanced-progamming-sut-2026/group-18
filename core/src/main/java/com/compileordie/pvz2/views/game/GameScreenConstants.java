package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.graphics.Color;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.game.levels.ChapterType;

public final class GameScreenConstants {

    private GameScreenConstants() {
    }

    public static final String ASSET_RESOLUTION = "768";
    public static final String INITIAL_ZOMBIE_ROOT = ASSET_RESOLUTION + "/INITIAL/ZOMBIE/";
    public static final String FULL_ZOMBIE_ROOT = ASSET_RESOLUTION + "/FULL/ZOMBIE/";

    public static final String BG_REGION_PREFIX = "IMAGE_BACKGROUNDS_";
    public static final String BG_TEXTURE_SUFFIX = "_TEXTURE";
    public static final String BG_TEXTURE_LEFT_SUFFIX = "_TEXTURE_LEFT";
    public static final String BG_TEXTURE_RIGHT_SUFFIX = "_TEXTURE_RIGHT";

    public static final float ZOMBIE_SCALE = 0.8f;
    public static final float MOWER_SCALE = 0.8f;
    public static final float TOMB_SCALE = 0.8f;
    public static final float BG_SIDE_SCALE = 0.9f;

    public static final String PAUSE_PANEL_BG_REGION = "IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN";
    public static final String PAUSE_FOG_DECORATION_REGION = "IMAGE_UI_PAUSEMENU_ZOMBOSS_FOG";

    public static final String RESUME_BUTTON_TEXT = "RESUME";
    public static final String RESTART_BUTTON_TEXT = "RESTART";
    public static final String SAVE_EXIT_BUTTON_TEXT = "SAVE AND EXIT";

    public static final float PAUSE_BUTTON_PAD = 20f;

    public static final float PAUSE_PANEL_WIDTH = 650f;
    public static final float PAUSE_PANEL_HEIGHT = 450f;
    public static final float PAUSE_MENU_BUTTON_WIDTH = 320f;
    public static final float PAUSE_MENU_BUTTON_HEIGHT = 90f;
    public static final float PAUSE_MENU_BUTTON_PAD = 12f;

    public static final Color PAUSE_PANEL_FILL_COLOR = new Color(1f, 0.45f, 0.75f, 1f);
    public static final Color PAUSE_PANEL_BORDER_COLOR = Color.WHITE;
    public static final int PAUSE_PANEL_BORDER_PX = 6;

    public static final float PAUSE_FOG_WIDTH_RATIO = 1.0f;
    public static final float PAUSE_FOG_OVERLAP_RATIO = 0.62f;

    public static final float SIMULATION_STEP_SECONDS = Constants.Game.TIME_COEFFICIENT;

    public static String chapterFolder(ChapterType chapter) {
        if (chapter == null) {
            return "EGYPT";
        }
        return switch (chapter) {
            case ANCIENT_EGYPT -> "EGYPT";
            case BIG_WAVE_BEACH -> "BEACH";
            case DARK_AGES -> "DARK";
            case FROSTBITE_CAVES -> "ICEAGE";
            case MINIGAME -> "FRONTLAWN_SPRING";
            default -> "EGYPT";
        };
    }
}
