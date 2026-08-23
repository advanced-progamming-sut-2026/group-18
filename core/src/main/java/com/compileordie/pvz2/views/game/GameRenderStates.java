package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.graphics.Color;
import com.compileordie.pvz2.models.entities.LawnMower;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.board.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * View-only render state holders extracted from GameScreen (no logic change).
 */
final class GameRenderStates {

    GameRenderStates() {}

    static final class PlantRenderState {
        float animTime = 0f;
        String currentClip = "idle";
    }

    static final class ZombieRenderState {
        float animTime = 0f;
        boolean wasEating = false;
        boolean flip = true;
        float lastDrawX = Float.NaN;
        float lastDrawY = Float.NaN;
        float damageAlphaTimer = 0f;
        boolean armDropped = false;
        float lastArmorHealth = Float.NaN;
        float bombAnimTime = 0f;
        boolean wasBooming = false;
    }

    static final class DeadZombieAnim {
        String typeKey;
        float x, y;
        boolean flip;
        float animTime = 0f;
        public Color effectColor;
        public boolean isReversedDirection;
        static final float MAX_DURATION = 1.5f;
    }

    static final class FallingDebris {
        String pam;
        String partName;
        String sourceClip;
        float freezeTime;
        float startX, startY;
        float scaleX, scaleY;
        boolean parabolic;
        float horizontalDir;
        float fallDistanceM;
        float animTime = 0f;
    }

    static final class MowerRenderState {
        float animTime = 0f;
        boolean wasIdle = true;
    }

    static final class TombRenderState {
        float lastDrawX = Float.NaN;
        float lastDrawY = Float.NaN;
        float damageAlphaTimer = 0f;
    }

    static final class SunRenderState {
        float animTime = 0f;
        boolean isFading = false;
        float fadeTimer = 0f;
    }

    final Map<Plant, PlantRenderState> plantRenderStates = new HashMap<>();
    final Map<Zombie, ZombieRenderState> zombieRenderStates = new HashMap<>();
    final Map<LawnMower, MowerRenderState> mowerRenderStates = new HashMap<>();
    final Map<Tomb, TombRenderState> tombRenderStates = new HashMap<>();
    final Map<Object, SunRenderState> sunRenderStates = new HashMap<>();
    final Map<Tile, Float> fireTileAnimTimes = new HashMap<>();
    final List<DeadZombieAnim> deadZombies = new ArrayList<>();
    final List<FallingDebris> fallingDebris = new ArrayList<>();

    void clearAll() {
        zombieRenderStates.clear();
        mowerRenderStates.clear();
        tombRenderStates.clear();
        sunRenderStates.clear();
        fireTileAnimTimes.clear();
        deadZombies.clear();
        fallingDebris.clear();
        plantRenderStates.clear();
    }
}
