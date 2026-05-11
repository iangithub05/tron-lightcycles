package com.example.utils;

import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

public class InputManager {

    private static final Set<KeyCode> pressedKeys = new HashSet<>();

    public static void press(KeyCode key) {
        pressedKeys.add(key);
    }

    public static void release(KeyCode key) {
        pressedKeys.remove(key);
    }

    public static boolean isDown(KeyCode key) {
        return pressedKeys.contains(key);
    }

    public static void clear() {
        pressedKeys.clear();
    }
}
