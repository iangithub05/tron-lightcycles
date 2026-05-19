package com.example.utils;

import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

public class InputManager {

    private static final Set<KeyCode> pressed = new HashSet<>();

    public static void press(KeyCode key) {
        pressed.add(key);
    }

    public static void release(KeyCode key) {
        pressed.remove(key);
    }

    public static boolean isDown(KeyCode key) {
        return pressed.contains(key);
    }

    public static void clear() {
        pressed.clear();
    }
}
