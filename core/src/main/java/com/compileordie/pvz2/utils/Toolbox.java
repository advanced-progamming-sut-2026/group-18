package com.compileordie.pvz2.utils;

public class Toolbox {
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            } else {
                c = Character.toLowerCase(c); // Keeps the rest of the word lowercase
            }
            titleCase.append(c);
        }

        return titleCase.toString();
    }

    public static String enumToString(Enum<?> enumType, boolean titleCase) {
        String input = enumType.name().toLowerCase().replace('_', ' ');
        if (titleCase) {
            return toTitleCase(input);
        } else {
            return input;
        }
    }
}
