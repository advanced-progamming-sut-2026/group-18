package com.compileordie.pvz2.models.repositories.configs;

import com.badlogic.gdx.Gdx;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.UpgradeLevel;
import com.compileordie.pvz2.models.entities.plants.enums.AttackStrategyType;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.plants.enums.PlantFoodEffectType;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class PlantConfigRepository {

    private final Map<String, PlantTemplate> plantDatabase = new HashMap<>();

    public void loadFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(Gdx.files.internal(filePath).reader())) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Split on commas not inside quotes
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length < 16) continue;

                processCsvRow(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processCsvRow(String[] parts) {
        try {
            PlantTemplate template = new PlantTemplate();

            parseBasicInfo(template, parts);
            template.setTags(parseTags(parts[3].trim()));
            parseStats(template, parts);
            template.setUpgradeMap(parseUpgradeMap(parts));
            parseStrategies(template, parts);
            applyDefaultValues(template);

            assignSpecificParameters(template);

            plantDatabase.put(template.getName(), template);

        } catch (Exception e) {
            System.err.println("Error parsing row for plant: " + parts[1]);
            e.printStackTrace();
        }
    }

    private void parseBasicInfo(PlantTemplate template, String[] parts) {
        template.setName(parts[1].trim());
        template.setCategory(PlantCategory.valueOf(parts[2].trim()
            .toUpperCase()
            .replace(" ", "_")
            .replace("-", "_")));
    }

    private List<PlantTag> parseTags(String rawTags) {
        List<PlantTag> tags = new ArrayList<>();
        if (!rawTags.equals("-") && !rawTags.isEmpty()) {
            // NEW: Strip out quotes from the CSV string!
            rawTags = rawTags.replace("\"", "");
            for (String t : rawTags.split(",")) {
                try {
                    tags.add(PlantTag.valueOf(t.trim().toUpperCase().replace(" ", "_")));
                } catch (IllegalArgumentException ignored) {
                    // Ignore invalid tags as per original implementation
                }
            }
        }
        return tags;
    }

    private void parseStats(PlantTemplate template, String[] parts) {
        template.setCost(Integer.parseInt(parts[4].trim()));
        template.setBaseHp(Integer.parseInt(parts[5].trim()));

        // Safely parse Base Damage to handle complex text formats
        String damageStr = parts[6].trim().toLowerCase();
        if (damageStr.equals("insta-kill")) {
            template.setInstantKill(true);
            template.setBaseDamage(0);
        } else if (damageStr.contains("x")) {
            template.setBaseDamage(Integer.parseInt(damageStr.split("x")[0].trim()));
        } else if (damageStr.contains("/")) {
            template.setBaseDamage(Integer.parseInt(damageStr.split("/")[0].trim()));
        } else {
            template.setBaseDamage(Integer.parseInt(damageStr));
            template.setInstantKill(false); // Just to be safe!
        }

        // Safely parse Action Interval Ticks to handle "-"
        String intervalStr = parts[12].trim();
        if (intervalStr.equals("-")) {
            template.setActionIntervalTicks(0);
        } else {
            template.setActionIntervalTicks(
                (int) Math.floor(Double.parseDouble(intervalStr) / Constants.Game.TIME_COEFFICIENT)
            );
        }

        // NEW: Clean and direct Recharge parsing!
        String rechargeStr = parts[13].trim();
        template.setRechargeTicks(
            (int) Math.floor(Double.parseDouble(rechargeStr) / Constants.Game.TIME_COEFFICIENT)
        );
    }

    private Map<Integer, UpgradeLevel> parseUpgradeMap(String[] parts) {
        Map<Integer, UpgradeLevel> upgradeMap = new HashMap<>();
        upgradeMap.put(2, parseUpgradeString(parts[9].trim()));
        upgradeMap.put(3, parseUpgradeString(parts[10].trim()));
        upgradeMap.put(4, parseUpgradeString(parts[11].trim()));
        return upgradeMap;
    }

    private void parseStrategies(PlantTemplate template, String[] parts) {
        String attackStr = parts[14].trim().toUpperCase();
        if (!attackStr.equals("NONE")) {
            template.setAttackStrategyType(AttackStrategyType.valueOf(attackStr));
        } else {
            // Explicitly setting NONE prevents the NullPointerException in the factory
            template.setAttackStrategyType(AttackStrategyType.NONE);
        }

        String foodStr = parts[15].trim().toUpperCase();
        if (!foodStr.equals("NONE")) {
            template.setFoodEffectType(PlantFoodEffectType.valueOf(foodStr));
        } else {
            // Explicitly setting NONE prevents future crashes for food effects
            template.setFoodEffectType(PlantFoodEffectType.NONE);
        }
    }


    private void applyDefaultValues(PlantTemplate template) {
        template.setProjectileType(NormalProjectile.class);
        template.setLaneOffsets(Collections.singletonList(0));

        // NEW: Default to shooting one standard projectile straight forward
        template.setShootVectors(Collections.singletonList(new double[]{1.0, 0.0, 0.0}));

        template.setRangeTiles(10.0);
        template.setFoodEffectValue(1);
    }

    public PlantTemplate getTemplate(String plantName) {
        return plantDatabase.get(plantName);
    }

    public PlantTemplate getTemplate(PlantType plantType) {
        return plantDatabase.get(plantType.getCommercialName());
    }

    private UpgradeLevel parseUpgradeString(String upgradeStr) {
        int hpBonus = 0;
        int damageBonus = 0;
        int costReduction = 0;
        double actionIntervalReductionTicks = 0.0;
        double growTimeReductionTicks = 0.0;
        double rechargeReductionTicks = 0.0;
        double chillTimeBonusTicks = 0.0;
        boolean doubleSun = false;
        boolean targetPriorityUp = false;
        int extraSunYield = 0;
        int pierceBonus = 0;
        double atkSpeedBonusPercentage = 0.0;
        int poisonDmgTickBonus = 0;
        double plantFoodChanceBonus = 0.0;
        double rangeBonus = 0.0;
        double lifespanBonusTicks = 0.0;
        double butterChanceBonus = 0.0;
        int aoeDamageBonus = 0;
        int warmthRadiusBonus = 0;
        double armTimeReductionTicks = 0.0;
        int extraCrushes = 0;
        int extraBounces = 0;
        int extraTargets = 0;
        double freezeTimeBonusTicks = 0.0;
        int maxSizeBonus = 0;
        boolean explodesOnDeath = false;
        boolean zombieHpBuff = false;
        boolean zombieDmgBuff = false;
        boolean plantFoodOnSpawn = false;
        boolean meltArea3x3 = false;
        double mintDurationBonusTicks = 0.0;
        boolean resetFamilyCooldowns = false;
        upgradeStr = upgradeStr.toLowerCase();

        // Standard Stats
        if (upgradeStr.contains("dmg +") || upgradeStr.contains("explode dmg +")) damageBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("target priority up")) targetPriorityUp = true;
        if (upgradeStr.contains("hp +")) hpBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("cost -")) costReduction = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("double sun")) doubleSun = true;
        if (upgradeStr.contains("sun +") || upgradeStr.contains("sun drop +")) extraSunYield = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("dmg/tick +")) poisonDmgTickBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("atk speed +")) atkSpeedBonusPercentage = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", ""));
        if (upgradeStr.contains("pierce +")) pierceBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("plant food chance +")) plantFoodChanceBonus = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", ""));
        if (upgradeStr.contains("range +")) rangeBonus = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", ""));
        if (upgradeStr.contains("lifespan +")) lifespanBonusTicks = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", "")) / Constants.Game.TIME_COEFFICIENT;
        if (upgradeStr.contains("butter +")) butterChanceBonus = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", ""));
        if (upgradeStr.contains("cooldown -")) rechargeReductionTicks = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", "")) / Constants.Game.TIME_COEFFICIENT;
        if (upgradeStr.contains("eat time -") || upgradeStr.contains("prod. time -") || upgradeStr.contains("charge time -") || upgradeStr.contains("regen -") || upgradeStr.contains("digest -")) actionIntervalReductionTicks = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", "")) / Constants.Game.TIME_COEFFICIENT;
        if (upgradeStr.contains("grow time -")) growTimeReductionTicks = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", "")) / Constants.Game.TIME_COEFFICIENT;
        if (upgradeStr.contains("chill time +")) chillTimeBonusTicks = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", "")) / Constants.Game.TIME_COEFFICIENT;
        if (upgradeStr.contains("aoe dmg +")) aoeDamageBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("warmth radius +")) warmthRadiusBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("arm time -")) armTimeReductionTicks = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", "")) / Constants.Game.TIME_COEFFICIENT;
        if (upgradeStr.contains("crush 2x")) extraCrushes = 1;
        if (upgradeStr.contains("bounces +")) extraBounces = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("targets +")) extraTargets = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("freeze time +")) freezeTimeBonusTicks = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", "")) / Constants.Game.TIME_COEFFICIENT;
        if (upgradeStr.contains("max size +")) maxSizeBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("aoe on death") || upgradeStr.contains("explode on finish")) explodesOnDeath = true;
        if (upgradeStr.contains("zombie hp buff")) zombieHpBuff = true;
        if (upgradeStr.contains("zombie dmg buff")) zombieDmgBuff = true;
        if (upgradeStr.contains("plant food on enter")) plantFoodOnSpawn = true;
        if (upgradeStr.contains("melt area")) meltArea3x3 = true;
        if (upgradeStr.contains("duration +")) mintDurationBonusTicks = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", "")) * 60.0;
        if (upgradeStr.contains("reset family")) resetFamilyCooldowns = true;

        return new UpgradeLevel(hpBonus, damageBonus, costReduction, actionIntervalReductionTicks,
            rechargeReductionTicks, doubleSun, growTimeReductionTicks, extraSunYield, chillTimeBonusTicks,
            targetPriorityUp, pierceBonus, atkSpeedBonusPercentage, poisonDmgTickBonus, plantFoodChanceBonus,
            rangeBonus, lifespanBonusTicks, butterChanceBonus, aoeDamageBonus, warmthRadiusBonus,
            armTimeReductionTicks,extraCrushes, extraBounces, extraTargets, freezeTimeBonusTicks,
            maxSizeBonus, explodesOnDeath, zombieHpBuff, zombieDmgBuff, plantFoodOnSpawn,
            meltArea3x3, mintDurationBonusTicks, resetFamilyCooldowns);
    }

    private void assignSpecificParameters(PlantTemplate t) {
        String name = t.getName();

        // 1. Specific Projectiles & Payloads
        if (name.equals("Snow Pea") || name.equals("Ice-shroom")) {
            t.setProjectileType(IceProjectile.class);
        } else if (name.equals("Fire Peashooter")) {
            t.setProjectileType(FireProjectile.class);
        } else if (name.equals("Goo Peashooter")) {
            t.setProjectileType(PoisonProjectile.class);
        } else if (name.equals("Cabbage-pult") || name.equals("Melon-pult") || name.equals("Winter Melon") || name.equals("Pepper-pult") || name.equals("Kernel-pult")) {
            if (name.equals("Winter Melon")) {
                t.setProjectileType(IceLobbedProjectile.class);
            } else if (name.equals("Pepper-pult")) {
                t.setProjectileType(FireLobbedProjectile.class);
            } else {
                t.setProjectileType(LobbedProjectile.class);
            }
            // 2. Melon-pults get 1.5 tiles of splash damage. The others get 0.0 (single target).
            t.setRangeTiles(name.equals("Melon-pult") || name.equals("Winter Melon")  || name.equals("Pepper-pult") ? 1.5 : 0.0);
        }
        else if (name.equals("Potato Mine") || name.equals("Primal Potato Mine") || name.equals("Iceberg Lettuce")) {
            t.setRangeTiles(name.equals("Primal Potato Mine") ? 1.5 : 0.0);

            if (!name.equals("Iceberg Lettuce")) {
                t.setFoodEffectValue(2); // Clones for Potatoes
            } else {
                t.setFoodEffectValue(150); // freezing the whole map for 15 seconds.
            }
        }
        else if (name.equals("Cherry Bomb") || name.equals("Grapeshot")) {t.setRangeTiles(1.5);} // 3x3 Splash Damage
        if (name.equals("Doom-shroom")) { t.setRangeTiles(2.5); } // Massive 5x5 area
        else if (name.equals("Jalapeno")) { t.setRangeTiles(0.0); } // Lane-wide math

        else if (name.equals("Cactus")) {
            t.setProjectileType(PiercingProjectile.class);
        } else if (name.equals("Fume-shroom")) {
            t.setProjectileType(FumeProjectile.class);
            t.setRangeTiles(5.0);
        } else if (name.equals("Bowling Bulb")) {
            t.setProjectileType(BouncingProjectile.class);
        } else if (name.equals("Caulipower") || name.equals("Electric Blueberry") || name.equals("Electric Redberry") || name.equals("Cat-tail")) {
            t.setProjectileType(HomingProjectile.class);
        }
        if (name.equals("Puff-shroom") || name.equals("Sea-shroom")) {
            t.setRangeTiles(3.0); // Base short range!
        }

        // 2. Vector Routing & Physics (Replaces the obsolete projectileCount!)
        if (name.equals("Repeater")) {
            t.setShootVectors(Arrays.asList(new double[]{1.0, 0.0, 0.0}, new double[]{1.0, 0.0, 1.0}));
        } else if (name.equals("Split Pea")) {
            t.setShootVectors(Arrays.asList(new double[]{1.0, 0.0, 0.0}, new double[]{-1.0, 0.0, 0.0}, new double[]{-1.0, 0.0, 1.0}));
        } else if (name.equals("Mega Gatling Pea")) {
            t.setShootVectors(Arrays.asList(
                new double[]{1.0, 0.0, 0.0}, // Pea 1
                new double[]{1.0, 0.0, 1.0}, // Pea 2 (staggered slightly)
                new double[]{1.0, 0.0, 2.0}, // Pea 3 (staggered more)
                new double[]{1.0, 0.0, 3.0}  // Pea 4 (staggered most)
            ));
        } else if (name.equals("Threepeater")) {
            t.setLaneOffsets(Arrays.asList(1, 0, -1));
            t.setShootVectors(Collections.singletonList(new double[]{1.0, 0.0, 0.0}));
        } else if (name.equals("Rotobaga")) {
            List<double[]> rotoVectors = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                rotoVectors.add(new double[]{1.0, 1.0, i});
                rotoVectors.add(new double[]{1.0, -1.0, i});
                rotoVectors.add(new double[]{-1.0, 1.0, i});
                rotoVectors.add(new double[]{-1.0, -1.0, i});
            }
            t.setShootVectors(rotoVectors);
        } else if (name.equals("Starfruit")) { // NEW: Starfruit 5-way vectors
            t.setShootVectors(Arrays.asList(
                new double[]{-1.0, 0.0, 0.0},  // Backward
                new double[]{0.0, -1.0, 0.0},  // Up
                new double[]{0.0, 1.0, 0.0},   // Down
                new double[]{1.0, -0.5, 0.0},  // Up-Forward
                new double[]{1.0, 0.5, 0.0}    // Down-Forward
            ));
        }



// 3. Special Values /////////////////----> check this matter bro <---- ////////////////////////
        if (name.equals("Bonk Choy") || name.equals("Phat Beet") || name.equals("Wasabi Whip") || name.equals("Kiwibeast")) t.setRangeTiles(1.5); // Reaches 1 tile ahead and 1 behind
        if (name.equals("Chomper")) {
            t.setRangeTiles(0.7);
            t.setFoodEffectValue(3); // Devours exactly 3 random zombies across the map!
        }
        if (name.equals("Wall-nut") || name.equals("Explode-o-nut") || name.equals("Pumpkin")) t.setFoodEffectValue(4000);
        if (name.equals("Tall-nut")) t.setFoodEffectValue(8000);
        if (name.equals("Endurian") || name.equals("Sweet Potato")) t.setFoodEffectValue(3000);
    }
}
