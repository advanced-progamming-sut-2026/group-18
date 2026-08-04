package com.compileordie.pvz2.controllers;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.factory.PlantFactory;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.repositories.configs.PlantConfigRepository;

public class PlantSpawner {
    public static Plant spawn(PlantType plantType, double x, double y, boolean isBoosted, boolean isSpecial) {
        // TODO: Fix and use boosted and special.
        PlantConfigRepository repository = new PlantConfigRepository();
        repository.loadFromCSV("assets/configs/main_plantscsv.csv");
        return PlantFactory.createPlant(repository.getTemplate(plantType.getCommercialName()), x, y);
    }

    public static Plant spawn(PlantType plantType) {
        return spawn(plantType, 0, 0, false, false);
    }
}
