package com.compileordie.pvz2.models.entities.zombies.services.ability;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext;

public interface ZombieAbilityService {

    /**
     * بررسی کول‌داون (Cooldown) و اجرای توانایی از راه دور زامبی‌های خاص موجود در مپ
     */
    void executeActiveAbilities(ZombieTickContext context);

    /**
     * مخصوص زامبی رع (Ra) و تورکوایز؛ کشیدن و جذب خورشیدهای رها شده روی زمین به سمت خود و ذخیره در کیسه
     */
    void stealSunsFromField(Zombie raZombie, ZombieTickContext context);

    /**
     * مخصوص زامبی جادوگر (Wizard)؛ تبدیل موقت گیاهان زمین به گوسفند/گربه غیرقابل استفاده
     */
    void transformPlantToAnimal(Zombie wizardZombie, ZombieTickContext context);

    /**
     * مخصوص زامبی اختاپوس؛ پرتاب اختاپوس روی گیاه هم‌سطر جهت بلوک کردن شلیک و توانایی‌های آن
     */
    void bindPlantWithOctopus(Zombie octopusZombie, ZombieTickContext context);

    /**
     * مخصوص زامبی شکارچی (Hunter)؛ پرتاب بلوک یخ به سمت گیاهان جهت منجمد کردن آن‌ها
     */
    void freezePlantWithIceBlock(Zombie hunterZombie, ZombieTickContext context);
}
