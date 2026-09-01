package com.compileordie.pvz2.controllers.menus.progression;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;

import java.util.ArrayList;

public class CollectionMenuController {
    private CollectionMenuController() {
    }

    public static Result<String> upgradePlant(PlantType plantType) {
        if (plantType == null) return Result.failure("Invalid plant");
        Player player = AppModel.player;

        if (player.unlockedPlants == null || !player.unlockedPlants.contains(plantType)) {
            return Result.failure("You must unlock this plant before upgrading it");
        }

        int currentLevel = player.plantLevels.get(plantType);
        if (currentLevel >= 4) {
            return Result.failure("Plant is already at maximum level (Level 4)");
        }
        int coinCost = currentLevel * ConfigManager.economy().plantUpgradeCoinsPerLevel;
        int packetCost = currentLevel * ConfigManager.economy().plantUpgradeSeedsPerLevel;
        int currentPackets = player.seedPackets.get(plantType);

        if (player.coins < coinCost || currentPackets < packetCost) {
            return Result.failure("Insufficient resources. You need " + Math.max(0, coinCost - player.coins) +
                " more coins and " + Math.max(0, packetCost - currentPackets) + " more seed packets");
        }

        player.coins -= coinCost;
        player.seedPackets.put(plantType, currentPackets - packetCost);
        player.plantLevels.put(plantType, currentLevel + 1);

        new UserDatabase().save(player);
        return Result.success(plantType.name() + " upgraded successfully to level " + (currentLevel + 1) + "!");
    }

    public static Result<String> purchasePlant(PlantType plantType) {
        if (plantType == null) return Result.failure("Invalid plant");
        int price = ConfigManager.economy().plantPurchaseCoins;
        Player player = AppModel.player;

        if (player.unlockedPlants != null && player.unlockedPlants.contains(plantType)) {
            return Result.failure("Plant is already unlocked");
        }
        if (player.coins < price) {
            return Result.failure("Insufficient coins. You need " + (price - player.coins) + " more coins");
        }

        player.coins -= price;
        if (player.unlockedPlants == null) player.unlockedPlants = new ArrayList<>();
        player.unlockedPlants.add(plantType);

        new UserDatabase().save(player);
        return Result.success(plantType.name() + " purchased successfully!");
    }

    public static boolean canUpgrade(PlantType plantType) {
        Player player = AppModel.player;
        if (player.unlockedPlants == null || !player.unlockedPlants.contains(plantType)) return false;

        int currentLevel = player.plantLevels.getOrDefault(plantType, 1);
        if (currentLevel >= 4) return false;
        int coinCost = currentLevel * ConfigManager.economy().plantUpgradeCoinsPerLevel;
        int packetCost = currentLevel * ConfigManager.economy().plantUpgradeSeedsPerLevel;
        int currentPackets = player.seedPackets.getOrDefault(plantType, 0);

        return player.coins >= coinCost && currentPackets >= packetCost;
    }

    public static String getPlantCardAssetPath(PlantType plantType) {
        return switch (plantType) {
            case SUNFLOWER -> "IMAGE_UI_PACKETS_SUNFLOWER";
            case TWIN_SUNFLOWER -> "IMAGE_UI_PACKETS_TWINSUNFLOWER";
            case SUN_SHROOM -> "IMAGE_UI_PACKETS_SUNSHROOM";
            case PRIMAL_SUNFLOWER -> "IMAGE_UI_PACKETS_PRIMALSUNFLOWER";
            case GOLD_BLOOM -> "IMAGE_UI_PACKETS_GOLDBLOOM";
            case PEASHOOTER -> "IMAGE_UI_PACKETS_PEASHOOTER";
            case REPEATER -> "IMAGE_UI_PACKETS_REPEATER";
            case THREEPEATER -> "IMAGE_UI_PACKETS_THREEPEATER";
            case SNOW_PEA -> "IMAGE_UI_PACKETS_SNOWPEA";
            case ROTOBAGA -> "IMAGE_UI_PACKETS_XSHOT";
            case PEA_POD -> "IMAGE_UI_PACKETS_PEAPOD";
            case SPLIT_PEA -> "IMAGE_UI_PACKETS_SPLITPEA";
            case CITRON -> "IMAGE_UI_PACKETS_CITRON";
            case CAULIPOWER -> "IMAGE_UI_PACKETS_CAULIPOWER";
            case ELECTRIC_BLUEBERRY -> "IMAGE_UI_PACKETS_ELECTRICBLUEBERRY";
            case BOWLING_BULB -> "IMAGE_UI_PACKETS_BOWLINGBULB";
            case CACTUS -> "IMAGE_UI_PACKETS_CACTUS";
            case FIRE_PEASHOOTER -> "IMAGE_UI_PACKETS_FIREPEASHOOTER";
            case STARFRUIT -> "IMAGE_UI_PACKETS_STARFRUIT";
            case GOO_PEASHOOTER -> "IMAGE_UI_PACKETS_POISONPEASHOOTER";
            case MEGA_GATLING_PEA -> "IMAGE_UI_PACKETS_MEGAGATLING";
            case SEA_SHROOM -> "IMAGE_UI_PACKETS_SEASHROOM";
            case PUFF_SHROOM -> "IMAGE_UI_PACKETS_PUFFSHROOM";
            case FUME_SHROOM -> "IMAGE_UI_PACKETS_FUMESHROOM";
            case CABBAGE_PULT -> "IMAGE_UI_PACKETS_CABBAGEPULT";
            case KERNEL_PULT -> "IMAGE_UI_PACKETS_KERNELPULT";
            case MELON_PULT -> "IMAGE_UI_PACKETS_MELONPULT";
            case WINTER_MELON -> "IMAGE_UI_PACKETS_WINTERMELON";
            case PEPPER_PULT -> "IMAGE_UI_PACKETS_PEPPERPULT";
            case POTATO_MINE -> "IMAGE_UI_PACKETS_POTATOMINE";
            case PRIMAL_POTATO_MINE -> "IMAGE_UI_PACKETS_PRIMALPOTATOMINE";
            case CHERRY_BOMB -> "IMAGE_UI_PACKETS_CHERRY_BOMB";
            case SQUASH -> "IMAGE_UI_PACKETS_SQUASH";
            case GRAPESHOT -> "IMAGE_UI_PACKETS_GRAPESHOT";
            case JALAPENO -> "IMAGE_UI_PACKETS_JALAPENO";
            case DOOM_SHROOM -> "IMAGE_UI_PACKETS_DOOMSHROOM";
            case TANGLE_KELP -> "IMAGE_UI_PACKETS_TANGLEKELP";
            case ICEBERG_LETTUCE -> "IMAGE_UI_PACKETS_ICEBURG";
            case BONK_CHOY -> "IMAGE_UI_PACKETS_BONKCHOY";
            case PHAT_BEET -> "IMAGE_UI_PACKETS_PHATBEET";
            case CHOMPER -> "IMAGE_UI_PACKETS_CHOMPER";
            case WASABI_WHIP -> "IMAGE_UI_PACKETS_WASABIWHIP";
            case KIWIBEAST -> "IMAGE_UI_PACKETS_KIWIBEAST";
            case WALL_NUT -> "IMAGE_UI_PACKETS_WALLNUT";
            case TALL_NUT -> "IMAGE_UI_PACKETS_TALLNUT";
            case ENDURIAN -> "IMAGE_UI_PACKETS_ENDURIAN";
            case GARLIC -> "IMAGE_UI_PACKETS_GARLIC";
            case SWEET_POTATO -> "IMAGE_UI_PACKETS_SWEETPOTATO";
            case EXPLODE_O_NUT -> "IMAGE_UI_PACKETS_EXPLODEONUT";
            case PUMPKIN -> "IMAGE_UI_PACKETS_PUMPKIN";
            case SUN_BEAN -> "IMAGE_UI_PACKETS_SUNBEAN";
            case TORCHWOOD -> "IMAGE_UI_PACKETS_TORCHWOOD";
            case MAGNET_SHROOM -> "IMAGE_UI_PACKETS_MAGNETSHROOM";
            case HYPNO_SHROOM -> "IMAGE_UI_PACKETS_HYPNOSHROOM";
            case CAT_TAIL -> "IMAGE_UI_PACKETS_ELECTRICPEASHOOTER";
            case IMITATER -> "IMAGE_UI_PACKETS_IMITATER";
            case ICE_SHROOM -> "IMAGE_UI_PACKETS_ICESHROOM";
            case LILY_PAD -> "IMAGE_UI_PACKETS_LILYPAD";
            case HOT_POTATO -> "IMAGE_UI_PACKETS_HOTPOTATO";
            case GRAVE_BUSTER -> "IMAGE_UI_PACKETS_GRAVEBUSTER";
            case ENLIGHTEN_MINT -> "IMAGE_UI_PACKETS_ENLIGHTENMINT";
            case APPEASE_MINT -> "IMAGE_UI_PACKETS_APPEASEMINT";
            case ARMA_MINT -> "IMAGE_UI_PACKETS_ARMAMINT";
            case BOMBARD_MINT -> "IMAGE_UI_PACKETS_BOMBARDMINT";
            case ENFORCE_MINT -> "IMAGE_UI_PACKETS_ENFORCEMINT";
            case REINFORCE_MINT -> "IMAGE_UI_PACKETS_REINFORCEMINT";
            case ENCHANT_MINT -> "IMAGE_UI_PACKETS_ENCHANTMINT";
            case PIERCE_MINT -> "IMAGE_UI_PACKETS_SPEARMINT";
            case CATTAIL_MINT -> "IMAGE_UI_PACKETS_WINTERMINT";
            //TODO :
            case BOWLING_WALL_NUT -> "IMAGE_UI_PACKETS_TALLNUT";
            case BOWLING_EXPLODE_O_NUT -> "IMAGE_UI_PACKETS_EXPLODEONUT";
            case GIANT_WALL_NUT -> "IMAGE_UI_PACKETS_PRIMALWALLNUT";
        };
    }

    public static String getZombieCardAssetPath(ZombieType zombieType) {
        return switch (zombieType) {
            case STANDARD -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_TUTORIAL";
            case CONEHEAD -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_TUTORIAL_ARMOR1";
            case BUCKETHEAD -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_TUTORIAL_ARMOR2";
            case KNIGHT -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_DARK_ARMOR3";
            case BLOCKHEAD -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_TUTORIAL_ARMOR4";
            case GARGANTUAR -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_TUTORIAL_GARGANTUAR";
            case IMP -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_TUTORIAL_IMP";
            case ALL_STAR -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_MODERN_ALLSTAR";
            case PARASOL_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_LOSTCITY_JANE";
            case TURQUOISE_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_LOSTCITY_CRYSTALSKULL";
            case PROSPECTOR_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_PROSPECTOR";
            case PIANIST_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_PIANO";
            case NEWSPAPER_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_MODERN_NEWSPAPER";
            case BARREL_ROLLER -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_BARRELROLLER";
            case RAINCOAT_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_RAINCOAT";
            case RA_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_RA";
            case EXPLORER_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_EXPLORER";
            case TOMBRAISER -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_TOMB_RAISER";
            case DODO_RIDER -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_ICEAGE_DODO";
            case HUNTER_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_ICEAGE_HUNTER";
            case SNORKEL_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_BEACH_SNORKEL";
            case OCTOPUS_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_BEACH_OCTOPUS";
            case IMP_DRAGON -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_DARK_IMP_DRAGON";
            case ZOMBOSS_IN_EGYPT -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_ZOMBOSSMECH_EGYPT";
            case ZOMBOSS_IN_DARK -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_ZOMBOSSMECH_DARK";
        };
    }
}
