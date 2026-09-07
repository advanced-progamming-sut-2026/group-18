package com.compileordie.pvz2.models.entities.plants;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.AttackStrategy;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.SunProduceStrategy;
import com.compileordie.pvz2.models.entities.plants.strategies.food.PlantFoodEffectStrategy;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;
import com.compileordie.pvz2.models.user.Player;
import java.util.List;
import java.util.Map;
enum PlantCoverState {
    NONE, ICE, OCTOPUS
}
public class Plant extends GameEntity {
    public boolean isFiringButter = false;
    public double plantFoodTimer = 0;
    public double sunDropCooldown = 0.0;
    private PlantCoverState coverState = PlantCoverState.NONE;
    private double coverHp = 0;
    private int chillLevel = 0;
    private boolean isSpecial;
    private String name;
    private PlantCategory category;
    private List<PlantTag> tags;
    private int baseHp;
    public boolean isFed = false;
    private int currentHp;
    private int baseDamage;
    private int cost;
    private double actionIntervalTicks;
    private double currentActionTimer = 0;
    private int level = 1;
    public PlantTemplate template;
    public boolean holdAction = false;
    private double atkSpeedBonusPercentage = 0.0;
    private double maxLifespanTicks = -1;
    private double currentLifespanTicks = -1;
    private double rangeBonus = 0.0;
    private boolean doubleSunChance = false;
    private boolean targetsHighestHp = false;
    private int extraSunYield = 0;
    private boolean isHidden = false;
    private boolean isExhausted = false;
    private int extraCrushes = 0;
    public boolean isShootingForward = false;
    public boolean isShootingBackward = false;
    private double ageTicks = 0;
    private double growTimeReductionTicks = 0;
    public double getAgeTicks() { return ageTicks; }
    public double getGrowTimeReductionTicks() { return growTimeReductionTicks; }
    private AttackStrategy attackStrategy;
    private PlantFoodEffectStrategy foodStrategy;
    private Map<Integer, UpgradeLevel> upgradeMap;
    private int stackCount = 1;
    private double chillTimeBonusTicks = 0;
    public java.util.LinkedList<Integer> bulbs = new java.util.LinkedList<>(java.util.Arrays.asList(1, 2, 3));
    public double bulbRegenTimer = 0;
    public boolean isWindingUp = false;
    public double windupTimer = 0;
    public int currentlyFiringBulb = 0;
    private double actionIntervalReductionTicks = 0.0;
    private int pierceBonus = 0;
    private int poisonDmgTickBonus = 0;
    private double plantFoodChance = 0.0;
    private double butterChance = 0.0;
    private int aoeDamageBonus = 0;
    private int warmthRadiusBonus = 0;
    private double maxArmTimeTicks = 0;
    private double currentArmTimer = 0;
    private boolean isArmed = true;
    private int extraBounces = 0;
    private int extraTargets = 0;
    private double freezeTimeBonusTicks = 0;
    private int maxSizeBonus = 0;
    private boolean isMaxStageForced = false;
    private boolean isBlueFlame = false;
    private boolean explodesOnDeath = false;
    private boolean zombieHpBuff = false;
    private boolean zombieDmgBuff = false;
    private boolean plantFoodOnSpawn = false;
    private boolean meltArea3x3 = false;
    private double mintDurationBonusTicks = 0.0;
    private boolean resetFamilyCooldowns = false;
    private boolean isBoosted;

    public Plant(String name,
                 PlantCategory category,
                 List<PlantTag> tags,
                 double x,
                 double y,
                 int hp,
                 int damage,
                 int cost,
                 double actionIntervalTicks,
                 AttackStrategy attackStrategy,
                 PlantFoodEffectStrategy foodStrategy,
                 Map<Integer, UpgradeLevel> upgradeMap) {
        super(x, y, 0, 0);
        this.name = name;
        this.category = category;
        this.tags = tags;
        this.baseHp = hp;
        this.currentHp = hp;
        this.baseDamage = damage;
        this.cost = cost;
        this.actionIntervalTicks = actionIntervalTicks;
        if (attackStrategy instanceof SunProduceStrategy || this.name.equals("Citron")) { this.currentActionTimer = 0; }
        else { this.currentActionTimer = actionIntervalTicks; }
        this.attackStrategy = attackStrategy;
        this.foodStrategy = foodStrategy;
        this.upgradeMap = upgradeMap;
        if (this.name.equals("Mega Gatling Pea")) {
            this.plantFoodChance = 0.0;
        }
        if (this.name.equals("Puff-shroom") || this.name.equals("Sea-shroom")) {
            this.maxLifespanTicks = 600.0;
            this.currentLifespanTicks = 600.0;
        }
        if (this.name.equals("Kernel-pult")) {
            this.butterChance = 25.0;
        }
        if (this.name.equals("Potato Mine")) {
            this.maxArmTimeTicks = 150.0;
            this.isArmed = false;
        } else if (this.name.equals("Primal Potato Mine")) {
            this.maxArmTimeTicks = 50.0;
            this.isArmed = false;
        }
    }
    public void tick(GameBoard board, int tickDelta) {
        if (this.isDead() || this.currentHp <= 0) {
            this.die();
            return;
        }
        if (this.sunDropCooldown > 0) {
            this.sunDropCooldown -= tickDelta;
        }
        if (coverState != PlantCoverState.NONE) {
            if (coverState == PlantCoverState.ICE) {
                handleIceMelting(board, tickDelta);
            }
            return;
        }
        if (this.isFed) {
            this.plantFoodTimer += tickDelta;
            return;
        }
        if (isBoosted) {
            feed(board, AppModel.player);
            isBoosted = false;
        }
        this.ageTicks += tickDelta;
        if (this.maxLifespanTicks > 0) {
            this.currentLifespanTicks -= tickDelta;
            if (this.currentLifespanTicks <= 0) {
                this.die();
                return;
            }
        }
        if (this.name.equals("Bowling Bulb") && bulbs.size() < 3) {
            bulbRegenTimer += tickDelta;
            if (bulbRegenTimer >= getRegenThreshold()) {
                int growingBulb = 1;
                if (!bulbs.contains(1)) growingBulb = 1;
                else if (!bulbs.contains(2)) growingBulb = 2;
                else if (!bulbs.contains(3)) growingBulb = 3;
                bulbs.add(growingBulb);
                java.util.Collections.sort(bulbs);
                bulbRegenTimer = 0;
            }
        }
        currentActionTimer += tickDelta;
        if (!this.isArmed) {
            this.currentArmTimer += tickDelta;
            if (this.currentArmTimer >= this.maxArmTimeTicks) {
                this.isArmed = true;
            }
            return;
        }
        if (currentActionTimer >= actionIntervalTicks) {
            this.holdAction = false;
            if (this.plantFoodChance > 0 && (Math.random() * 100 < this.plantFoodChance)) {
                this.feed(board, null);
            } else if (attackStrategy != null) {
                attackStrategy.attack(this, board, tickDelta);
            }
            if(!this.holdAction) {
                currentActionTimer = 0;
            }
        }
    }
    public void addChill() {
        if (hasTag(PlantTag.FIRE) || coverState != PlantCoverState.NONE) return;

        chillLevel++;
        if (chillLevel >= 1) {
            this.coverState = PlantCoverState.ICE;
            this.coverHp = 600.0;
            this.chillLevel = 0;
            this.holdAction = true;
        }
    }
    public void applyOctopus(double octopusHp) {
        if (coverState != PlantCoverState.NONE) return;
        this.coverState = PlantCoverState.OCTOPUS;
        this.coverHp = octopusHp;
        this.chillLevel = 0;
    }
    public boolean hasActiveCover() {
        return this.coverState != PlantCoverState.NONE;
    }
    private void handleIceMelting(GameBoard board, int tickDelta) {
        Tile currentTile = board.getTile((float) this.getX(), (float) this.getY());
        if (currentTile == null) return;
        boolean hasAdjacentFire = false;
        int row = currentTile.row;
        int col = currentTile.column;
        for (int r = row - 2; r <= row + 2; r++) {
            for (int c = col - 2; c <= col + 2; c++) {
                if (r == row && c == col) continue;
                if (r >= 0 && r < board.totalRows && c >= 0 && c < board.totalCols) {
                    Tile adjacentTile = board.getTile(r, c);
                    if (adjacentTile != null && adjacentTile.plant != null) {
                        Plant checkingPlant = adjacentTile.plant;
                        if (checkingPlant.hasTag(PlantTag.FIRE)) {
                            int effectiveRadius;
                            if (checkingPlant.getWarmthRadius() > 0) {
                                effectiveRadius = checkingPlant.getWarmthRadius();
                            } else {
                                effectiveRadius = 1;
                            }
                            if (Math.abs(r - row) <= effectiveRadius && Math.abs(c - col) <= effectiveRadius) {
                                hasAdjacentFire = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (hasAdjacentFire) break;
        }
        if (hasAdjacentFire) {
            double meltAmount = (60.0 / 10.0) * tickDelta;
            takeDamage((int) meltAmount);
        }
    }
    public void feed(GameBoard board, Player player) {
        if (this.isFed || hasActiveCover()) return;
        this.isFed = true;
        this.plantFoodTimer = 0;
        if (this.foodStrategy != null) {
            float delaySeconds = (float) (this.getFoodWindupDelay() * Constants.Game.TIME_COEFFICIENT);
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    if (!isDead()) {
                        foodStrategy.applyEffect(Plant.this, board, player);
                    }
                }
            }, delaySeconds);
        } else {
            System.out.println("DEBUG: " + this.name + " has no Plant Food Strategy mapped!");
        }
    }
    public void resetFeed() {
        this.isFed = false;
        this.plantFoodTimer = 0;
    }
    public boolean isFed() {
        return this.isFed;
    }
    public void takeDamage(int amount) {
        if (this.isDead()) return;
        if (this.name.equals("Sun Bean")) {
            if (this.sunDropCooldown <= 0) {
                if (AppModel.gameSession != null && AppModel.gameSession.gameBoard != null) {
                    int sunAmount = 5 + this.getExtraSunYield();
                    if (sunAmount > 5) {
                        AppModel.gameSession.gameBoard.economyManager.suns.add(new Sun(this.getX(),
                            this.getY(),
                            SunType.SMALL,
                            false,
                            (float) this.getY()));
                    }
                    AppModel.gameSession.gameBoard.economyManager.suns.add(new Sun(this.getX(),
                        this.getY(),
                        SunType.TINY,
                        false,
                        (float) this.getY()));
                }
                this.sunDropCooldown = 10.0;
            }
        }
        if (coverState != PlantCoverState.NONE) {
            this.coverHp -= amount;
            if (this.coverHp <= 0) {
                this.coverState = PlantCoverState.NONE;
                this.coverHp = 0;
                this.chillLevel = 0;
                this.holdAction = false;
            }
            return;
        }
        boolean hadArmor = (this.currentHp > this.baseHp);
        this.currentHp -= amount;
        if (hadArmor && this.currentHp <= this.baseHp && this.currentHp > 0) {
            if (this.name.equals("Explode-o-nut")) {
                this.triggerExplosion();
            }
        }
        if (this.currentHp <= 0) {
            this.currentHp = 0;
            this.die();
        }
    }
    public void applyLevelUpgrade(int targetLevel) {
        for (int i = 2; i <= targetLevel; i++) {
            if (upgradeMap != null && upgradeMap.containsKey(i)) {
                UpgradeLevel stats = upgradeMap.get(i);
                if (stats.doubleSunChance) this.doubleSunChance = true;
                if (stats.targetPriorityUp) this.targetsHighestHp = true;
                if (this.maxLifespanTicks > 0) {
                    this.maxLifespanTicks += stats.lifespanBonusTicks;
                    this.currentLifespanTicks += stats.lifespanBonusTicks;
                }
                this.baseHp += stats.hpBonus;
                this.currentHp += stats.hpBonus;
                this.baseDamage += stats.damageBonus;
                this.cost = Math.max(0, this.cost - stats.costReduction);
                this.actionIntervalTicks = Math.max(0.1, this.actionIntervalTicks - stats.actionIntervalReductionTicks);
                this.growTimeReductionTicks += stats.growTimeReductionTicks;
                this.extraSunYield += stats.extraSunYield;
                this.chillTimeBonusTicks += stats.chillTimeBonusTicks;
                this.actionIntervalReductionTicks += stats.actionIntervalReductionTicks;
                this.pierceBonus += stats.pierceBonus;
                this.atkSpeedBonusPercentage += stats.atkSpeedBonusPercentage;
                this.poisonDmgTickBonus += stats.poisonDmgTickBonus;
                this.plantFoodChance += stats.plantFoodChanceBonus;
                this.rangeBonus += stats.rangeBonus;
                this.butterChance += stats.butterChanceBonus;
                this.aoeDamageBonus += stats.aoeDamageBonus;
                this.warmthRadiusBonus += stats.warmthRadiusBonus;
                this.maxArmTimeTicks = Math.max(0, this.maxArmTimeTicks - stats.armTimeReductionTicks);
                this.extraCrushes += stats.extraCrushes;
                this.extraBounces += stats.extraBounces;
                this.extraTargets += stats.extraTargets;
                this.freezeTimeBonusTicks += stats.freezeTimeBonusTicks;
                this.maxSizeBonus += stats.maxSizeBonus;
                if (stats.explodesOnDeath) this.explodesOnDeath = true;
                if (stats.zombieHpBuff) this.zombieHpBuff = true;
                if (stats.zombieDmgBuff) this.zombieDmgBuff = true;
                if (stats.plantFoodOnSpawn) this.plantFoodOnSpawn = true;
                if (stats.meltArea3x3) this.meltArea3x3 = true;
                this.mintDurationBonusTicks += stats.mintDurationBonusTicks;
                if (stats.resetFamilyCooldowns) this.resetFamilyCooldowns = true;
            }
        }
        if (this.atkSpeedBonusPercentage > 0) {
            this.actionIntervalTicks
                = Math.max(1.0, this.actionIntervalTicks * (1.0 - (this.atkSpeedBonusPercentage / 100.0)));
        }
        this.level = targetLevel;
    }
    @Override
    public void die() {
        super.die();
        if (isSpecial) {
            AppModel.gameSession.gameBoard.specialIsLost = true;
        }
        if (this.name.equals("Explode-o-nut") || (this.name.equals("Torchwood") && this.explodesOnDeath)) {
            this.triggerExplosion();
        }
        int xInt = (int) this.getX();
        int yInt = (int) this.getY();
    }
    public int getBulbCount() { return bulbs.size(); }
    public void consumeBulb() {}
    public void reloadAllBulbs() {
        this.bulbs.clear();
        this.bulbs.addAll(java.util.Arrays.asList(1, 2, 3));
        this.bulbRegenTimer = 0;
    }
    public int getStackCount() { return stackCount; }
    public void addStack() { if (this.stackCount < 5) this.stackCount++; }
    public double getChillTimeBonusTicks() { return chillTimeBonusTicks; }
    public PlantFoodEffectStrategy getFoodEffectStrategy() { return foodStrategy; }
    public AttackStrategy getAttackStrategy() { return attackStrategy; }
    public int getBaseHp() { return baseHp; }
    public void setBaseHp(int baseHp) { this.baseHp = baseHp; }
    public void setActionIntervalTicks(double actionIntervalTicks) { this.actionIntervalTicks = actionIntervalTicks; }
    public String getName() { return name; }
    public PlantCategory getCategory() { return category; }
    public List<PlantTag> getTags() { return tags; }
    public boolean hasTag(PlantTag tag) { return tags != null && tags.contains(tag); }
    public int getBaseDamage() { return baseDamage; }
    public void setBaseDamage(int baseDamage) { this.baseDamage = baseDamage; }
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int hp) { this.currentHp = hp; }
    public boolean isDead() { return this.currentHp <= 0; }
    public boolean targetsHighestHp() { return targetsHighestHp; }
    public int getCost() { return cost; }
    public int getLevel() { return level; }
    public boolean isSpecial() { return isSpecial; }
    public void setSpecial() { isSpecial = true; }
    public boolean hasDoubleSunChance() { return this.doubleSunChance; }
    public int getExtraSunYield() { return extraSunYield; }
    public int getPierceBonus() { return pierceBonus; }
    public int getPoisonDmgTickBonus() { return poisonDmgTickBonus; }
    public double getRangeTiles() {
        double baseRange = (template != null ? template.getRangeTiles() : 10.0);
        if (this.name.equals("Sea-shroom") || this.name.equals("Puff-shroom") || this.name.equals("Fume-shroom")) {
            baseRange = 4.0;
        }
        return baseRange + rangeBonus;
    }
    public void resetLifespan() {
        if (this.maxLifespanTicks > 0) this.currentLifespanTicks = this.maxLifespanTicks;
    }
    public double getButterChance() { return butterChance; }
    public int getAoeDamage() { return (this.baseDamage / 2) + this.aoeDamageBonus; }

    public int getWarmthRadius() {
        return (this.name.equals("Pepper-pult") || this.name.equals("Wasabi Whip") ? 1 : 0)
            + this.warmthRadiusBonus;
    }
    public boolean isArmed() { return isArmed; }
    public void forceArm() { this.isArmed = true; }
    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { this.isHidden = hidden; }
    public boolean isExhausted() { return isExhausted; }
    public void setExhausted(boolean exhausted) { this.isExhausted = exhausted; }
    public int getExtraCrushes() { return extraCrushes; }
    public int getExtraBounces() { return extraBounces; }
    public int getExtraTargets() { return extraTargets; }
    public double getFreezeTimeBonusTicks() { return freezeTimeBonusTicks; }
    public double getCoverHp() { return this.coverHp; }
    public int getMaxSizeBonus() { return maxSizeBonus; }
    public void setBlueFlame(boolean blueFlame) { this.isBlueFlame = blueFlame; }
    public boolean isBlueFlame() { return isBlueFlame; }
    public boolean hasZombieHpBuff() { return zombieHpBuff; }
    public boolean hasZombieDmgBuff() { return zombieDmgBuff; }
    public boolean hasPlantFoodOnSpawn() { return plantFoodOnSpawn; }
    public boolean hasMeltArea3x3() { return meltArea3x3; }
    public boolean isExplodesOnDeath() { return explodesOnDeath; }
    public double getMintDurationBonusTicks() { return mintDurationBonusTicks; }
    public boolean hasResetFamilyCooldowns() { return resetFamilyCooldowns; }
    public double getActionIntervalTicks() { return actionIntervalTicks; }
    public double getCurrentActionTimer() { return currentActionTimer; }
    public int getGrowthStage() {
        if (isMaxStageForced) {
            return 3 + maxSizeBonus;
        }
        double age = this.getAgeTicks();
        double stg2Threshold = Math.max(0, 480.0 - this.getGrowTimeReductionTicks());
        double stg3Threshold = Math.max(0, 1440.0 - this.getGrowTimeReductionTicks());
        if (age >= stg3Threshold) return 3;
        if (age >= stg2Threshold) return 2;
        return 1;
    }
    public boolean isFrozen() {
        return this.coverState == PlantCoverState.ICE;
    }
    public void forceMaxGrowth() {
        this.isMaxStageForced = true;
    }
    public int getReflectDamage() {
        if (this.name.equals("Endurian")) {
            int reflectDmg = this.baseDamage;
            if (this.currentHp > this.baseHp) {
                reflectDmg += 15;
            }
            return reflectDmg;
        }
        if (this.name.equals("Garlic")) {
            return this.baseDamage;
        }
        return 0;
    }
    private void triggerExplosion() {
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null) return;
        GameBoard board = AppModel.gameSession.gameBoard;
        double radiusPixels = 1.5 * Constants.Game.TILE_HEIGHT;
        for (Zombie z : board.getAllZombies()) {
            if (z.isDead()) continue;
            double dist = Math.hypot(z.getX() - this.getX(), z.getY() - this.getY());
            if (dist <= radiusPixels) {
                z.takeDamage(this.baseDamage, DamageType.NORMAL, PlantType.getByName(this.getName()));
            }
        }
    }
    public void applyBoost() {
        this.isBoosted = true;
    }
    public double getRegenThreshold() {
        int growingBulb = 1;
        if (!bulbs.contains(1)) growingBulb = 1;
        else if (!bulbs.contains(2)) growingBulb = 2;
        else if (!bulbs.contains(3)) growingBulb = 3;
        double baseThreshold = (growingBulb == 3) ? 600.0 : (growingBulb == 2 ? 300.0 : 120.0);
        return Math.max(1.0, baseThreshold - this.actionIntervalReductionTicks);
    }
    private double getFoodWindupDelay() {
        switch (this.name) {
            case "Sunflower":
            case "Twin Sunflower":
                return 16.6;
            case "Primal Sunflower":
                return 20.0;
            case "Fume-shroom":
                return 30.0;
            case "Sun-shroom", "Fire Peashooter", "Iceberg Lettuce":
                return 10.0;
            case  "Puff-shroom", "Cactus", "Goo Peashooter":
                return 15.0;
            case "Starfruit", "Sea-shroom" :
                return 12.0;
            case "Snow Pea" :
                return 7.0;
            case "Peashooter", "Repeater", "Threepeater", "Split Pea", "Mega Gatling Pea", "Phat Beet", "Kiwibeast" :
                return 3.0;
            case "Citron":
                return 90.0;
            case "Wall-nut",
                 "Explode-o-nut",
                 "Sun Bean",
                 "Tall-nut",
                 "Torchwood",
                 "Potato Mine",
                 "Primal Potato Mine",
                 "Sweet Potato":
                return 0.0;
            case "Chomper" :
                return 130.0;
            default:
                return 19.0;
        }
    }
}
