package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.graphics.Color;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.MovementState;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.SnorkelZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.KnightZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.StandardZombie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Effect colors + armor visibility map (extracted, logic unchanged).
 */
final class ZombieVisualHelpers {

    private static final float BLINK_SPEED_HYPNOTIZED = 6f;
    private static final float BLINK_SPEED_STUNNED = 8f;
    private static final float BLINK_SPEED_POISON = 5f;

    private ZombieVisualHelpers() {}

    static Color computeZombieEffectColor(Zombie zombie, float effectPulseTime) {
        if (zombie.hasEffect(EffectType.FROZEN)) {
            return new Color(0.15f, 0.35f, 0.75f, 0.95f);
        }
        if (zombie.hasEffect(EffectType.CHILLED)) {
            return new Color(0.25f, 0.55f, 0.85f, 0.85f);
        }
        if (zombie.hasEffect(EffectType.HYPNOTIZED)) {
            float blink = 0.5f + 0.5f * (float) Math.sin(effectPulseTime * BLINK_SPEED_HYPNOTIZED);
            return new Color(1.0f, 0.35f, 0.75f, 0.6f + 0.7f * blink);
        }
        if (zombie.hasEffect(EffectType.STUNNED)) {
            float blink = 0.5f + 0.5f * (float) Math.sin(effectPulseTime * BLINK_SPEED_STUNNED);
            return new Color(1.0f, 0.9f, 0.15f, 0.9f);
        }
        if (zombie.hasEffect(EffectType.POISON)) {
            float blink = 0.5f + 0.5f * (float) Math.sin(effectPulseTime * BLINK_SPEED_POISON);
            return new Color(0.05f, 0.35f, 0.1f, 0.6f + 0.7f * blink);
        }
        Color snorkel = snorkelColor(zombie);
        if (snorkel != null) return snorkel;
        return redWarningColor(zombie);
    }

    private static Color snorkelColor(Zombie zombie) {
        if (!(zombie instanceof SnorkelZombie)) return null;
        MovementState snorkelState = ((SnorkelZombie) zombie).getState();
        if (snorkelState == MovementState.UNDERWATER_SURFACED
            || snorkelState == MovementState.UNDERWATER_NOT_SURFACED) {
            return new Color(0.0f, 0.85f, 1.0f, 1f);
        }
        return null;
    }

    private static Color redWarningColor(Zombie zombie) {
        final float RED_WARNING_X_THRESHOLD = 10f;
        double zombieX = zombie.getX();
        if (zombieX < RED_WARNING_X_THRESHOLD) {
            float intensity = (float) Math.min(1.0,
                (RED_WARNING_X_THRESHOLD - zombieX) / RED_WARNING_X_THRESHOLD);
            float r = 1f;
            float g = 1f - intensity;
            float b = 1f - intensity;
            return new Color(r, g, b, 1f);
        }
        return null;
    }

    static int armorStage(double current, double max) {
        if (max <= 0 || current <= 0) return -1;
        double ratio = current / max;
        if (ratio > 2.0 / 3.0) return 0;
        if (ratio > 1.0 / 3.0) return 1;
        return 2;
    }

    static String findStateKey(List<String> knownKeys, String needle) {
        for (String key : knownKeys) {
            if (key.contains(needle)) return key;
        }
        return null;
    }

    static void applyArmorPieceStage(Map<String, Boolean> map, String masterKey, String prefix,
                                     double current, double max) {
        int stage = armorStage(current, max);
        if (stage == -1) return;
        if (masterKey != null) map.put(masterKey, true);
        String activeKey = switch (stage) {
            case 0 -> prefix + "norm";
            case 1 -> prefix + "damage_01";
            default -> prefix + "damage_02";
        };
        map.put(activeKey, true);
    }

    static Map<String, Boolean> buildArmorVisibilityMap(Zombie zombie,
                                                        ZombieVisualRegistry.ZombieVisualDef def) {
        List<String> stateFilters = def.getResolvedStateFilters();
        if (stateFilters.isEmpty()) return null;

        Map<String, Boolean> map = new HashMap<>();
        for (String key : stateFilters) map.put(key, false);

        ZombieType type = zombie.getType();
        if (type == ZombieType.KNIGHT && zombie instanceof KnightZombie knight) {
            applyArmorPieceStage(map,
                findStateKey(stateFilters, "armor_crown_states"), "zombie_armor_crown_",
                knight.helmetArmorHealth, knight.maxHelmetArmorHealth);
            applyArmorPieceStage(map,
                findStateKey(stateFilters, "zombie_shoulder_armor"), "zombie_shoulder_armor_",
                knight.shoulderArmorHealth, knight.maxShoulderArmorHealth);
        } else if ((type == ZombieType.CONEHEAD || type == ZombieType.BUCKETHEAD
            || type == ZombieType.BLOCKHEAD) && zombie instanceof StandardZombie sz) {
            applyStandardArmor(map, stateFilters, type, sz);
        } else {
            for (String key : stateFilters) map.put(key, true);
        }
        return map;
    }

    private static void applyStandardArmor(Map<String, Boolean> map, List<String> stateFilters,
                                          ZombieType type, StandardZombie sz) {
        String prefix = switch (type) {
            case CONEHEAD -> "zombie_armor_cone_";
            case BUCKETHEAD -> "zombie_armor_bucket_";
            default -> "zombie_armor_brick_";
        };
        String masterNeedle = switch (type) {
            case CONEHEAD -> "armor1_states";
            case BUCKETHEAD -> "armor2_states";
            default -> null;
        };
        String masterKey = masterNeedle != null ? findStateKey(stateFilters, masterNeedle) : null;
        applyArmorPieceStage(map, masterKey, prefix, sz.getArmorHealth(), sz.getMaxArmorHealth());
    }
}
