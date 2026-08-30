package com.compileordie.pvz2.views.game;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;

public class PlantFoodClipManager {

    public static String getClip(String name, Plant plant) {

        // Convert the logic ticks directly to seconds for easy sequencing!
        float t = (float) (plant.plantFoodTimer * Constants.Game.TIME_COEFFICIENT);

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

// --- LOBBER BURSTS ---
        if (name.equals("Cabbage-pult") || name.equals("Kernel-pult") || name.equals("Melon-pult") || name.equals("Winter Melon") || name.equals("Pepper-pult")) {
            if (t < 6.0f) return "plantfood";
            return "plantfood";
        }

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

        // --- 13. PROJECTILE ENHANCE (Permanent Transformations) ---
        if (name.equals("Cactus")) {
            if (t < 1.0f) return "plantfood";
            plant.resetFeed();
            return null;
        }
// --- ARMOR BUFFS & MODIFIERS (0.5 Seconds Total) ---
        if (name.equals("Wall-nut") || name.equals("Explode-o-nut") || name.equals("Sun Bean") || name.equals("Torchwood")) {
            if (t < 0.7f) return "plantfood_on";
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
}
