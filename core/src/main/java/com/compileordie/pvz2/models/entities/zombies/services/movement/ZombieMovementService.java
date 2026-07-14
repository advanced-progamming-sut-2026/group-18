package com.compileordie.pvz2.models.entities.zombies.services.movement;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.maps.GameMap;
import com.compileordie.pvz2.models.entities.plants.variants.Plant;
import java.util.List;

public interface ZombieMovementService {
    void moveAll(List<Zombie> zombies, GameMap map, List<Plant> plants, double delta);
}
