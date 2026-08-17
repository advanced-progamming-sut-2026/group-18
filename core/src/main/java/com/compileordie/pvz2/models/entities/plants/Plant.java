package com.compileordie.pvz2.models.entities.plants;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.plants.factory.FoodEffectFactory;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.AttackStrategy;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.SunProduceStrategy;
import com.compileordie.pvz2.models.entities.plants.strategies.food.PlantFoodEffectStrategy;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.user.Player;

import java.util.List;
import java.util.Map;

// Unified state for Ice Blocks and Octopuses!
enum PlantCoverState {
    NONE, ICE, OCTOPUS
}

public class Plant extends GameEntity {

    // --- Crowd Control & Cover Mechanics ---
    private PlantCoverState coverState = PlantCoverState.NONE;
    private double coverHp = 0;
    private int chillLevel = 0; // Reaches 3 -> Turns into ICE
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
    public boolean holdAction = false; // charge plants hold their charge if there are no zombies in the lane!
    private double atkSpeedBonusPercentage = 0.0;
    private double maxLifespanTicks = -1; // -1 means infinite lifespan (like Peashooter)
    private double currentLifespanTicks = -1;
    private double rangeBonus = 0.0;
    // Special flags for specific AI behaviors
    private boolean doubleSunChance = false;
    private boolean targetsHighestHp = false;
    private int extraSunYield = 0;
    private boolean isHidden = false; // True when Squash is jumping!
    private boolean isExhausted = false; // True when Squash is done attacking and becomes a meat shield
    private int extraCrushes = 0; // For the "Can crush 2x" upgrade!
    // life cycle tracking ...
    private double ageTicks = 0;
    private double growTimeReductionTicks = 0;
    public double getAgeTicks() { return ageTicks; }
    public double getGrowTimeReductionTicks() { return growTimeReductionTicks; }
    // life cycle tracking ...
    private AttackStrategy attackStrategy;
    private PlantFoodEffectStrategy foodStrategy;
    private Map<Integer, UpgradeLevel> upgradeMap;
    // NEW: engine variables for our advanced shooters : 1-Pea Pod 2- Snow Pea 3- bowling bulb
    private int stackCount = 1;
    private double chillTimeBonusTicks = 0;
    private int bulbCount = 3;
    private double bulbRegenTimer = 0;
    private double actionIntervalReductionTicks = 0.0;
    private int pierceBonus = 0;
    private int poisonDmgTickBonus = 0;
    private double plantFoodChance = 0.0;
    private double butterChance = 0.0;
    private int aoeDamageBonus = 0;
    private int warmthRadiusBonus = 0;
    private double maxArmTimeTicks = 0;
    private double currentArmTimer = 0;
    private boolean isArmed = true; // True by default for non-traps!
    private int extraBounces = 0;

    public Plant(String name, PlantCategory category, List<PlantTag> tags,
                 double x, double y, int hp, int damage, int cost, double actionIntervalTicks,
                 AttackStrategy attackStrategy, PlantFoodEffectStrategy foodStrategy,
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
        // FIX: Sun producers must WAIT first. Everyone else starts fully charged!
        if (attackStrategy instanceof SunProduceStrategy) { this.currentActionTimer = 0; }
        else { this.currentActionTimer = actionIntervalTicks; }
        this.attackStrategy = attackStrategy;
        this.foodStrategy = foodStrategy;
        this.upgradeMap = upgradeMap;
        if (this.name.equals("Mega Gatling Pea")) {
            this.plantFoodChance = 5.0; // Base 5% chance!
        }
        if (this.name.equals("Puff-shroom") || this.name.equals("Sea-shroom")) {
            this.maxLifespanTicks = 600.0; // 60 Seconds
            this.currentLifespanTicks = 600.0;
        }
        if (this.name.equals("Kernel-pult")) {
            this.butterChance = 25.0;
        }
        if (this.name.equals("Potato Mine")) {
            this.maxArmTimeTicks = 150.0; // 15 seconds
            this.isArmed = false;
        } else if (this.name.equals("Primal Potato Mine")) {
            this.maxArmTimeTicks = 50.0; // 5 seconds
            this.isArmed = false;
        }
    }

    public void tick(GameBoard board, int tickDelta) {
        // GUARANTEED DEATH CHECK 1: If it died via time/decay (like a Mint)
        if (this.isDead() || this.currentHp <= 0) {
            this.die();
            return;
        }

        // 1. If covered by Ice or Octopus, the plant is completely disabled
        if (coverState != PlantCoverState.NONE) {
            // Only Ice melts from nearby fire plants
            if (coverState == PlantCoverState.ICE) {
                handleIceMelting(board, tickDelta);
            }
            return; // Skip attacking while covered!
        }
        // NEW: The plant gets older every single frame!
        this.ageTicks += tickDelta;
        if (this.maxLifespanTicks > 0) {
            this.currentLifespanTicks -= tickDelta;
            if (this.currentLifespanTicks <= 0) {
                this.die();
                return;
            }
        }
        // NEW: Bowling Bulb Ammo Regeneration
        if (this.name.equals("Bowling Bulb") && bulbCount < 3) {
            bulbRegenTimer += tickDelta;

            // Base delays: Cyan (20 ticks), Blue (50 ticks), Orange (100 ticks)
            double regenThreshold = (bulbCount == 2) ? 100.0 : (bulbCount == 1 ? 50.0 : 20.0);

            // Apply the Regen -1s upgrade!
            regenThreshold = Math.max(1.0, regenThreshold - this.actionIntervalReductionTicks);

            if (bulbRegenTimer >= regenThreshold) {
                bulbCount++;
                bulbRegenTimer = 0;
            }
        }

        currentActionTimer += tickDelta;

        // --- THE TRAP ARMING ENGINE ---
        if (!this.isArmed) {
            this.currentArmTimer += tickDelta;
            if (this.currentArmTimer >= this.maxArmTimeTicks) {
                this.isArmed = true;
            }
            // An unarmed trap cannot trigger its attack strategy! (If a zombie reaches it now, the zombie will just eat it normally!)
            return;
        }
        // 2. Execute attack strategy
        if (currentActionTimer >= actionIntervalTicks) {
            this.holdAction = false; // reset the flag

            if (this.plantFoodChance > 0 && (Math.random() * 100 < this.plantFoodChance)) {
                // It won the dice roll! Trigger the ultimate for free!
                this.feed(board, null);
            }

            else if (attackStrategy != null) {
                attackStrategy.attack(this, board, tickDelta);
            }
            // If the strategy didn't request a hold, reset the timer!
            if(!this.holdAction) {
                currentActionTimer = 0;
            }
        }
    }

    // --- Unified Crowd Control API ---

    public void addChill() {
        if (coverState != PlantCoverState.NONE) return;

        chillLevel++;
        if (chillLevel >= 3) {
            this.coverState = PlantCoverState.ICE;
            this.coverHp = 600.0; // Base Ice HP
            this.chillLevel = 0;  // Reset chill stacks
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

                        // If it's a fire plant, check if we are within its specific warmth radius!
                        if (checkingPlant.hasTag(PlantTag.FIRE)) {
                            int effectiveRadius = checkingPlant.getWarmthRadius() > 0 ? checkingPlant.getWarmthRadius() : 1;

                            // Check if this frozen plant is actually inside that radius
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
            // Melts at 60 HP per second (6 HP per tick)
            double meltAmount = (60.0 / 10.0) * tickDelta;
            takeDamage((int) meltAmount); // Damage goes to the cover!
        }
    }

    // ----------------------------------

    public void feed(GameBoard board, Player player) {
        if (this.isFed || hasActiveCover()) return; // Can't feed a frozen plant!

        this.isFed = true;
        if (this.template != null && this.template.getFoodEffectType() != null) {
            PlantFoodEffectStrategy foodStrategy = FoodEffectFactory.createEffect(
                this.template.getFoodEffectType(),
                this.template.getProjectileType(),
                this.template.getFoodEffectValue(),
                this.template
            );
            foodStrategy.applyEffect(this, board, player);
        }
    }

    public void resetFeed() {
        this.isFed = false;
    }

    public boolean isFed() {
        return this.isFed;
    }

    public void takeDamage(int amount) {
        if (this.isDead()) return; // Prevent double-triggering death

        // If the plant has an Ice Block or Octopus, the cover takes the damage!
        if (coverState != PlantCoverState.NONE) {
            this.coverHp -= amount;
            if (this.coverHp <= 0) {
                this.coverState = PlantCoverState.NONE;
                this.coverHp = 0;
            }
            return;
        }

        // Otherwise, the plant takes damage normally
        this.currentHp -= amount;

        // GUARANTEED DEATH CHECK 2: If it died via combat damage
        if (this.currentHp <= 0) {
            this.currentHp = 0;
            this.die();
        }
    }

    public void applyLevelUpgrade(int targetLevel) {
        // Loop from level 2 up to the player's current max level to stack everything!
        for (int i = 2; i <= targetLevel; i++) {
            if (upgradeMap != null && upgradeMap.containsKey(i)) {
                UpgradeLevel stats = upgradeMap.get(i);
                if (stats.doubleSunChance) {
                    this.doubleSunChance = true;
                }
                if (stats.targetPriorityUp) {
                    this.targetsHighestHp = true;
                }
                if (this.maxLifespanTicks > 0) {
                    this.maxLifespanTicks += stats.lifespanBonusTicks;
                    this.currentLifespanTicks += stats.lifespanBonusTicks; // Instantly give the bonus time!
                }
                this.baseHp += stats.hpBonus;
                this.currentHp += stats.hpBonus;
                this.baseDamage += stats.damageBonus;
                this.cost = Math.max(0, this.cost - stats.costReduction);
                this.actionIntervalTicks = Math.max(1.0, this.actionIntervalTicks - stats.actionIntervalReductionTicks);
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
            }
        }
        if (this.atkSpeedBonusPercentage > 0) {
            this.actionIntervalTicks = Math.max(1.0, this.actionIntervalTicks * (1.0 - (this.atkSpeedBonusPercentage / 100.0)));
        }
        this.level = targetLevel;
    }

    @Override
    public void die() {
        super.die();
        AppModel.gameSession.gameBoard.lostPlants++;
        if (isSpecial) {
            AppModel.gameSession.gameBoard.specialIsLost = true;
        }
        // Explosive plants, Mints, and cleanup logic hook into this!

        // Exact string format required by the project document
        int xInt = (int) this.getX();
        int yInt = (int) this.getY();

        // Using teammate's custom message queue
        AppModel.addAfterPrompt("Plant " + this.name + " at (" + xInt + ", " + yInt + ") is destroyed.");
    }

    // NEW: Getters and modifiers for the advanced strategies
    public int getStackCount() { return stackCount; }
    public void addStack() { if (this.stackCount < 5) this.stackCount++; }
    public double getChillTimeBonusTicks() { return chillTimeBonusTicks; }

    // --- Standard Getters & Setters & helpers ---
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
    public int getBulbCount() { return bulbCount; }
    public void consumeBulb() { if (this.bulbCount > 0) this.bulbCount--; }
    public void reloadAllBulbs() { this.bulbCount = 3; this.bulbRegenTimer = 0; }
    public int getPoisonDmgTickBonus() { return poisonDmgTickBonus; }
    public double getRangeTiles() { return (template != null ? template.getRangeTiles() : 10.0) + rangeBonus; }
    public void resetLifespan() {
        if (this.maxLifespanTicks > 0) this.currentLifespanTicks = this.maxLifespanTicks;
    }
    public double getButterChance() { return butterChance; }
    public int getAoeDamage() { return (this.baseDamage / 2) + this.aoeDamageBonus; }
    public int getWarmthRadius() { return (this.name.equals("Pepper-pult") ? 1 : 0) + this.warmthRadiusBonus; }
    public boolean isArmed() { return isArmed; }
    public void forceArm() { this.isArmed = true; } // For the Plant Food effect!
    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { this.isHidden = hidden; }
    public boolean isExhausted() { return isExhausted; }
    public void setExhausted(boolean exhausted) { this.isExhausted = exhausted; }
    public int getExtraCrushes() { return extraCrushes; }
    public int getExtraBounces() { return extraBounces; }
}
