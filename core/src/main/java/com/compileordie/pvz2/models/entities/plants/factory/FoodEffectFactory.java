package com.compileordie.pvz2.models.entities.plants.factory;

import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.enums.PlantFoodEffectType;
import com.compileordie.pvz2.models.entities.plants.strategies.food.*;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;

public class FoodEffectFactory {

    public static PlantFoodEffectStrategy createEffect(
        PlantFoodEffectType type,
        Class<? extends Projectile> projectileClass,
        int effectValue,
        PlantTemplate baseTemplate) {

        if (type == null) return null;

        switch (type) {
            case RAPID_FIRE:
                return new RapidFireEffect(projectileClass);
            case BURST_SUN:
                return new BurstSunEffect(effectValue);
            case AREA_DAMAGE:
                return new AreaDamageEffect(effectValue);
            case ARMOR_BUFF:
                return new ArmorBuffEffect(effectValue);
            case LANE_CLEAR:
                return new LaneClearEffect();
            case SCREEN_FREEZE:
                return new ScreenFreezeEffect(effectValue);
            case SPAWN_CLONES:
                return new SpawnClonesEffect(effectValue, baseTemplate);
            case INSTANT_KILL:
                return new InstantKillEffect(effectValue);
            case MAGNETIC:
                return new MagneticEffect();
            case HYPNOTIZE:
                return new HypnotizeEffect(effectValue);
            case PROJECTILE_ENHANCE:
                return new ProjectileEnhanceEffect(effectValue);
            case HEAL_AND_ATTRACT:
                return new HealAndRedirectEffect(true, effectValue);
            case HOMING_BURST:
                return new HomingBurstEffect(projectileClass, effectValue);
            case GOO_PUDDLE:
                return new GooPuddleEffect();
            default:
                return null;
        }
    }
}
