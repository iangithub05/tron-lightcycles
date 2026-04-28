package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainMenu {

    private final Main app;

    public MainMenu(Main app) {
        this.app = app;
    }

    public Pane getView() {
        // Title
        Label title = new Label("TRON: LIGHT CYCLES");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#00f0ff"));

        Label subtitle = new Label("Select a Game Mode");
        subtitle.setFont(Font.font("Monospace", 16));
        subtitle.setTextFill(Color.web("#ffffff88"));

        // Mode buttons
        VBox modes = new VBox(12,
            modeButton("⏱  TIME SURVIVAL",    "Survive for 60 seconds without crashing.",       GameMode.TIME_SURVIVAL),
            modeButton("📏 SCORE CHASE",       "Build a trail of 5,000 points.",                 GameMode.SCORE_DISTANCE),
            modeButton("⭐ PICKUP RUSH",       "Collect all 20 pickups before you crash.",       GameMode.PICKUPS),
            modeButton("🏁 LAP RACE",          "Pass through all 4 checkpoints × 3 laps.",       GameMode.LAPS),
            modeButton("🎨 FILL THE BOARD",    "Cover 30% of the arena with your light trail.",  GameMode.FILL_BOARD)
        );
        modes.setAlignment(Pos.CENTER);

        Button exit = styledButton("EXIT", "#ff4444");
        exit.setOnAction(e -> System.exit(0));

        VBox layout = new VBox(24, title, subtitle, modes, exit);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setBackground(new Background(new BackgroundFill(Color.web("#0a0a1a"), CornerRadii.EMPTY, Insets.EMPTY)));

        StackPane root = new StackPane(layout);
        root.setPrefSize(800, 600);
        root.setBackground(new Background(new BackgroundFill(Color.web("#0a0a1a"), CornerRadii.EMPTY, Insets.EMPTY)));
        return root;
    }

    private VBox modeButton(String label, String description, GameMode mode) {
        Button btn = styledButton(label, "#00f0ff");
        btn.setPrefWidth(420);
        btn.setOnAction(e -> app.showGame(mode));

        Label desc = new Label(description);
        desc.setFont(Font.font("Monospace", 11));
        desc.setTextFill(Color.web("#ffffff66"));

        VBox box = new VBox(2, btn, desc);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private Button styledButton(String text, String hexColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        btn.setTextFill(Color.web(hexColor));
        btn.setBackground(new Background(new BackgroundFill(Color.web("#ffffff11"), new CornerRadii(4), Insets.EMPTY)));
        btn.setBorder(new Border(new BorderStroke(Color.web(hexColor + "88"), BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(1))));
        btn.setPadding(new Insets(10, 20, 10, 20));

        // Hover effect
        btn.setOnMouseEntered(e -> {
            btn.setBackground(new Background(new BackgroundFill(Color.web(hexColor + "22"), new CornerRadii(4), Insets.EMPTY)));
            btn.setBorder(new Border(new BorderStroke(Color.web(hexColor), BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(1))));
        });
        btn.setOnMouseExited(e -> {
            btn.setBackground(new Background(new BackgroundFill(Color.web("#ffffff11"), new CornerRadii(4), Insets.EMPTY)));
            btn.setBorder(new Border(new BorderStroke(Color.web(hexColor + "88"), BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(1))));
        });

        return btn;
    }
}
