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

                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length < 16) continue;

                try {
                    PlantTemplate template = new PlantTemplate();

                    template.setName(parts[1].trim());
                    template.setCategory(PlantCategory.valueOf(parts[2].trim()
                        .toUpperCase()
                        .replace(" ", "_")
                        .replace("-", "_")));

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

                    template.setCost(Integer.parseInt(parts[4].trim()));
                    template.setBaseHp(Integer.parseInt(parts[5].trim()));
                    template.setBaseDamage(Integer.parseInt(parts[6].trim()));
                    template.setActionIntervalTicks(Double.parseDouble(parts[12].trim()) * 10.0);

                    Map<Integer, UpgradeLevel> upgradeMap = new HashMap<>();
                    upgradeMap.put(2, parseUpgradeString(parts[9].trim()));
                    upgradeMap.put(3, parseUpgradeString(parts[10].trim()));
                    upgradeMap.put(4, parseUpgradeString(parts[11].trim()));
                    template.setUpgradeMap(upgradeMap);

                    String attackStr = parts[14].trim().toUpperCase();
                    if (!attackStr.equals("NONE") && !attackStr.equals("-")) {
                        template.setAttackStrategyType(AttackStrategyType.valueOf(attackStr));
                    }

                    String foodStr = parts[15].trim().toUpperCase();
                    if (!foodStr.equals("NONE") && !foodStr.equals("-")) {
                        template.setFoodEffectType(PlantFoodEffectType.valueOf(foodStr));
                    }

                    template.setProjectileType(NormalProjectile.class);
                    template.setLaneOffsets(Collections.singletonList(0));
                    template.setProjectileCount(1);
                    template.setRangeTiles(3.0);
                    template.setFoodEffectValue(1);

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

        return new UpgradeLevel(hpBonus, damageBonus, costReduction, speedBonus, doubleSun);
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
        } else if (name.equals("Cabbage-pult") || name.equals("Melon-pult") || name.equals("Winter Melon") || name.equals("Pepper-pult") || name.equals("Kernel-pult")) {
            t.setProjectileType(name.equals("Kernel-pult") ? ButterProjectile.class : LobbedProjectile.class);
            t.setRangeTiles(name.equals("Melon-pult") || name.equals("Winter Melon") ? 1.5 : 0.0);
        } else if (name.equals("Cactus") || name.equals("Fume-shroom")) {
            t.setProjectileType(PiercingProjectile.class);
        } else if (name.equals("Bowling Bulb")) {
            t.setProjectileType(BouncingProjectile.class);
        }

        // 2. Multi-Lane Offsets & Projectile Counts
        if (name.equals("Repeater") || name.equals("Split Pea")) {
            t.setProjectileCount(2);
        } else if (name.equals("Mega Gatling Pea")) {
            t.setProjectileCount(4);
        } else if (name.equals("Threepeater")) {
            t.setProjectileCount(1);
            t.setLaneOffsets(Arrays.asList(1, 0, -1)); // Up, Current, Down
        }

        // 3. Special Values (Sun Yield, Armor HP, Freeze Timers)
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
