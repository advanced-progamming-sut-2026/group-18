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
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length < 16) {
                    continue;
                }
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
        String categoryStr = parts[2].trim().toUpperCase().replace(" ", "_").replace("-", "_");
        template.setCategory(PlantCategory.valueOf(categoryStr));
    }

    private List<PlantTag> parseTags(String rawTags) {
        List<PlantTag> tags = new ArrayList<>();
        if (!rawTags.equals("-") && !rawTags.isEmpty()) {
            rawTags = rawTags.replace("\"", "");
            for (String t : rawTags.split(",")) {
                try {
                    tags.add(PlantTag.valueOf(t.trim().toUpperCase().replace(" ", "_")));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return tags;
    }

    private void parseStats(PlantTemplate template, String[] parts) {
        template.setCost(Integer.parseInt(parts[4].trim()));
        template.setBaseHp(Integer.parseInt(parts[5].trim()));
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
            template.setInstantKill(false);
        }
        String intervalStr = parts[12].trim();
        if (intervalStr.equals("-")) {
            template.setActionIntervalTicks(0);
        } else {
            template.setActionIntervalTicks(
                (int) Math.floor(Double.parseDouble(intervalStr) / Constants.Game.TIME_COEFFICIENT));
        }
        String rechargeStr = parts[13].trim();
        template.setRechargeTicks(
            (int) Math.floor(Double.parseDouble(rechargeStr) / Constants.Game.TIME_COEFFICIENT));
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
            template.setAttackStrategyType(AttackStrategyType.NONE);
        }
        String foodStr = parts[15].trim().toUpperCase();
        if (!foodStr.equals("NONE")) {
            template.setFoodEffectType(PlantFoodEffectType.valueOf(foodStr));
        } else {
            template.setFoodEffectType(PlantFoodEffectType.NONE);
        }
    }

    private void applyDefaultValues(PlantTemplate template) {
        template.setProjectileType(NormalProjectile.class);
        template.setLaneOffsets(Collections.singletonList(0));
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
        int hpBonus = 0, damageBonus = 0, costReduction = 0, extraSunYield = 0, pierceBonus = 0;
        int poisonDmgTickBonus = 0, aoeDamageBonus = 0, warmthRadiusBonus = 0, extraCrushes = 0;
        int extraBounces = 0, extraTargets = 0, maxSizeBonus = 0;
        double actionIntervalReductionTicks = 0.0, growTimeReductionTicks = 0.0, rechargeReductionTicks = 0.0;
        double chillTimeBonusTicks = 0.0, atkSpeedBonusPercentage = 0.0, plantFoodChanceBonus = 0.0;
        double rangeBonus = 0.0, lifespanBonusTicks = 0.0, butterChanceBonus = 0.0, armTimeReductionTicks = 0.0;
        double freezeTimeBonusTicks = 0.0, mintDurationBonusTicks = 0.0;
        boolean doubleSun = false, targetPriorityUp = false, explodesOnDeath = false, zombieHpBuff = false;
        boolean zombieDmgBuff = false, plantFoodOnSpawn = false, meltArea3x3 = false, resetFamilyCooldowns = false;

        String s = upgradeStr.toLowerCase();
        if (s.contains("dmg +") || s.contains("explode dmg +")) damageBonus = extractInt(s);
        if (s.contains("target priority up")) targetPriorityUp = true;
        if (s.contains("hp +")) hpBonus = extractInt(s);
        if (s.contains("cost -")) costReduction = extractInt(s);
        if (s.contains("double sun")) doubleSun = true;
        if (s.contains("sun +") || s.contains("sun drop +")) extraSunYield = extractInt(s);
        if (s.contains("dmg/tick +")) poisonDmgTickBonus = extractInt(s);
        if (s.contains("atk speed +")) atkSpeedBonusPercentage = extractDouble(s);
        if (s.contains("pierce +")) pierceBonus = extractInt(s);
        if (s.contains("plant food chance +")) plantFoodChanceBonus = extractDouble(s);
        if (s.contains("range +")) rangeBonus = extractDouble(s);
        if (s.contains("lifespan +")) lifespanBonusTicks = extractTimeInTicks(s);
        if (s.contains("butter +")) butterChanceBonus = extractDouble(s);
        if (s.contains("cooldown -")) rechargeReductionTicks = extractTimeInTicks(s);
        if (s.contains("eat time -") || s.contains("prod. time -") || s.contains("charge time -")
            || s.contains("regen -") || s.contains("digest -")) {
            actionIntervalReductionTicks = extractTimeInTicks(s);
        }
        if (s.contains("grow time -")) growTimeReductionTicks = extractTimeInTicks(s);
        if (s.contains("chill time +")) chillTimeBonusTicks = extractTimeInTicks(s);
        if (s.contains("aoe dmg +")) aoeDamageBonus = extractInt(s);
        if (s.contains("warmth radius +")) warmthRadiusBonus = extractInt(s);
        if (s.contains("arm time -")) armTimeReductionTicks = extractTimeInTicks(s);
        if (s.contains("crush 2x")) extraCrushes = 1;
        if (s.contains("bounces +")) extraBounces = extractInt(s);
        if (s.contains("targets +")) extraTargets = extractInt(s);
        if (s.contains("freeze time +")) freezeTimeBonusTicks = extractTimeInTicks(s);
        if (s.contains("max size +")) maxSizeBonus = extractInt(s);
        if (s.contains("aoe on death") || s.contains("explode on finish")) explodesOnDeath = true;
        if (s.contains("zombie hp buff")) zombieHpBuff = true;
        if (s.contains("zombie dmg buff")) zombieDmgBuff = true;
        if (s.contains("plant food on enter")) plantFoodOnSpawn = true;
        if (s.contains("melt area")) meltArea3x3 = true;
        if (s.contains("duration +")) mintDurationBonusTicks = extractDouble(s) * 60.0;
        if (s.contains("reset family")) resetFamilyCooldowns = true;

        return new UpgradeLevel(
            hpBonus, damageBonus, costReduction, actionIntervalReductionTicks, rechargeReductionTicks,
            doubleSun, growTimeReductionTicks, extraSunYield, chillTimeBonusTicks, targetPriorityUp,
            pierceBonus, atkSpeedBonusPercentage, poisonDmgTickBonus, plantFoodChanceBonus, rangeBonus,
            lifespanBonusTicks, butterChanceBonus, aoeDamageBonus, warmthRadiusBonus, armTimeReductionTicks,
            extraCrushes, extraBounces, extraTargets, freezeTimeBonusTicks, maxSizeBonus, explodesOnDeath,
            zombieHpBuff, zombieDmgBuff, plantFoodOnSpawn, meltArea3x3, mintDurationBonusTicks,
            resetFamilyCooldowns
        );
    }

    private int extractInt(String s) {
        return Integer.parseInt(s.replaceAll("[^0-9]", ""));
    }

    private double extractDouble(String s) {
        return Double.parseDouble(s.replaceAll("[^0-9.]", ""));
    }

    private double extractTimeInTicks(String s) {
        return extractDouble(s) / Constants.Game.TIME_COEFFICIENT;
    }

    private void assignSpecificParameters(PlantTemplate t) {
        String name = t.getName();
        if (name.equals("Snow Pea") || name.equals("Ice-shroom")) {
            t.setProjectileType(IceProjectile.class);
        } else if (name.equals("Fire Peashooter")) {
            t.setProjectileType(FireProjectile.class);
        } else if (name.equals("Goo Peashooter")) {
            t.setProjectileType(PoisonProjectile.class);
        } else if (name.equals("Cabbage-pult") || name.equals("Melon-pult") || name.equals("Winter Melon")
            || name.equals("Pepper-pult") || name.equals("Kernel-pult")) {
            if (name.equals("Winter Melon")) {
                t.setProjectileType(IceLobbedProjectile.class);
            } else if (name.equals("Pepper-pult")) {
                t.setProjectileType(FireLobbedProjectile.class);
            } else {
                t.setProjectileType(LobbedProjectile.class);
            }
            t.setRangeTiles(name.equals("Melon-pult") || name.equals("Winter Melon")
                || name.equals("Pepper-pult") ? 1.5 : 0.0);
        } else if (name.equals("Potato Mine") || name.equals("Primal Potato Mine")
            || name.equals("Iceberg Lettuce")) {
            t.setRangeTiles(name.equals("Primal Potato Mine") ? 1.5 : 0.0);
            if (!name.equals("Iceberg Lettuce")) {
                t.setFoodEffectValue(2);
            } else {
                t.setFoodEffectValue(150);
            }
        } else if (name.equals("Cherry Bomb") || name.equals("Grapeshot")) {
            t.setRangeTiles(1.5);
        }
        if (name.equals("Doom-shroom")) {
            t.setRangeTiles(2.5);
        } else if (name.equals("Jalapeno")) {
            t.setRangeTiles(0.0);
        } else if (name.equals("Cactus")) {
            t.setProjectileType(PiercingProjectile.class);
        } else if (name.equals("Fume-shroom")) {
            t.setProjectileType(FumeProjectile.class);
            t.setRangeTiles(5.0);
        } else if (name.equals("Bowling Bulb")) {
            t.setProjectileType(BouncingProjectile.class);
        } else if (name.equals("Caulipower") || name.equals("Electric Blueberry")
            || name.equals("Electric Redberry") || name.equals("Cat-tail")) {
            t.setProjectileType(HomingProjectile.class);
        }
        if (name.equals("Puff-shroom") || name.equals("Sea-shroom")) {
            t.setRangeTiles(3.0);
        }
        if (name.equals("Repeater")) {
            t.setShootVectors(Arrays.asList(new double[]{1.0, 0.0, 0.0}, new double[]{1.0, 0.0, 1.0}));
        } else if (name.equals("Split Pea")) {
            t.setShootVectors(Arrays.asList(
                new double[]{1.0, 0.0, 0.0}, new double[]{-1.0, 0.0, 0.0}, new double[]{-1.0, 0.0, 1.0}));
        } else if (name.equals("Mega Gatling Pea")) {
            t.setShootVectors(Arrays.asList(
                new double[]{1.0, 0.0, 0.0}, new double[]{1.0, 0.0, 1.0},
                new double[]{1.0, 0.0, 2.0}, new double[]{1.0, 0.0, 3.0}));
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
        } else if (name.equals("Starfruit")) {
            t.setShootVectors(Arrays.asList(
                new double[]{-1.0, 0.0, 0.0}, new double[]{0.0, -1.0, 0.0},
                new double[]{0.0, 1.0, 0.0}, new double[]{1.0, -0.5, 0.0}, new double[]{1.0, 0.5, 0.0}));
        }
        if (name.equals("Bonk Choy") || name.equals("Phat Beet")
            || name.equals("Wasabi Whip") || name.equals("Kiwibeast")) {
            t.setRangeTiles(1.5);
        }
        if (name.equals("Chomper")) {
            t.setRangeTiles(0.7);
            t.setFoodEffectValue(3);
        }
        if (name.equals("Wall-nut") || name.equals("Explode-o-nut") || name.equals("Pumpkin")) {
            t.setFoodEffectValue(4000);
        }
        if (name.equals("Tall-nut")) {
            t.setFoodEffectValue(8000);
        }
        if (name.equals("Endurian") || name.equals("Sweet Potato")) {
            t.setFoodEffectValue(3000);
        }
        if (name.equals("Caulipower") || name.equals("Electric Blueberry")) {
            t.setFoodEffectValue(3);
        }
    }
}
