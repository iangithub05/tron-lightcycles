package com.example;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class ConfigScreen {

    private final Main app;

    public ConfigScreen(Main app) {
        this.app = app;
    }

    public Pane getView() {
        StackPane layout = new StackPane();
        layout.setBackground(UIHelper.createBackground("/images/background_main.png"));

        
        
        return layout;
    }
}