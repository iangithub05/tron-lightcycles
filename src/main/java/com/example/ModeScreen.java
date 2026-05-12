package com.example;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ModeScreen {

    private final Main app;
    private VBox menuBox;

    public ModeScreen(Main app) {
        this.app = app;
    }

    public Pane getView() {
        StackPane layout = new StackPane();
        layout.setBackground(UIHelper.createBackground("/images/background_main.png"));

        menuBox = new VBox(15); 
        menuBox.setAlignment(Pos.CENTER);

        // 1. Logo
        VBox logo = UIHelper.createLogo("TRON", "LIGHT CYCLES");
        Label mainLabel = (Label) logo.getChildren().get(0);
        Label subLabel = (Label) logo.getChildren().get(1);

        // 2. Main Buttons
        Button singleBtn = UIHelper.createMenuButton("SINGLEPLAYER", 30);
        Button multiBtn = UIHelper.createMenuButton("MULTIPLAYER", 30);
        Button backBtn = UIHelper.createMenuButton("BACK", 25);

        // 3. Sub-Menu Buttons
        Button vsSelfBtn = UIHelper.createMenuButton("vs. SELF", 20);
        Button vsAiBtn = UIHelper.createMenuButton("vs. AI", 20);
        VBox singleSub = createSubMenu(vsSelfBtn, vsAiBtn);

        Button createRoomBtn = UIHelper.createMenuButton("CREATE ROOM", 20);
        Button joinRoomBtn = UIHelper.createMenuButton("JOIN ROOM", 20);
        VBox multiSub = createSubMenu(createRoomBtn, joinRoomBtn);

        // 4. Logic
        singleBtn.setOnAction(e -> toggleDropDown(singleSub, multiSub));
        multiBtn.setOnAction(e -> toggleDropDown(multiSub, singleSub));
        vsSelfBtn.setOnAction(e -> app.showGame());
        backBtn.setOnAction(e -> app.showMainMenu());

        menuBox.getChildren().addAll(
            logo, 
            singleBtn, 
            singleSub, 
            multiBtn, 
            multiSub, 
            backBtn
        );
        
        hideNodeImmediately(singleSub, multiSub);

        UIHelper.bindFontSize(mainLabel, layout, 80);
        UIHelper.bindFontSize(subLabel, layout, 25);
        
        UIHelper.bindFontSize(singleBtn, layout, 30);
        UIHelper.bindFontSize(multiBtn, layout, 30);
        UIHelper.bindFontSize(backBtn, layout, 25);

        UIHelper.bindFontSize(vsSelfBtn, layout, 20);
        UIHelper.bindFontSize(vsAiBtn, layout, 20);
        UIHelper.bindFontSize(createRoomBtn, layout, 20);
        UIHelper.bindFontSize(joinRoomBtn, layout, 20);

        layout.getChildren().add(menuBox);
        return layout;
    }

    private VBox createSubMenu(Node... nodes) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(nodes);
        box.setManaged(false); 
        box.setVisible(false);
        return box;
    }

    private void toggleDropDown(VBox toToggle, VBox otherToClose) {
        if (otherToClose.isVisible()) {
            hideNodeImmediately(otherToClose);
        }

        if (toToggle.isVisible()) {
            hideNodeImmediately(toToggle);
        } else {
            toToggle.setVisible(true);
            toToggle.setManaged(true);
            fadeIn(toToggle);
        }
    }

    private void fadeIn(Node node) {
        node.setOpacity(0);
        node.setTranslateY(-10); 
        
        FadeTransition ft = new FadeTransition(Duration.millis(250), node);
        ft.setToValue(1.0);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), node);
        tt.setToY(0);
        
        ParallelTransition pt = new ParallelTransition(node, ft, tt);
        pt.play();
    }

    private void hideNodeImmediately(Node... nodes) {
        for (Node n : nodes) {
            n.setVisible(false);
            n.setManaged(false);
            n.setOpacity(0); 
            n.setTranslateY(0); 
        }
    }
}