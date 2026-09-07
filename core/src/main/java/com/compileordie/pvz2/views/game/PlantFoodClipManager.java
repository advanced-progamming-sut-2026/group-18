package com.compileordie.pvz2.views.game;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;

public class PlantFoodClipManager {

    public static String getClip(String name, Plant plant) {
        // Convert the logic ticks directly to seconds for easy sequencing!
        float t = (float) (plant.plantFoodTimer * Constants.Game.TIME_COEFFICIENT);

        switch (name) {
            case "Sunflower":
            case "Twin Sunflower":
            case "Primal Sunflower":
            case "Sun-shroom":
                return getSunProducerClip(name, plant, t);

            case "Split Pea":
            case "Threepeater":
            case "Peashooter":
            case "Repeater":
            case "Mega Gatling Pea":
            case "Fire Peashooter":
            case "Snow Pea":
            case "Starfruit":
            case "Puff-shroom":
            case "Rotobaga":
            case "Sea-shroom":
            case "Pea Pod":
            case "Bowling Bulb":
            case "Cactus":
            case "Goo Peashooter":
                return getShooterClip(name, plant, t);

            case "Cabbage-pult":
            case "Kernel-pult":
            case "Melon-pult":
            case "Winter Melon":
            case "Pepper-pult":
                return getLobberClip(name, plant, t);

            case "Phat Beet":
            case "Kiwibeast":
            case "Bonk Choy":
            case "Wasabi Whip":
            case "Tangle Kelp":
            case "Citron":
            case "Fume-shroom":
            case "Chomper":
                return getMeleeAndSpecialClip(name, plant, t);

            case "Wall-nut":
            case "Explode-o-nut":
            case "Sun Bean":
            case "Tall-nut":
            case "Torchwood":
            case "Potato Mine":
            case "Primal Potato Mine":
            case "Iceberg Lettuce":
            case "Sweet Potato":
            case "Caulipower":
            case "Electric Blueberry":
            case "Endurian":
            case "Garlic":
            case "Magnet-shroom":
                return getDefenseAndSupportClip(name, plant, t);

            default:
                return null;
        }
    }

    private static String getSunProducerClip(String name, Plant plant, float t) {
        // --- SUNFLOWER & TWIN SUNFLOWER (2.5 Seconds Total) ---
        if (name.equals("Sunflower") || name.equals("Twin Sunflower")) {
            if (t < 0.83f) return "plantfood_on";
            if (t < 1.66f) return "plantfood";
            if (t < 2.50f) return "plantfood_off";

            plant.resetFeed();
            return null;
        }

        // --- PRIMAL SUNFLOWER (3.0 Seconds Total) ---
        if (name.equals("Primal Sunflower")) {
            if (t < 1.0f) return "plantfood_on";
            if (t < 2.0f) return "plantfood";
            if (t < 3.0f) return "plantfood_off";

            plant.resetFeed();
            return null;
        }

        // --- SUN-SHROOM (2.2 Seconds Total) ---
        if (name.equals("Sun-shroom")) {
            if (t < 2.2f) {
                return "plantfood_stage" + plant.getGrowthStage();
            }

            plant.resetFeed();
            return null;
        }

        return null;
    }

    private static String getShooterClip(String name, Plant plant, float t) {
        // --- 1. SINGLE-CLIP BARRAGES (3.0 Seconds Total) ---
        // (Peashooter, Split Pea, Threepeater)
        if (name.equals("Split Pea") || name.equals("Threepeater")) {
            if (t < 2.1f) return "plantfood";
            plant.resetFeed();
            return null;
        }
        // --- 2. REPEATER (4.0 Seconds Total) ---
        // (3s barrage + 1s giant pea recoil)
        if (name.equals("Peashooter") || name.equals("Repeater")) {
            if (t < 3.6f) return "plantfood";
            plant.resetFeed();
            return null;
        }
        // --- 3. MEGA GATLING PEA (5.5 Seconds Total) ---
        // (4.5s barrage + 1s giant peas recoil)
        if (name.equals("Mega Gatling Pea")) {
            if (t < 5.5f) return "plantfood";
            plant.resetFeed();
            return null;
        }
        // --- 4. FIRE PEASHOOTER (4.0 Seconds Total) ---
        // (plantfood -> plantfood_loop -> plantfood_end)
        if (name.equals("Fire Peashooter")) {
            if (t < 0.5f) return "plantfood";      // Ignite
            if (t < 4.1f) return "plantfood_loop"; // 3 second barrage loop
            if (t < 4.2f) return "plantfood_end";  // Cool down
            plant.resetFeed();
            return null;
        }
        // --- 5. 3-PHASE SEQUENCES (4.0 Seconds Total) ---
        // (Snow Pea, Pea Pod, Starfruit, Puff-shroom)
        if (name.equals("Snow Pea")) {
            if (t < 0.5f) return "plantfood_on";
            if (t < 4.4f) return "plantfood";      // 3 second barrage
            if (t < 4.5f) return "plantfood_off";
            plant.resetFeed();
            return null;
        }
        if (name.equals("Starfruit") || name.equals("Puff-shroom")) {
            if (t < 0.5f) return "plantfood_on";
            if (t < 3.5f) return "plantfood";      // 3 second barrage
            if (t < 3.6f) return "plantfood_off";
            plant.resetFeed();
            return null;
        }
        // --- 6. ROTO-BAGA (3.0 Seconds Total) ---
        // (Only has plantfood_on clip for the entire barrage)
        if (name.equals("Rotobaga")) {
            if (t < 3.0f) return "plantfood_on";
            plant.resetFeed();
            return null;
        }
        // --- 7. SEA-SHROOM (3.0 Seconds Total) ---
        if (name.equals("Sea-shroom")) {
            if (t < 3.0f) return "pf";
            plant.resetFeed();
            return null;
        }
        // --- DYNAMIC PEA POD ---
        if (name.equals("Pea Pod")) {
            // Duration scales exactly with the number of heads (0.8s per head)
            float totalDuration = plant.getStackCount() * 0.8f;

            if (t < 0.5f) return "plantfood_on";
            if (t < 0.5f + totalDuration) return "plantfood";
            if (t < 1.0f + totalDuration) return "plantfood_off";

            plant.resetFeed();
            return null;
        }
        // --- 8. BOWLING BULB (4.0 Seconds Total) ---
        if (name.equals("Bowling Bulb")) {
            if (t < 0.5f) return "plantfood_on";
            if (t < 1.0f) return "plantfood_idle";
            if (t < 2.0f) return "plantfood1";
            if (t < 3.0f) return "plantfood2";
            if (t < 4.0f) return "plantfood3";
            plant.resetFeed();
            return null;
        }
        // --- 13. PROJECTILE ENHANCE (Permanent Transformations) ---
        if (name.equals("Cactus")) {
            if (t < 1.0f) return "plantfood";
            plant.resetFeed();
            return null;
        }
        if (name.equals("Goo Peashooter")) {
            if (t < 2.0f) return "plantfood";
            plant.resetFeed();
            return null;
        }
        return null;
    }

    private static String getLobberClip(String name, Plant plant, float t) {
        // --- LOBBER BURSTS ---
        if (name.equals("Cabbage-pult") || name.equals("Kernel-pult") || name.equals("Melon-pult")
            || name.equals("Winter Melon") || name.equals("Pepper-pult")) {
            if (t < 6.0f) return "plantfood";
            return "plantfood";
        }
        return null;
    }

    private static String getMeleeAndSpecialClip(String name, Plant plant, float t) {
        // --- 10. PHAT BEET & KIWIBEAST (2.5 Seconds Total) ---
        if (name.equals("Phat Beet") || name.equals("Kiwibeast")) {
            if (t < 2.5f) return name.equals("Kiwibeast") ? "plantfood_stage3" : "plantfood";
            plant.resetFeed();
            return null;
        }

        // --- 11. MELEE / TANGLE KELP (3.5 Seconds Total) ---
        if (name.equals("Bonk Choy") || name.equals("Wasabi Whip") || name.equals("Tangle Kelp")) {
            if (t < 1.1f) return "plantfood_on";
            if (t < 3.0f) return "plantfood";
            if (t < 3.5f) return "plantfood_off";
            plant.resetFeed();
            return null;
        }

        if (name.equals("Citron")) {
            if (t < 3.7f) return "idle"; // Stays idle while lightning renders over it!
            if (t < 7.7f) return "plantfood"; // Shoots the orb
            plant.resetFeed();
            return null;
        }

        if (name.equals("Fume-shroom")) {
            if (t < 5.5f) return "plantfood";
            plant.resetFeed();
            return null;
        }

        if (name.equals("Chomper")) {
            if (t < 0.5f) return "plantfood_on";
            if (t < 1.5f) return "plantfood";
            if (t < 2.0f) return "plantfood_off";
            if (t < 9.0f) return "plantfood_burp"; // 7 seconds of glorious burping!
            if (t < 9.5f) return "plantfood_burp_end";

            // The animation is finished. NOW we reset the plant to normal!
            plant.resetFeed();
            return null;
        }

        return null;
    }

    private static String getDefenseAndSupportClip(String name, Plant plant, float t) {
        // --- 12. ARMOR BUFFS (0.5 Seconds Total) ---
        if (name.equals("Wall-nut") || name.equals("Explode-o-nut") || name.equals("Sun Bean")) {
            if (t < 0.5f) return "plantfood_on";
            plant.resetFeed(); // Animation done, return to normal logic!
            return null;
        }

        if (name.equals("Tall-nut")) {
            plant.resetFeed(); // Tall-nut has no animation, end state instantly!
            return null;
        }

        // --- ARMOR BUFFS & MODIFIERS (0.5 Seconds Total) ---
        if (name.equals("Wall-nut") || name.equals("Explode-o-nut") || name.equals("Sun Bean")
            || name.equals("Torchwood")) {
            if (t < 0.7f) return "plantfood_on";
            plant.resetFeed();
            return null;
        }

        // --- 15. SPAWN CLONES (2.5 Seconds Total) ---
        if (name.equals("Potato Mine")) {
            if (t < 0.5f) return "plantfood_on";
            if (t < 2.0f) return "plantfood";
            if (t < 2.5f) return "plantfood2";
            plant.resetFeed();
            return null;
        }

        if (name.equals("Primal Potato Mine")) {
            if (t < 0.5f) return "plantfood_on";
            if (t < 2.0f) return "plantfood";
            if (t < 2.5f) return "plantfood_off";
            plant.resetFeed();
            return null;
        }

        if (name.equals("Iceberg Lettuce")) {
            if (t < 1.0f) return "plantfood";
            plant.resetFeed();
            return null;
        }

        if (name.equals("Sweet Potato")) {
            if (t < 2.0f) return "plantfood";
            plant.resetFeed();
            return null;
        }

        // --- 19. CAULIPOWER (3.0 Seconds) ---
        if (name.equals("Caulipower")) {
            if (t < 0.5f) return "plantfood_start";
            if (t < 2.5f) return "plantfood_loop";
            if (t < 3.0f) return "plantfood_end";
            plant.resetFeed();
            return null;
        }

        // --- 20. ELECTRIC BLUEBERRY (2.5 Seconds) ---
        if (name.equals("Electric Blueberry")) {
            if (t < 2.5f) return "plantfood";
            plant.resetFeed();
            return null;
        }

        // --- 21. ENDURIAN (1.5 Seconds) ---
        if (name.equals("Endurian")) {
            if (t < 1.5f) return "Plantfood_on";
            plant.resetFeed();
            return null;
        }

        // --- 22. GARLIC & MAGNET-SHROOM (1.5 Seconds) ---
        if (name.equals("Garlic") || name.equals("Magnet-shroom")) {
            if (t < 1.5f) return "plantfood";
            plant.resetFeed();
            return null;
        }

        return null;
    }
}
