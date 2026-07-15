package com.compileordie.pvz2.models.shop;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public class DailyOffer {
    public PlantType targetPlant;
    public int price;
    public int quantity;
    public String offerDate;
    public boolean purchased;

    public DailyOffer() {
    }

    public DailyOffer(String offerDate) {
        this.price = ConfigManager.economy().dailyOfferSeedPrice;
        this.quantity = ConfigManager.economy().dailyOfferSeedCount;
        this.offerDate = offerDate;
        this.purchased = false;
    }
}
