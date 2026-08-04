package com.compileordie.pvz2.models.repositories.configs;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

import java.util.HashMap;

public class ZombiesConfig {
    public HashMap<String, ZombieStatsConfig> zombies = new HashMap<>();

    public ZombieStatsConfig get(ZombieType type) {
        if (type == null) return null;
        ZombieStatsConfig stats = zombies.get(type.name());
        if (stats == null) {
            throw new IllegalArgumentException("No config found for ZombieType: " + type);
        }
        return stats;
    }
}
