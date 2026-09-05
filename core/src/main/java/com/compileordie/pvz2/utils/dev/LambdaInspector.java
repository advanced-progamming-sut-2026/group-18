package com.compileordie.pvz2.utils.dev;

import com.compileordie.pvz2.models.game.board.GameBoard;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class LambdaInspector {
    private static final Set<Class<?>> VISITED = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Scanning object graph for interfaces and anonymous classes...");
        inspect(GameBoard.class, "GameBoard");
    }

    private static void inspect(Class<?> clazz, String path) {
        if (clazz == null || clazz.isPrimitive() || clazz.getName().startsWith("java.")) return;
        if (!VISITED.add(clazz)) return;

        Class<?> current = clazz;
        while (current != null && !current.equals(Object.class)) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue; // Kryo already ignores static and transient fields
                }

                Class<?> fieldType = field.getType();
                String fieldPath = path + " -> " + field.getName() + " (" + fieldType.getName() + ")";

                // Flag interfaces (where lambdas are assigned) and inner/anonymous classes
                if (fieldType.isInterface() || fieldType.getName().contains("$")) {
                    System.out.println("⚠️ SUSPICIOUS FIELD: " + fieldPath);
                }

                inspect(fieldType, fieldPath);
            }
            current = current.getSuperclass();
        }
    }
}
