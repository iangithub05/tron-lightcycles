package com.example;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MainMenu {

    private final Main app;

    public MainMenu(Main app) {
        this.app = app;
    }

    public Pane getView() {
        StackPane layout = new StackPane();
        layout.setBackground(UIHelper.createBackground("/images/background_main.png"));

        VBox menuBox = new VBox(20); 
        menuBox.setAlignment(Pos.CENTER);

        // 1. Create the Logo and get the labels inside it
        VBox logo = UIHelper.createLogo("TRON", "LIGHT CYCLES");
        Label mainLabel = (Label) logo.getChildren().get(0);
        Label subLabel = (Label) logo.getChildren().get(1);

        // 2. Create the Buttons
        Button startBtn = UIHelper.createMenuButton("START", 30);
        Button aboutBtn = UIHelper.createMenuButton("ABOUT", 30);
        Button settingsBtn = UIHelper.createMenuButton("SETTINGS", 30);
        Button quitBtn = UIHelper.createMenuButton("QUIT", 30);

        UIHelper.bindFontSize(mainLabel, layout, 80);
        UIHelper.bindFontSize(subLabel, layout, 25);
        UIHelper.bindFontSize(startBtn, layout, 30);
        UIHelper.bindFontSize(aboutBtn, layout, 30);
        UIHelper.bindFontSize(settingsBtn, layout, 30);
        UIHelper.bindFontSize(quitBtn, layout, 30);

        // 3. Logic
        startBtn.setOnAction(e -> app.showMode());
        // aboutBtn.setOnAction(e -> app.showAbout());
        // settingsBtn.setOnAction(e -> app.showConfig());
        quitBtn.setOnAction(e -> System.exit(0));

        menuBox.getChildren().addAll(logo, startBtn, aboutBtn, settingsBtn, quitBtn);
        layout.getChildren().add(menuBox);
        
        return layout;
    }
}