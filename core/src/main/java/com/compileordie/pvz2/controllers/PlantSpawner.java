package com.compileordie.pvz2.controllers;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.plants.factory.PlantFactory;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.repositories.configs.PlantConfigRepository;

public class PlantSpawner {

    // Don't load the CSV every time you spawn a plant! It causes lag.
    // Load this once when the game starts and store it as a static variable.
    private static PlantConfigRepository repository;
    public static void initRepository() {
        if (repository == null) {
            repository = new PlantConfigRepository();
            repository.loadFromCSV(Constants.Paths.Configs.PLANTS);
        }
    }

    // NEW: Exposes the parsed plant metadata (category/tags/cost/...) for a given
    // PlantType without needing to spawn an actual Plant instance. Used by the quest
    // system to figure out things like "is this a sun-producing plant?" purely from
    // the PlantType that was planted or that scored a kill.
    public static PlantTemplate getTemplate(PlantType plantType) {
        initRepository();
        return plantType != null ? repository.getTemplate(plantType) : null;
    }

    /**
     * Checks whether a plant belongs to a given "family" string, as used by the
     * Family Slayer / Flourishing in Limits quests (e.g. "PEA", "MINT", "ARMAMINT").
     * A family can be satisfied in three ways, checked in order:
     *   1. It matches the plant's PlantCategory (e.g. "MINT" -> PlantCategory.MINT).
     *   2. It matches one of the plant's PlantTags (e.g. "PEA" -> PlantTag.PEA).
     *   3. As a fallback, it appears as a substring of the plant's enum identifier
     *      with underscores stripped (e.g. "ARMAMINT" -> ARMA_MINT), which lets
     *      bespoke, single-plant "families" work without needing their own Category/Tag.
     */
    public static boolean matchesFamily(PlantType plantType, String family) {
        if (plantType == null || family == null) return false;
        String fam = family.trim().toUpperCase();

        PlantTemplate template = getTemplate(plantType);
        if (template != null) {
            if (template.getCategory() != null && template.getCategory().name().equals(fam)) {
                return true;
            }
            if (template.getTags() != null) {
                for (PlantTag tag : template.getTags()) {
                    if (tag.name().equals(fam)) return true;
                }
            }
        }

        return plantType.name().replace("_", "").contains(fam);
    }

    public static Plant spawn(PlantType plantType, double x, double y, boolean isBoosted, boolean isSpecial) {
        initRepository(); // Ensures CSV is loaded

        // 1. Build the fresh Level 1 blueprint
        Plant newPlant = PlantFactory.createPlant(repository.getTemplate(plantType), x, y);

        // 2. Fetch the player's persistent plant level from memory!
        int targetLevel = 1; // Default
        if (AppModel.player != null && AppModel.player.plantLevels.containsKey(plantType)) {
            targetLevel = AppModel.player.plantLevels.get(plantType);
        }

        // 3. Inject the cumulative upgrades using our new loop logic!
        if (targetLevel > 1) {
            newPlant.applyLevelUpgrade(targetLevel);
        }

        // 4. (Optional) TODO: Apply isBoosted logic here if the player bought a boost!
        if (isBoosted) {
             newPlant.applyBoost();
        }

        if (isSpecial) {
            newPlant.setSpecial();
        }

        return newPlant;
    }

    public static Plant spawn(PlantType plantType) {
        return spawn(plantType, 0, 0, false, false);
    }
}
