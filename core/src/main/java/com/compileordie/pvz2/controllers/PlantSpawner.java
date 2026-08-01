package com.compileordie.pvz2.controllers;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.factory.PlantFactory;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.repositories.configs.PlantConfigRepository;

public class PlantSpawner {
    public static Plant spawn(PlantType plantType, double x, double y, boolean isBoosted, boolean isSpecial) {
        PlantConfigRepository repository = new PlantConfigRepository();
        repository.loadFromCSV(Constants.Paths.Configs.PLANTS);
        return PlantFactory.createPlant(repository.getTemplate(plantType.getCommercialName()), x, y);
    }

    public static Plant spawn(PlantType plantType) {
        return spawn(plantType, 0, 0, false, false);
    }
}
