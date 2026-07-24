package com.compileordie.pvz2.models.game.effects;

import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.utils.Toolbox;

public enum EffectType {
    STANDARD {
        @Override
        public void tick(int ticks, EffectModifier self, GameBoard gameBoard) {
        }
    },
    FROSTBITE_CAVES {
        @Override
        public void tick(int ticks, EffectModifier self, GameBoard gameBoard) {
        }
    };
    // TODO: Add more side effects needed, here.

    abstract public void tick(int ticks, EffectModifier self, GameBoard gameBoard);

    public static EffectType getByName(String name) {
        for (EffectType effectType : EffectType.values()) {
            if (effectType.toString().equalsIgnoreCase(name)) {
                return effectType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
