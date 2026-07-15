package com.compileordie.pvz2.models.entities.zombies.services.effects;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext;

public interface ZombieStatusEffectService {

    /**
     * جلو بردن زمان (Elapsed Ticks) افکت‌های فعال روی زامبی‌ها و بازگرداندن سرعت زامبی به حالت عادی پس از انقضا
     */
    void updateActiveEffects(ZombieTickContext context);

    /**
     * اعمال یک افکت جدید (FROZEN, POISONED, HYPNOTIZED, STUNNED) روی زامبی و اعمال تغییرات فوری در ویژگی‌ها
     */
    void applyNewEffect(Zombie zombie, StatusEffect effect, ZombieTickContext context);

    /**
     * حذف اجباری یک افکت از روی زامبی (مثلاً خنثی شدن افکت یخ با افکت آتش تیرها)
     */
    void forceRemoveEffect(Zombie zombie, EffectType effectType);

    /**
     * مخصوص زامبی روزنامه‌به‌دست و فرعون؛ به محض نابود شدن روزنامه/تابوت،
     * سرعت حرکت و نرخ حمله زامبی به صورت دائمی به شدت افزایش می‌یابد (حالت خشم).
     */
    void handleRageMechanic(Zombie zombie);
}
