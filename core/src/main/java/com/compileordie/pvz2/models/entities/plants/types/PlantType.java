package com.compileordie.pvz2.models.entities.plants.types;

import com.compileordie.pvz2.utils.Toolbox; //[cite: 1]

public enum PlantType {
    SUNFLOWER("Sunflower"),
    TWIN_SUNFLOWER("Twin Sunflower"),
    SUN_SHROOM("Sun-shroom"),
    PRIMAL_SUNFLOWER("Primal Sunflower"),
    GOLD_BLOOM("Gold Bloom"),
    PEASHOOTER("Peashooter"),
    REPEATER("Repeater"),
    THREEPEATER("Threepeater"),
    SNOW_PEA("Snow Pea"),
    ROTOBAGA("Rotobaga"),
    PEA_POD("Pea Pod"),
    SPLIT_PEA("Split Pea"),
    CITRON("Citron"),
    CAULIPOWER("Caulipower"),
    ELECTRIC_BLUEBERRY("Electric Blueberry"),
    BOWLING_BULB("Bowling Bulb"),
    CACTUS("Cactus"),
    FIRE_PEASHOOTER("Fire Peashooter"),
    STARFRUIT("Starfruit"),
    GOO_PEASHOOTER("Goo Peashooter"),
    MEGA_GATLING_PEA("Mega Gatling Pea"),
    SEA_SHROOM("Sea-shroom"),
    PUFF_SHROOM("Puff-shroom"),
    FUME_SHROOM("Fume-shroom"),
    CABBAGE_PULT("Cabbage-pult"),
    KERNEL_PULT("Kernel-pult"),
    MELON_PULT("Melon-pult"),
    WINTER_MELON("Winter Melon"),
    PEPPER_PULT("Pepper-pult"),
    POTATO_MINE("Potato Mine"),
    PRIMAL_POTATO_MINE("Primal Potato Mine"),
    CHERRY_BOMB("Cherry Bomb"),
    SQUASH("Squash"),
    GRAPESHOT("Grapeshot"),
    JALAPENO("Jalapeno"),
    DOOM_SHROOM("Doom-shroom"),
    TANGLE_KELP("Tangle Kelp"),
    ICEBERG_LETTUCE("Iceberg Lettuce"),
    BONK_CHOY("Bonk Choy"),
    PHAT_BEET("Phat Beet"),
    CHOMPER("Chomper"),
    WASABI_WHIP("Wasabi Whip"),
    KIWIBEAST("Kiwibeast"),
    WALL_NUT("Wall-nut"),
    TALL_NUT("Tall-nut"),
    ENDURIAN("Endurian"),
    GARLIC("Garlic"),
    SWEET_POTATO("Sweet Potato"),
    EXPLODE_O_NUT("Explode-o-nut"),
    PUMPKIN("Pumpkin"),
    SUN_BEAN("Sun Bean"),
    TORCHWOOD("Torchwood"),
    MAGNET_SHROOM("Magnet-shroom"),
    HYPNO_SHROOM("Hypno-shroom"),
    CAT_TAIL("Cat-tail"),
    IMITATER("Imitater"),
    ICE_SHROOM("Ice-shroom"),
    LILY_PAD("Lily Pad"),
    HOT_POTATO("Hot Potato"),
    GRAVE_BUSTER("Grave Buster"),
    ENLIGHTEN_MINT("Enlighten-mint"),
    APPEASE_MINT("Appease-mint"),
    ARMA_MINT("Arma-mint"),
    BOMBARD_MINT("Bombard-mint"),
    ENFORCE_MINT("Enforce-mint"),
    REINFORCE_MINT("Reinforce-mint"),
    ENCHANT_MINT("Enchant-mint"),
    PIERCE_MINT("Pierce-mint"),
    CATTAIL_MINT("catTail-mint"); //[cite: 1]

    private final String commercialName;

    // Constructor to bind the exact CSV name to the enum constant
    PlantType(String commercialName) {
        this.commercialName = commercialName;
    }

    /**
     * Returns the exact corresponding plant name as found in main_plantscsv.csv
     */
    public String getCommercialName() {
        return commercialName;
    }

    public static PlantType getByName(String name) {
        for (PlantType plantType : PlantType.values()) {
            // FIXED: Compare against the exact CSV string you stored!
            if (plantType.getCommercialName().equalsIgnoreCase(name)) {
                return plantType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
