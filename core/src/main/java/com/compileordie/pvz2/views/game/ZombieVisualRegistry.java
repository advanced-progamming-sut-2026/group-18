package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.levels.ChapterType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZombieVisualRegistry {
    private static final String REAL_ASSET_ROOT = "pvz-assets/IMAGES/";

    public static class PamSpec {
        public final String pathTemplate;
        public final float offsetX;
        public final float offsetY;

        public PamSpec(String pathTemplate, float offsetX, float offsetY) {
            this.pathTemplate = pathTemplate;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public PamSpec(String pathTemplate) {
            this(pathTemplate, 0f, 0f);
        }

        public static PamSpec fixed(String literalPath) {
            return new PamSpec(literalPath) {
                @Override
                public String getResolvedPath() {
                    return this.pathTemplate;
                }
            };
        }

        public String getResolvedPath() {
            if (pathTemplate == null) return "";

            String chTag = getChapterTag();
            String path = pathTemplate;

            if ("EGYPT".equals(chTag)) {
                path = path.replace("/FULL/", "/INITIAL/");
            } else {
                path = path.replace("/INITIAL/", "/FULL/");
            }

            return path.replace("{CH}", chTag);
        }

        public boolean existsOnDisk() {
            try {
                String realPath = REAL_ASSET_ROOT + getResolvedPath();
                return Gdx.files.internal(realPath).exists();
            } catch (Throwable t) {
                Gdx.app.error("PVZ-REGISTRY-ERROR", "❌ خطا در بررسی دیسک برای فایل Asset: " + t.getMessage());
                return false;
            }
        }
    }

    public static class ZombieVisualDef {
        public final List<PamSpec> pams;
        public final List<String> extraStateFilterTemplates;

        public ZombieVisualDef(List<PamSpec> pams, List<String> extraStateFilterTemplates) {
            this.pams = pams != null ? pams : Collections.emptyList();
            if (extraStateFilterTemplates != null) {
                this.extraStateFilterTemplates = extraStateFilterTemplates;
            } else {
                this.extraStateFilterTemplates = Collections.emptyList();
            }
        }

        public List<String> getResolvedStateFilters() {
            String chTag = getChapterTag();
            List<String> resolved = new ArrayList<>(extraStateFilterTemplates.size());
            for (String tmpl : extraStateFilterTemplates) {
                resolved.add(tmpl.replace("{CH}", chTag).replace("{ch}", chTag.toLowerCase()));
            }
            return resolved;
        }

        public boolean isAvailableForCurrentChapter() {
            if (pams.isEmpty()) return false;
            for (PamSpec pam : pams) {
                if (!pam.existsOnDisk()) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final Map<String, ZombieVisualDef> REGISTRY = new HashMap<>();
    private static final Map<String, Boolean> AVAILABILITY_CACHE = new ConcurrentHashMap<>();

    public static void register(String zombieTypeName, ZombieVisualDef def) {
        if (zombieTypeName != null && def != null) {
            REGISTRY.put(zombieTypeName.toUpperCase(), def);
        }
    }

    public static ZombieVisualDef get(String zombieTypeName) {
        if (zombieTypeName == null || zombieTypeName.trim().isEmpty()) return null;
        String upper = zombieTypeName.trim().toUpperCase();

        ZombieVisualDef def = REGISTRY.get(upper);
        if (def != null) return def;

        String noUnderscore = upper.replace("_", "");
        def = REGISTRY.get(noUnderscore);
        if (def != null) return def;

        if (upper.endsWith("_ZOMBIE")) {
            String base = upper.substring(0, upper.length() - "_ZOMBIE".length());
            def = REGISTRY.get(base);
            if (def == null) def = REGISTRY.get(base.replace("_", ""));
        } else {
            String withZombie = upper + "_ZOMBIE";
            def = REGISTRY.get(withZombie);
            if (def == null) def = REGISTRY.get(withZombie.replace("_", ""));
        }

        return def;
    }

    public static boolean isAvailable(String zombieTypeName) {
        if (zombieTypeName == null) return false;
        ZombieVisualDef def = get(zombieTypeName);
        if (def == null) return false;

        String cacheKey = zombieTypeName.toUpperCase() + "@" + getChapterTag();

        Boolean cached = AVAILABILITY_CACHE.get(cacheKey);
        if (cached != null) return cached;

        boolean available = def.isAvailableForCurrentChapter();
        AVAILABILITY_CACHE.put(cacheKey, available);
        return available;
    }

    public static void clearAvailabilityCache() {
        AVAILABILITY_CACHE.clear();
    }

    public static String getChapterTag() {
        if (AppModel.currentChapter == ChapterType.ANCIENT_EGYPT) {
            return "EGYPT";
        } else if (AppModel.currentChapter == ChapterType.DARK_AGES) {
            return "DARK";
        } else if (AppModel.currentChapter == ChapterType.FROSTBITE_CAVES) {
            return "ICEAGE";
        } else if (AppModel.currentChapter == ChapterType.BIG_WAVE_BEACH) {
            return "BEACH";
        } else if (AppModel.currentChapter == ChapterType.MINIGAME) {
            return "MODERN";
        }
        return "EGYPT";
    }

    public static List<String> describeMissingAssets(String zombieTypeName) {
        List<String> missing = new ArrayList<>();
        ZombieVisualDef def = get(zombieTypeName);
        if (def == null) return missing;
        for (PamSpec pam : def.pams) {
            if (!pam.existsOnDisk()) {
                missing.add(pam.getResolvedPath());
            }
        }
        return missing;
    }

    static {
        ZombieVisualDef standardDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            Collections.emptyList()
        );
        register("STANDARD", standardDef);
        register("BASIC", standardDef);
        register("STANDARD_ZOMBIE", standardDef);

        ZombieVisualDef coneheadDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of(
                "_zombie_{ch}_armor1_states",
                "zombie_armor_cone_norm",
                "zombie_armor_cone_damage_01",
                "zombie_armor_cone_damage_02"
            )
        );
        register("CONEHEAD", coneheadDef);
        register("CONEHEAD_ZOMBIE", coneheadDef);

        ZombieVisualDef bucketheadDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of(
                "_zombie_{ch}_armor2_states",
                "zombie_armor_bucket_norm",
                "zombie_armor_bucket_damage_01",
                "zombie_armor_bucket_damage_02"
            )
        );
        register("BUCKETHEAD", bucketheadDef);
        register("BUCKETHEAD_ZOMBIE", bucketheadDef);

        ZombieVisualDef blockheadDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of(
                "zombie_armor_brick_norm",
                "zombie_armor_brick_damage_01",
                "zombie_armor_brick_damage_02"
            )
        );
        register("BLOCKHEAD", blockheadDef);
        register("BLOCKHEAD_ZOMBIE", blockheadDef);

        ZombieVisualDef knightDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of(
                "_zombie_armor_crown_states",
                "zombie_armor_crown_norm",
                "zombie_armor_crown_damage_01",
                "zombie_armor_crown_damage_02",
                "zombie_shoulder_armor",
                "zombie_shoulder_armor_norm",
                "zombie_shoulder_armor_damage_01",
                "zombie_shoulder_armor_damage_02"
            )
        );
        register("KNIGHT", knightDef);
        register("KNIGHT_ZOMBIE", knightDef);

        ZombieVisualDef raDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_RA/ZOMBIE_{CH}_RA.PAM")),
            Collections.emptyList()
        );
        register("RA", raDef);
        register("RA_ZOMBIE", raDef);

        ZombieVisualDef gargDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/{CH}_GARGANTUAR/{CH}_GARGANTUAR.PAM") {
                @Override
                public String getResolvedPath() {
                    String chTag = getChapterTag();
                    if ("ICEAGE".equals(chTag)) {
                        return "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_GARGANTUAR/ZOMBIE_ICEAGE_GARGANTUAR.PAM";
                    }
                    return super.getResolvedPath();
                }
            }),
            Collections.emptyList()
        );
        register("GARGANTUAR", gargDef);
        register("GARGANTUAR_ZOMBIE", gargDef);

        String impName;
        if (AppModel.currentChapter == ChapterType.ANCIENT_EGYPT) {
            impName = "768/INITIAL/ZOMBIE/ZOMBIE_{CH}_IMP/ZOMBIE_{CH}_IMP.PAM";
        } else {
            if (AppModel.currentChapter == ChapterType.DARK_AGES) {
                impName = "768/FULL/ZOMBIE/ZOMBIE_{CH}_IMP_MONK/ZOMBIE_{CH}_IMP_MONK.PAM";
            } else {
                if (AppModel.currentChapter == ChapterType.BIG_WAVE_BEACH) {
                    impName = "768/FULL/ZOMBIE/ZOMBIE_{CH}_IMP_MERMAID/ZOMBIE_{CH}_IMP_MERMAID.PAM";
                } else {
                    impName = "768/FULL/ZOMBIE/ZOMBIE_{CH}_IMP/ZOMBIE_{CH}_IMP.PAM";
                }
            }
        }
        ZombieVisualDef impDef = new ZombieVisualDef(
            List.of(new PamSpec(impName)),
            Collections.emptyList()
        );
        register("IMP", impDef);
        register("IMP_ZOMBIE", impDef);

        ZombieVisualDef allstarDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_MODERN_ALLSTAR/ZOMBIE_MODERN_ALLSTAR.PAM")),
            Collections.emptyList()
        );
        register("ALLSTAR", allstarDef);
        register("ALL_STAR", allstarDef);
        register("ALL_STAR_ZOMBIE", allstarDef);

        ZombieVisualDef parasolDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_JANE/ZOMBIE_LOSTCITY_JANE.PAM")),
            Collections.emptyList()
        );
        register("PARASOL", parasolDef);
        register("PARASOL_ZOMBIE", parasolDef);

        ZombieVisualDef turquoiseDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_CRYSTALSKULL/ZOMBIE_LOSTCITY_CRYSTALSKULL.PAM")),
            Collections.emptyList()
        );
        register("TURQUOISE", turquoiseDef);
        register("TURQUOISE_ZOMBIE", turquoiseDef);

        ZombieVisualDef newspaperDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_MODERN_NEWSPAPER/ZOMBIE_MODERN_NEWSPAPER.PAM")),
            Collections.emptyList()
        );
        register("NEWSPAPER", newspaperDef);
        register("NEWSPAPER_ZOMBIE", newspaperDef);

        ZombieVisualDef prospectorDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_PROSPECTOR/ZOMBIE_PROSPECTOR.PAM")),
            Collections.emptyList()
        );
        register("PROSPECTOR", prospectorDef);
        register("PROSPECTOR_ZOMBIE", prospectorDef);

        ZombieVisualDef pianistDef = new ZombieVisualDef(
            List.of(
                PamSpec.fixed("768/FULL/ZOMBIE/PIANO/PIANO.PAM"),
                PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_PIANO/ZOMBIE_PIANO.PAM")
            ),
            Collections.emptyList()
        );
        register("PIANIST", pianistDef);
        register("PIANIST_ZOMBIE", pianistDef);
        register("PIANO", pianistDef);

        ZombieVisualDef barrelDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_PIRATE_BARREL_PUSHER/ZOMBIE_PIRATE_BARREL_PUSHER.PAM")),
            Collections.emptyList()
        );
        register("BARREL_PUSHER", barrelDef);
        register("BARREL_ROLLER", barrelDef);
        register("BARREL_ZOMBIE", barrelDef);

        ZombieVisualDef explorerDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/INITIAL/ZOMBIE/ZOMBIE_EXPLORER/ZOMBIE_EXPLORER.PAM")),
            Collections.emptyList()
        );
        register("EXPLORER", explorerDef);
        register("EXPLORER_ZOMBIE", explorerDef);
        register("TORCHLIGHT", explorerDef);

        ZombieVisualDef tombraiserDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_TOMBRAISER/ZOMBIE_{CH}_TOMBRAISER.PAM")),
            Collections.emptyList()
        );
        register("TOMBRAISER", tombraiserDef);
        register("TOMB_RAISER", tombraiserDef);
        register("TOMBRAISER_ZOMBIE", tombraiserDef);

        ZombieVisualDef dodoDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_ICEAGE_DODORIDER/ZOMBIE_ICEAGE_DODORIDER.PAM")),
            Collections.emptyList()
        );
        register("DODORIDER", dodoDef);
        register("DODO_RIDER", dodoDef);
        register("DODORIDER_ZOMBIE", dodoDef);

        ZombieVisualDef hunterDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_ICEAGE_HUNTER/ZOMBIE_ICEAGE_HUNTER.PAM")),
            Collections.emptyList()
        );
        register("HUNTER", hunterDef);
        register("HUNTER_ZOMBIE", hunterDef);

        ZombieVisualDef snorkelerDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_BEACH_SNORKELER/ZOMBIE_BEACH_SNORKELER.PAM")),
            Collections.emptyList()
        );
        register("SNORKELER", snorkelerDef);
        register("SNORKEL_ZOMBIE", snorkelerDef);
        register("DIVER", snorkelerDef);

        ZombieVisualDef octopusDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_BEACH_OCTOPUS/ZOMBIE_BEACH_OCTOPUS.PAM")),
            Collections.emptyList()
        );
        register("OCTOPUS", octopusDef);
        register("OCTOPUS_ZOMBIE", octopusDef);

        ZombieVisualDef impDragonDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_DARK_IMP_DRAGON/ZOMBIE_DARK_IMP_DRAGON.PAM")),
            Collections.emptyList()
        );
        register("IMP_DRAGON", impDragonDef);
        register("IMP_DRAGON_ZOMBIE", impDragonDef);

        ZombieVisualDef fishermanDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_BEACH_FISHERMAN/ZOMBIE_BEACH_FISHERMAN.PAM")),
            Collections.emptyList()
        );
        register("FISHERMAN", fishermanDef);
        register("FISHERMAN_ZOMBIE", fishermanDef);

        ZombieVisualDef explosiveDeathDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/EFFECTS/ZOMBIE_ASH/ZOMBIE_ASH.PAM") {
                @Override
                public String getResolvedPath() {
                    return this.pathTemplate;
                }
            }),
            Collections.emptyList()
        );
        register("EXPLOSIVE_DEATH", explosiveDeathDef);
        register("ASH_EFFECT", explosiveDeathDef);

        ZombieVisualDef jesterDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_DARK_JESTER/ZOMBIE_DARK_JESTER.PAM")),
            Collections.emptyList()
        );
        register("JESTER", jesterDef);
        register("JESTER_ZOMBIE", jesterDef);

        ZombieVisualDef wizardDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_DARK_WIZARD/ZOMBIE_DARK_WIZARD.PAM")),
            Collections.emptyList()
        );
        register("WIZARD", wizardDef);
        register("WIZARD_ZOMBIE", wizardDef);

        ZombieVisualDef kingDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_DARK_KING/ZOMBIE_DARK_KING.PAM")),
            Collections.emptyList()
        );
        register("KING", kingDef);
        register("KING_ZOMBIE", kingDef);

        ZombieVisualDef troglobiteDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_ICEAGE_TROGLOBITE/ZOMBIE_ICEAGE_TROGLOBITE.PAM")),
            Collections.emptyList()
        );
        register("TROGLOBITE", troglobiteDef);
        register("TROGLOBITE_ZOMBIE", troglobiteDef);

        ZombieVisualDef arcadeDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_MODERN_ARCADE/ZOMBIE_MODERN_ARCADE.PAM")),
            Collections.emptyList()
        );
        register("ARCADE", arcadeDef);
        register("ARCADE_ZOMBIE", arcadeDef);

        ZombieVisualDef zombossDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_ZOMBOSS/ZOMBIE_EGYPT_ZOMBOSS.PAM")),
            Collections.emptyList()
        );
        register("ZOMBOSS_IN_EGYPT", zombossDef);
        register("ZOMBOSS", zombossDef);

        ZombieVisualDef darkZombossDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_DARK_ZOMBOSS/ZOMBIE_DARK_ZOMBOSS.PAM")),
            Collections.emptyList()
        );
        register("ZOMBOSS_IN_DARK", darkZombossDef);
    }
}
