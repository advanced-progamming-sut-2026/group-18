package com.compileordie.pvz2.models.zombies;
import com.compileordie.pvz2.models.zombies.types.EffectType;
import com.compileordie.pvz2.models.zombies.variants.Zombie;

public class StatusEffect {
    private final EffectType effectType;
    private final int durationTicks;
    private int elapsedTicks;
    private boolean isApplied;

    public StatusEffect(EffectType effectType, int durationTicks) {
        this.effectType = effectType;
        this.durationTicks = durationTicks;
        this.elapsedTicks = 0;
        this.isApplied = false;
    }

    // بررسی منقضی شدگی افکت
    public boolean isExpired() {
        return elapsedTicks >= durationTicks;
    }

    // اعمال شدن اولیه رو زامبی و ایجاد تغییرات
    public void applyToZombie(Zombie zombie) {
        this.isApplied = true;

        // TODO :
        // باید با توجه به نوع افکت، اثر آن از روی زامبی اعمال شود
        // طبیعتا با استفاده از خود سرویس ها انجام می شود
    }

    public void removeFromZombie(Zombie zombie) {
        this.isApplied = false;

        // TODO :
        // باید با توجه به نوع افکت، اثر آن از روی زامبی برداشته شود
        // طبیعتا بخشی از آن با استفاده از خود سرویس ها انجام می شود
        // بخشی از آن نیز با استفاده از اتمام تیک یا یک تغییری ستینگ در زامبی انجام می شود
    }

    // پیش بردن تیک افکت + اعمال تغییراتی که احیانا حین افکت اعمال می شود
    public void updateZombieTick(Zombie zombie) {
        if (!isApplied) return;

        elapsedTicks++;

        // TODO :
        // باید با توجه به نوع افکت، اثر آن از روی زامبی اعمال شود
        // طبیعتا با استفاده از خود سرویس ها انجام می شود

        if (isExpired()){
            removeFromZombie(zombie);
        }
    }

    // Getters
    public EffectType getEffectType() { return effectType; }
    public int getDurationTicks() { return durationTicks; }
    public int getElapsedTicks() { return elapsedTicks; }
    public boolean isApplied() { return isApplied; }
}
