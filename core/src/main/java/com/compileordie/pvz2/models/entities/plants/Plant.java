package com.compileordie.pvz2.models.entities.plants;

import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.plants.factory.FoodEffectFactory;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.AttackStrategy;
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

    // Special flags for specific AI behaviors
    private boolean targetsHighestHp = false;

    private AttackStrategy attackStrategy;
    private PlantFoodEffectStrategy foodStrategy;
    private Map<Integer, UpgradeLevel> upgradeMap;

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
        this.attackStrategy = attackStrategy;
        this.foodStrategy = foodStrategy;
        this.upgradeMap = upgradeMap;
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

        currentActionTimer += tickDelta;

        // 2. Execute attack strategy
        if (currentActionTimer >= actionIntervalTicks) {
            if (attackStrategy != null) {
                attackStrategy.attack(this, board, tickDelta);
            }
            currentActionTimer = 0;
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

        // Scan the 8 surrounding tiles for a FIRE plant
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (r == row && c == col) continue;

                if (r >= 0 && r < board.totalRows && c >= 0 && c < board.totalCols) {
                    Tile adjacentTile = board.getTile(r, c);
                    if (adjacentTile != null && adjacentTile.plant != null) {
                        if (adjacentTile.plant.hasTag(PlantTag.FIRE)) {
                            hasAdjacentFire = true;
                            break;
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

    public void applyLevelUpgrade(int newLevel) {
        this.level = newLevel;
        if (upgradeMap != null && upgradeMap.containsKey(newLevel)) {
            UpgradeLevel stats = upgradeMap.get(newLevel);

            this.baseHp += stats.hpBonus;
            this.currentHp += stats.hpBonus;
            this.baseDamage += stats.damageBonus;
            this.cost = Math.max(0, this.cost - stats.costReduction);
            this.actionIntervalTicks = Math.max(1.0, this.actionIntervalTicks - stats.cooldownReductionTicks);

            if (stats.targetPriorityUp) {
                this.targetsHighestHp = true;
            }
        }
    }

    public void die() {
        // Explosive plants, Mints, and cleanup logic hook into this!
    }

    // --- Standard Getters & Setters ---
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
}
