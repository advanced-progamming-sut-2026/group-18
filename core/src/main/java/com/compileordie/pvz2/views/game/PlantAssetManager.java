package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.HashMap;
import java.util.Map;

public class PlantAssetManager {
    private final TextureBank textures;
    private final PamPlayer player;

    public PlantAssetManager() {
        FileHandle assets = Gdx.files.internal("pvz-assets");
        // Initialize the bank with the default resolution tag (e.g., "768")
        this.textures = new TextureBank("768", assets);
        this.player = new PamPlayer(textures, assets);
    }

    /**
     * Call this inside loading screen. It blocks until the PAM is fully baked.
     */
    public ClipRef loadPlantClip(PlantType plantType, String clipName) {
        String pamPath = getPath(plantType);

        // Blocks the calling thread until parsed, baked, and textures uploaded.
        player.loadSync(pamPath);
        // Returns the cached handle.
        return player.getClip(pamPath, clipName);
    }

    public ClipRef loadPlantClip(PlantType plantType) {
        String pamPath = getPath(plantType);
        String idleTag = getIdleTag(plantType);
        player.loadSync(pamPath);
        return player.getClip(pamPath, idleTag);
    }

    /**
     * Gets the local-space bounding box of the animation.
     */
    public Rectangle getBounds(PlantType plantType, String clipName) {
        String pamPath = getPath(plantType);
        return player.bounds(pamPath, clipName);
    }

    public Rectangle getBounds(PlantType plantType) {
        String pamPath = getPath(plantType);
        String idleTag = getIdleTag(plantType);
        return player.bounds(pamPath, idleTag);
    }

    /**
     * MUST be called every frame in main render() loop!
     */
    public void update() {
        textures.update();
    }

    /**
     * Draws the plant with precise alignment using LibGDX's Align constants.
     */
    public void drawPlant(Batch batch,
                          ClipRef clip,
                          Rectangle bounds,
                          float x,
                          float y,
                          int alignment,
                          float stateTime,
                          boolean loop) {
        float drawX = x;
        float drawY = y;

        // Calculate the visual center of the bounding box relative to its local origin
        float localCenterX = bounds.x + (bounds.width / 2f);
        float localCenterY = bounds.y + (bounds.height / 2f);

        if (alignment == Align.center) {
            // For shop UI cards: (x, y) is treated as the dead middle of the target area.
            drawX = x - localCenterX;
            drawY = y - localCenterY;
        } else if (alignment == Align.bottom) {
            // For the game grid: (x, y) is treated as the bottom-middle of the lawn tile.
            drawX = x - localCenterX;
            drawY = y - bounds.y;
        }

        // Pass the adjusted coordinates to the stateless per-call renderer.
        Map<String, Boolean> visibility = new HashMap<>();
        visibility.put("Magnet_Item", false);
        player.draw(batch, clip, stateTime, drawX, drawY, loop, visibility);
    }

    /**
     * Formats a simple plantType into the expected PAM file structure.
     */
    private String getPath(PlantType plantType) {
        return switch (plantType) {
            case SUNFLOWER -> "768/INITIAL/PLANT/SUNFLOWER/SUNFLOWER.PAM";
            case TWIN_SUNFLOWER -> "768/INITIAL/PLANT/SUNFLOWER_TWIN/SUNFLOWER_TWIN.PAM";
            case GOLD_BLOOM -> "768/INITIAL/PLANT/GOLDBLOOM/GOLDBLOOM.PAM";
            case PEASHOOTER -> "768/INITIAL/PLANT/PEASHOOTER/PEASHOOTER.PAM";
            case REPEATER -> "768/INITIAL/PLANT/REPEATER/REPEATER.PAM";
            case THREEPEATER -> "768/INITIAL/PLANT/THREEPEATER/THREEPEATER.PAM";
            case SNOW_PEA -> "768/INITIAL/PLANT/SNOWPEA/SNOWPEA.PAM";
            case CAULIPOWER -> "768/INITIAL/PLANT/CAULIPOWER/CAULIPOWER.PAM";
            case ELECTRIC_BLUEBERRY -> "768/INITIAL/PLANT/ELECTRICBLUEBERRY/ELECTRICBLUEBERRY.PAM";
            case CACTUS -> "768/INITIAL/PLANT/CACTUS/CACTUS.PAM";
            case FIRE_PEASHOOTER -> "768/INITIAL/PLANT/FIREPEASHOOTER/FIREPEASHOOTER.PAM";
            case STARFRUIT -> "768/INITIAL/PLANT/STARFRUIT/STARFRUIT.PAM";
            case GOO_PEASHOOTER -> "768/INITIAL/PLANT/GOOPEASHOOTER/GOOPEASHOOTER.PAM";
            case MEGA_GATLING_PEA -> "768/INITIAL/PLANT/MEGAGATLING/MEGAGATLING.PAM";
            case PUFF_SHROOM -> "768/INITIAL/PLANT/PUFFSHROOM/PUFFSHROOM.PAM";
            case FUME_SHROOM -> "768/INITIAL/PLANT/FUMESHROOM/FUMESHROOM.PAM";
            case CABBAGE_PULT -> "768/INITIAL/PLANT/CABBAGEPULT/CABBAGEPULT.PAM";
            case KERNEL_PULT -> "768/INITIAL/PLANT/KERNALPULT/KERNALPULT.PAM";
            case MELON_PULT -> "768/INITIAL/PLANT/MELONPULT/MELONPULT.PAM";
            case POTATO_MINE -> "768/INITIAL/PLANT/POTATOMINE/POTATOMINE.PAM";
            case SQUASH -> "768/INITIAL/PLANT/SQUASH/SQUASH.PAM";
            case GRAPESHOT -> "768/INITIAL/PLANT/GRAPESHOT/GRAPESHOT.PAM";
            case JALAPENO -> "768/INITIAL/PLANT/JALAPENO/JALAPENO.PAM";
            case ICEBERG_LETTUCE -> "768/INITIAL/PLANT/ICEBURG/ICEBURG.PAM";
            case BONK_CHOY -> "768/INITIAL/PLANT/BONKCHOY/BONKCHOY.PAM";
            case CHOMPER -> "768/INITIAL/PLANT/CHOMPER/CHOMPER.PAM";
            case WASABI_WHIP -> "768/INITIAL/PLANT/WASABIWHIP/WASABIWHIP.PAM";
            case KIWIBEAST -> "768/INITIAL/PLANT/KIWIBEAST/KIWIBEAST.PAM";
            case WALL_NUT -> "768/INITIAL/PLANT/WALLNUT/WALLNUT.PAM";
            case SWEET_POTATO -> "768/INITIAL/PLANT/SWEETPOTATO/SWEETPOTATO.PAM";
            case EXPLODE_O_NUT -> "768/INITIAL/PLANT/EXPLODEONUT/EXPLODEONUT.PAM";
            case PUMPKIN -> "768/INITIAL/PLANT/PUMPKIN/PUMPKIN.PAM";
            case TORCHWOOD -> "768/INITIAL/PLANT/TORCHWOOD/TORCHWOOD.PAM";
            case HYPNO_SHROOM -> "768/INITIAL/PLANT/HYPNOSHROOM/HYPNOSHROOM.PAM";
            case IMITATER -> "768/INITIAL/PLANT/IMITATER/IMITATER.PAM";
            case GRAVE_BUSTER -> "768/INITIAL/PLANT/GRAVEBUSTER/GRAVEBUSTER.PAM";
            case SUN_SHROOM -> "768/FULL/PLANT/SUNSHROOM/SUNSHROOM.PAM";
            case PRIMAL_SUNFLOWER -> "768/FULL/PLANT/PRIMAL_SUNFLOWER/PRIMAL_SUNFLOWER.PAM";
            case ROTOBAGA -> "768/FULL/PLANT/ROTORUTABAGA/ROTORUTABAGA.PAM";
            case PEA_POD -> "768/FULL/PLANT/PEAPOD/PEAPOD.PAM";
            case SPLIT_PEA -> "768/FULL/PLANT/SPLITPEA/SPLITPEA.PAM";
            case CITRON -> "768/FULL/PLANT/CITRON/CITRON.PAM";
            case BOWLING_BULB -> "768/FULL/PLANT/BOWLINGBULB/BOWLINGBULB.PAM";
            case SEA_SHROOM -> "768/FULL/PLANT/SEASHROOM/SEASHROOM.PAM";
            case WINTER_MELON -> "768/FULL/PLANT/WINTERMELON/WINTERMELON.PAM";
            case PEPPER_PULT -> "768/FULL/PLANT/PEPPERPULT/PEPPERPULT.PAM";
            case PRIMAL_POTATO_MINE -> "768/FULL/PLANT/PRIMAL_POTATOMINE/PRIMAL_POTATOMINE.PAM";
            case CHERRY_BOMB -> "768/FULL/PLANT/CHERRYBOMB/CHERRYBOMB.PAM";
            case DOOM_SHROOM -> "768/FULL/PLANT/DOOMSHROOM/DOOMSHROOM.PAM";
            case TANGLE_KELP -> "768/FULL/PLANT/TANGLEKELP/TANGLEKELP.PAM";
            case PHAT_BEET -> "768/FULL/PLANT/PHATBEETS/PHATBEETS.PAM";
            case TALL_NUT -> "768/FULL/PLANT/TALLNUT/TALLNUT.PAM";
            case ENDURIAN -> "768/FULL/PLANT/ENDURIAN/ENDURIAN.PAM";
            case GARLIC -> "768/FULL/PLANT/GARLIC/GARLIC.PAM";
            case SUN_BEAN -> "768/FULL/PLANT/SUNBEAN/SUNBEAN.PAM";
            case MAGNET_SHROOM -> "768/FULL/PLANT/MAGNETSHROOM/MAGNETSHROOM.PAM";
            case CAT_TAIL -> "768/INITIAL/PLANT/ELECTRIC_PEASHOOTER/ELECTRIC_PEASHOOTER.PAM"; // Didn't have cattail!
            case ICE_SHROOM -> "768/FULL/PLANT/ICESHROOM/ICESHROOM.PAM";
            case LILY_PAD -> "768/FULL/PLANT/LILYPAD/LILYPAD.PAM";
            case HOT_POTATO -> "768/FULL/PLANT/HOTPOTATO/HOTPOTATO.PAM";
            case ENLIGHTEN_MINT -> "768/INITIAL/EMPOWERMINTS/PLANT/ENLIGHTENMINT/ENLIGHTENMINT.PAM";
            case APPEASE_MINT -> "768/INITIAL/EMPOWERMINTS/PLANT/APPEASEMINT/APPEASEMINT.PAM";
            case ARMA_MINT -> "768/INITIAL/EMPOWERMINTS/PLANT/ARMAMINT/ARMAMINT.PAM";
            case BOMBARD_MINT -> "768/INITIAL/EMPOWERMINTS/PLANT/BOMBARDMINT/BOMBARDMINT.PAM";
            case ENFORCE_MINT -> "768/INITIAL/EMPOWERMINTS/PLANT/ENFORCEMINT/ENFORCEMINT.PAM";
            case REINFORCE_MINT -> "768/INITIAL/EMPOWERMINTS/PLANT/REINFORCEMINT/REINFORCEMINT.PAM";
            case ENCHANT_MINT -> "768/INITIAL/EMPOWERMINTS/PLANT/ENCHANTMINT/ENCHANTMINT.PAM";
            case PIERCE_MINT -> "768/INITIAL/EMPOWERMINTS/PLANT/PEPPERMINT/PEPPERMINT.PAM"; // Didn't have piercemint!
            case CATTAIL_MINT -> "768/INITIAL/EMPOWERMINTS/PLANT/WINTERMINT/WINTERMINT.PAM"; // Didn't have cattailmint!
        };
    }

    /**
     * Returns the animation tag used for idle state.
     */
    private String getIdleTag(PlantType plantType) {
        return switch (plantType) {
            case SUN_SHROOM, PUFF_SHROOM -> "idle_stage1";
            case CAULIPOWER, ELECTRIC_BLUEBERRY -> "idle1_1";
            case DOOM_SHROOM -> "stage1_idle";
            case KIWIBEAST -> "idle_stage1_";
            case GRAVE_BUSTER -> "attack1";
            case APPEASE_MINT,
                 ARMA_MINT,
                 BOMBARD_MINT,
                 CATTAIL_MINT,
                 ENCHANT_MINT,
                 ENFORCE_MINT,
                 ENLIGHTEN_MINT,
                 PIERCE_MINT,
                 REINFORCE_MINT -> "loop";
            default -> "idle";
        };
    }

    /**
     * A clean pass-through for rendering.
     */
    public void drawPlant(Batch batch, ClipRef clip, float stateTime, float x, float y, boolean loop) {
        player.draw(batch, clip, stateTime, x, y, loop);
    }

    public void dispose() {
        textures.dispose();
        // PamPlayer has no public memory eviction hooks, so dropping the instance
        // is the only way to let the garbage collector free the cached PAMs.
    }
}
