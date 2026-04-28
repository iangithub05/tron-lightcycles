package com.example.input;

import javafx.scene.input.KeyCode;
import java.util.HashSet;
import java.util.Set;

public class InputManager {
    private static final Set<KeyCode> pressed = new HashSet<>();

    public static void press(KeyCode code) {
        pressed.add(code);
    }

    public static void release(KeyCode code) {
        pressed.remove(code);
    }

    public static boolean isDown(KeyCode code) {
        return pressed.contains(code);
    }

    public static void clear() {
        pressed.clear();
    }
}
