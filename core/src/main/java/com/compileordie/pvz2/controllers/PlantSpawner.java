package com.compileordie.pvz2.controllers;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
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
            //TODO: i don't know why but i had to change the file patch, because i got errors.
            repository.loadFromCSV("configs/main_plantscsv.csv");
        }
    }

    public static Plant spawn(PlantType plantType, double x, double y, boolean isBoosted, boolean isSpecial) {
        initRepository(); // Ensures CSV is loaded

        // 1. Build the fresh Level 1 blueprint
        Plant newPlant = PlantFactory.createPlant(repository.getTemplate(plantType.getCommercialName()), x, y);

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
            // newPlant.applyBoost();
        }

        return newPlant;
    }

    public static Plant spawn(PlantType plantType) {
        return spawn(plantType, 0, 0, false, false);
    }
}
