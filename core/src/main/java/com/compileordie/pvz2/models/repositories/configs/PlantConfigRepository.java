package com.compileordie.pvz2.models.repositories.configs;

import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.UpgradeLevel;
import com.compileordie.pvz2.models.entities.plants.enums.AttackStrategyType;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.plants.enums.PlantFoodEffectType;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.projectiles.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class PlantConfigRepository {

    private final Map<String, PlantTemplate> plantDatabase = new HashMap<>();

    public void loadFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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
            template.setBaseDamage(9999);
            template.setInstantKill(true);
        } else if (damageStr.equals("-")) {
            template.setBaseDamage(0);
        } else if (damageStr.contains("x")) {
            template.setBaseDamage(Integer.parseInt(damageStr.split("x")[0].trim()));
        } else if (damageStr.contains("/")) {
            template.setBaseDamage(Integer.parseInt(damageStr.split("/")[0].trim()));
        } else {
            template.setBaseDamage(Integer.parseInt(damageStr));
        }

        // Safely parse Action Interval Ticks to handle "-"
        String intervalStr = parts[12].trim();
        if (intervalStr.equals("-")) {
            template.setActionIntervalTicks(0.0);
        } else {
            template.setActionIntervalTicks(Double.parseDouble(intervalStr) * 10.0);
        }

        // NEW: Clean and direct Recharge parsing!
        String rechargeStr = parts[13].trim();
        template.setRechargeTicks(Double.parseDouble(rechargeStr) * 10.0);
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

    private UpgradeLevel parseUpgradeString(String upgradeStr) {
        int hpBonus = 0;
        int damageBonus = 0;
        int costReduction = 0;
        double actionIntervalReductionTicks = 0.0;
        double growTimeReductionTicks = 0.0;
        double rechargeReductionTicks = 0.0;
        double chillTimeBonusTicks = 0.0; // NEW
        boolean doubleSun = false;
        boolean targetPriorityUp = false;
        int extraSunYield = 0;
        int pierceBonus = 0;
        double atkSpeedBonusPercentage = 0.0;
        int poisonDmgTickBonus = 0;
        double plantFoodChanceBonus = 0.0;
        double rangeBonus = 0.0;
        double lifespanBonusTicks = 0.0;
        upgradeStr = upgradeStr.toLowerCase();

        // NEW: Catch Damage bonuses!
        if (upgradeStr.contains("dmg +")) damageBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("target priority up")) targetPriorityUp = true;
        if (upgradeStr.contains("hp +")) hpBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("cost -")) costReduction = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("double sun")) doubleSun = true;
        if (upgradeStr.contains("sun +")) extraSunYield = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("dmg/tick +")) poisonDmgTickBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        // Add parsing for time-based stats
        if (upgradeStr.contains("time +") || upgradeStr.contains("time -") || upgradeStr.contains("cooldown -")) {
            double timeValue = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", "")) * 10.0;

            if (upgradeStr.contains("cooldown -")) rechargeReductionTicks = timeValue;
            if (upgradeStr.contains("prod. time -") || upgradeStr.contains("charge time -") || upgradeStr.contains("regen -")) actionIntervalReductionTicks = timeValue;
            if (upgradeStr.contains("grow time -")) growTimeReductionTicks = timeValue;
            if (upgradeStr.contains("atk speed +")) atkSpeedBonusPercentage = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", ""));
            // NEW: Catch Chill Time bonus!
            if (upgradeStr.contains("chill time +")) chillTimeBonusTicks = timeValue;
            if (upgradeStr.contains("pierce +")) pierceBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
            if (upgradeStr.contains("plant food chance +")) plantFoodChanceBonus = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", ""));
            if (upgradeStr.contains("range +")) rangeBonus = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", ""));
            if (upgradeStr.contains("lifespan +")) lifespanBonusTicks = Double.parseDouble(upgradeStr.replaceAll("[^0-9.]", "")) * 10.0;
        }

        // NOTE: Make sure your UpgradeLevel constructor accepts chillTimeBonusTicks as the 9th parameter!
        return new UpgradeLevel(hpBonus, damageBonus, costReduction, actionIntervalReductionTicks, rechargeReductionTicks, doubleSun, growTimeReductionTicks, extraSunYield, chillTimeBonusTicks, targetPriorityUp, pierceBonus, atkSpeedBonusPercentage, poisonDmgTickBonus, plantFoodChanceBonus, rangeBonus, lifespanBonusTicks);
    }

    private void assignSpecificParameters(PlantTemplate t) {
        String name = t.getName();

        // 1. Specific Projectiles & Payloads
        if (name.equals("Snow Pea") || name.equals("Winter Melon") || name.equals("Ice-shroom")) {
            t.setProjectileType(IceProjectile.class);
        } else if (name.equals("Fire Peashooter") || name.equals("Pepper-pult")) {
            t.setProjectileType(FireProjectile.class);
        } else if (name.equals("Goo Peashooter")) {
            t.setProjectileType(PoisonProjectile.class);
        } else if (name.equals("Cabbage-pult") || name.equals("Melon-pult") || name.equals("Winter Melon") ||
            name.equals("Pepper-pult") || name.equals("Kernel-pult")) {
            t.setProjectileType(name.equals("Kernel-pult") ? ButterProjectile.class : LobbedProjectile.class);
            t.setRangeTiles(name.equals("Melon-pult") || name.equals("Winter Melon") ? 1.5 : 0.0);
        } else if (name.equals("Cactus")) {
            t.setProjectileType(PiercingProjectile.class);
        }else if (name.equals("Fume-shroom")) {
            // NEW: Fume-shroom gets its own projectile and a base range of 5!
            t.setProjectileType(com.compileordie.pvz2.models.entities.projectiles.FumeProjectile.class);
            t.setRangeTiles(5.0);
        } else if (name.equals("Bowling Bulb")) {
            t.setProjectileType(BouncingProjectile.class);
        } else if (name.equals("Caulipower") || name.equals("Electric Blueberry")) {
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


        // 3. Special Values
        if (name.equals("Sunflower") || name.equals("Sun Bean")) t.setFoodEffectValue(50);
        if (name.equals("Primal Sunflower")) t.setFoodEffectValue(75);
        if (name.equals("Twin Sunflower")) t.setFoodEffectValue(100);
        if (name.equals("Gold Bloom")) t.setFoodEffectValue(375);
        if (name.equals("Iceberg Lettuce")) t.setFoodEffectValue(50);
        if (name.equals("Wall-nut") || name.equals("Explode-o-nut") || name.equals("Pumpkin")) t.setFoodEffectValue(4000);
        if (name.equals("Endurian") || name.equals("Sweet Potato")) t.setFoodEffectValue(3000);
        if (name.equals("Tall-nut")) t.setFoodEffectValue(8000);
    }
}
