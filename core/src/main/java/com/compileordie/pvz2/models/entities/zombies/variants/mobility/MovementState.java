package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

public enum MovementState {
    WALKING,      // حرکت عادی به سمت چپ
    CHARGING,     // دویدن/شارژ (مثل معدنچی قبل انفجار)
    FLYING,       // پرواز کردن/پریدن (مثل دودوسوار)
    UNDERWATER,   // زیر آب بودن (مثل غواص)
    EATING        // متوقف شده برای جویدن گیاه
}
