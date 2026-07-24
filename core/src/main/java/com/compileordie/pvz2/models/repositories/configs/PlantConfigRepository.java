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
                    continue; // Skip the header row
                }

                // Regex to handle commas inside quotes
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length < 16) continue;

                try {
                    PlantTemplate template = new PlantTemplate();

                    // 1. Core Strings & Enums
                    template.setName(parts[1].trim());
                    template.setCategory(PlantCategory.valueOf(parts[2].trim()
                        .toUpperCase()
                        .replace(" ", "_")
                        .replace("-", "_")));

                    // 2. Tags parsing
                    List<PlantTag> tags = new ArrayList<>();
                    String rawTags = parts[3].trim();
                    if (!rawTags.equals("-") && !rawTags.isEmpty()) {
                        for (String t : rawTags.split(",")) {
                            try {
                                tags.add(PlantTag.valueOf(t.trim().toUpperCase().replace(" ", "_")));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                    template.setTags(tags);

                    // 3. Base Stats
                    template.setCost(Integer.parseInt(parts[4].trim()));
                    template.setBaseHp(Integer.parseInt(parts[5].trim()));
                    template.setBaseDamage(Integer.parseInt(parts[6].trim()));
                    // Convert seconds to ticks (assuming 60 TPS)
                    template.setActionIntervalTicks(Double.parseDouble(parts[12].trim()) * 60.0);

                    // 4. Upgrade Parsing (Basic parser for Lvl 2, Lvl 3, Lvl 4 strings)
                    Map<Integer, UpgradeLevel> upgradeMap = new HashMap<>();
                    upgradeMap.put(2, parseUpgradeString(parts[9].trim()));
                    upgradeMap.put(3, parseUpgradeString(parts[10].trim()));
                    upgradeMap.put(4, parseUpgradeString(parts[11].trim()));
                    template.setUpgradeMap(upgradeMap);

                    // 5. Strategies
                    String attackStr = parts[14].trim().toUpperCase();
                    if (!attackStr.equals("NONE") && !attackStr.equals("-")) {
                        template.setAttackStrategyType(AttackStrategyType.valueOf(attackStr));
                    }

                    String foodStr = parts[15].trim().toUpperCase();
                    if (!foodStr.equals("NONE") && !foodStr.equals("-")) {
                        template.setFoodEffectType(PlantFoodEffectType.valueOf(foodStr));
                    }

                    // Default values before routing
                    template.setProjectileType(NormalProjectile.class);
                    template.setLaneOffsets(Collections.singletonList(0));
                    template.setProjectileCount(1);
                    template.setRangeTiles(3.0);
                    template.setFoodEffectValue(1);

                    // 6. Hardcode specific mechanical parameters based on plant name
                    assignSpecificParameters(template);

                    plantDatabase.put(template.getName(), template);

                } catch (Exception e) {
                    System.err.println("Error parsing row for plant: " + parts[1]);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlantTemplate getTemplate(String plantName) {
        return plantDatabase.get(plantName);
    }

    /**
     * Parses the upgrade string (e.g. "Cost -25" or "HP +150") into a usable object.
     */
    private UpgradeLevel parseUpgradeString(String upgradeStr) {
        int hpBonus = 0;
        int damageBonus = 0;
        int costReduction = 0;
        double speedBonus = 0.0;
        boolean doubleSun = false;

        upgradeStr = upgradeStr.toLowerCase();
        if (upgradeStr.contains("hp +")) hpBonus = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("cost -")) costReduction = Integer.parseInt(upgradeStr.replaceAll("[^0-9]", ""));
        if (upgradeStr.contains("double sun")) doubleSun = true;
        // Add more parsing logic based on your exact upgrade text

        return new UpgradeLevel(hpBonus, damageBonus, costReduction, speedBonus, doubleSun);
    }

    /**
     * Because the CSV doesn't specify if a plant shoots fire, ice, or three lanes at once,
     * this router fills in those gaps for the factories.
     */
    private void assignSpecificParameters(PlantTemplate t) {
        String name = t.getName();

        // Specific Projectiles
        if (name.equals("Snow Pea") || name.equals("Winter Melon")) {
            t.setProjectileType(IceProjectile.class);
        } else if (name.equals("Fire Peashooter")) {
            t.setProjectileType(FireProjectile.class);
        } else if (name.equals("Goo Peashooter")) {
            t.setProjectileType(PoisonProjectile.class);
        } else if (name.equals("Cabbage-pult") || name.equals("Melon-pult")) {
            t.setProjectileType(LobbedProjectile.class);
            t.setRangeTiles(name.equals("Melon-pult") ? 1.5 : 0.0); // Splash radius
        } else if (name.equals("Kernel-pult")) {
            t.setProjectileType(ButterProjectile.class);
        } else if (name.equals("Cactus")) {
            t.setProjectileType(PiercingProjectile.class);
        } else if (name.equals("Bowling Bulb")) {
            t.setProjectileType(BouncingProjectile.class);
        }

        // Multi-Lane Offsets
        if (name.equals("Threepeater")) {
            t.setLaneOffsets(Arrays.asList(-1, 0, 1)); // Shoots up, current, down
        } else if (name.equals("Starfruit") || name.equals("Rotobaga")) {
            // Depending on how you handle diagonals, you might pass specific offsets
            t.setLaneOffsets(Arrays.asList(-1, 0, 1));
        } else if (name.equals("Split Pea")) {
            t.setLaneOffsets(Arrays.asList(0, 0)); // Two offsets, you can handle reverse inside the strategy
        }

        // Projectile Counts
        if (name.equals("Repeater")) {
            t.setProjectileCount(2);
        } else if (name.equals("Mega Gatling Pea")) {
            t.setProjectileCount(4);
        } else if (name.equals("Pea Pod")) {
            // Initially 1, upgrades dynamically in gameplay
            t.setProjectileCount(1);
        }

        // Specific Food Effect Values (e.g. how much sun dropped, how long to freeze)
        if (name.equals("Sunflower")) t.setFoodEffectValue(150);
        if (name.equals("Twin Sunflower")) t.setFoodEffectValue(250);
        if (name.equals("Iceberg Lettuce")) t.setFoodEffectValue(300); // 5 seconds freeze (60 ticks * 5)
        if (name.equals("Wall-nut")) t.setFoodEffectValue(4000); // Bonus armor HP
        if (name.equals("Tall-nut")) t.setFoodEffectValue(8000); // Bonus armor HP
    }
}
