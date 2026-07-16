package com.compileordie.pvz2.models.missions.shop;

import com.compileordie.pvz2.utils.Toolbox;

public enum CurrencyType {
    COINS,
    DIAMONDS;


    @Override
    public String toString() {
        return Toolbox.enumToString(this, false);
    }
}
