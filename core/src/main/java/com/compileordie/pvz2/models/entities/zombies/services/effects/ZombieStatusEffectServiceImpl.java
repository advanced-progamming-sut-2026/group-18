package com.compileordie.pvz2.models.entities.zombies.services.effects;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.StatusEffectType;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext;

public class ZombieStatusEffectServiceImpl implements ZombieStatusEffectService {

    @Override
    public void updateActiveEffects(ZombieTickContext context) {
        // Because Zombie.java calls effect.updateZombieTick(this) inside its own tick(),
        // global updates aren't strictly necessary here unless you want board-wide effects.
        // We leave this available for future global logic.
    }

    @Override
    public void applyNewEffect(Zombie zombie, StatusEffect effect, ZombieTickContext context) {
        if (zombie.isDead()) return;

        // If the zombie is hit by Fire, it cannot be frozen/chilled.
        // This is handled by the Combat engine, but as a safeguard:
        if (effect.getEffectType() == StatusEffectType.FROZEN || effect.getEffectType() == StatusEffectType.CHILLED) {
            // Remove old instances of chill/freeze so they don't stack infinitely
            zombie.removeStatusEffect(StatusEffectType.FROZEN);
            zombie.removeStatusEffect(StatusEffectType.CHILLED);
        }

        zombie.addEffect(effect);
    }

    @Override
    public void forceRemoveEffect(Zombie zombie, StatusEffectType effectType) {
        zombie.removeStatusEffect(effectType);
    }

    @Override
    public void handleRageMechanic(Zombie zombie) {
        if (zombie.isDead()) return;

        // When Newspaper or Pharaoh loses their armor
        double enragedSpeed = zombie.getBaseSpeed() * 2.0;
        zombie.setMovementSpeed(enragedSpeed);
        zombie.recalculateSpeed();
    }
}
