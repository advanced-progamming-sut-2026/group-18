package com.compileordie.pvz2.models.game.judges;

import com.compileordie.pvz2.utils.Toolbox;

public enum GameFlow {
    CONTINUE,
    WIN,
    LOSS;

    public static GameFlow getByName(String name) {
        for (GameFlow gameFlow : GameFlow.values()) {
            if (gameFlow.toString().equalsIgnoreCase(name)) {
                return gameFlow;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
