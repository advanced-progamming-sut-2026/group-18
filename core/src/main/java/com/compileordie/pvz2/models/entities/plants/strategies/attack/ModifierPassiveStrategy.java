package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType; // Assuming this exists

import java.util.List;

public class ModifierPassiveStrategy implements AttackStrategy {

    public enum ModifierType { REDIRECT_ON_EAT, ATTRACT_LANE, STEAL_METAL }

    private final ModifierType modifierType;
    private final double rangeTiles;
    private double cooldownTimer = 0;

    public ModifierPassiveStrategy(ModifierType modifierType, double rangeTiles) {
        this.modifierType = modifierType;
        this.rangeTiles = rangeTiles;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        List<Zombie> zombies = board.getAllZombies();

        switch (modifierType) {
            case ATTRACT_LANE:
                // Sweet Potato: Constantly pull zombies from adjacent lanes into this lane
                for (Zombie zombie : zombies) {
                    if (zombie.isDead()) continue;
                    double distanceX = Math.abs(zombie.getX() - plant.getX());
                    double distanceY = Math.abs(zombie.getY() - plant.getY());

                    if (distanceX <= rangeTiles && distanceY > 0.1 && distanceY <= 1.5) {
                        // Force zombie to move into the plant's lane (Y coordinate)
                        zombie.setY(plant.getY());
                    }
                }
                break;

            case STEAL_METAL:
                // Magnet-shroom: Periodically rip armor off a zombie
                cooldownTimer += tickDelta;
                if (cooldownTimer >= 15.0) { // e.g., 15 ticks cooldown
                    for (Zombie zombie : zombies) {
                        if (zombie.isDead()) continue;
                        double distance = Math.hypot(zombie.getX() - plant.getX(), zombie.getY() - plant.getY());

                        if (distance <= rangeTiles && zombie.hasMetalArmor()) { // Assuming hasMetalArmor() exists
                            zombie.removeMetalArmor();
                            cooldownTimer = 0; // Reset cooldown after stealing
                            break; // Only steal one piece of metal at a time
                        }
                    }
                }
                break;

            case REDIRECT_ON_EAT:
                // Garlic: This is usually handled inside the takeDamage() method of Plant.java
                // But if implemented here, it actively scans for zombies overlapping it and pushes them
                for (Zombie zombie : zombies) {
                    if (zombie.isDead()) continue;
                    double distance = Math.hypot(zombie.getX() - plant.getX(), zombie.getY() - plant.getY());

                    if (distance <= 0.5) {
                        // Push zombie to lane above or below
                        double newY = plant.getY() + (Math.random() > 0.5 ? 1.0 : -1.0);
                        zombie.setY(newY);
                    }
                }
                break;
        }
    }
}
