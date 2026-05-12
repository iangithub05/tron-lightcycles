package com.example;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class AboutScreen {

    private final Main app;

    public AboutScreen(Main app) {
        this.app = app;
    }

    public Pane getView() {
        StackPane layout = new StackPane();
        layout.setBackground(UIHelper.createBackground("/images/background_main.png"));

        
        
        return layout;
    }
}