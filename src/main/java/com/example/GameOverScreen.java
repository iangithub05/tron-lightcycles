package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class GameOverScreen {

    private final Main     app;
    private final boolean  won;
    private final GameMode mode;
    private final String   message;

    public GameOverScreen(Main app, boolean won, GameMode mode, String message) {
        this.app     = app;
        this.won     = won;
        this.mode    = mode;
        this.message = message;
    }

    public Pane getView() {
        // Header
        Label header = new Label(won ? "YOU WIN!" : "YOU CRASHED!");
        header.setFont(Font.font("Monospace", FontWeight.BOLD, 40));
        header.setTextFill(won ? Color.web("#00ff88") : Color.web("#ff4444"));

        // Stats
        Label stats = new Label(message);
        stats.setFont(Font.font("Monospace", 16));
        stats.setTextFill(Color.web("#ffffff"));
        stats.setTextAlignment(TextAlignment.CENTER);
        stats.setWrapText(true);

        // Buttons
        Button retry  = styledButton("RETRY",      "#00f0ff");
        Button menu   = styledButton("MAIN MENU",  "#ffffff");

        retry.setOnAction(e -> app.showGame(mode));
        menu.setOnAction(e -> app.showMainMenu());

        HBox buttons = new HBox(20, retry, menu);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(28, header, stats, buttons);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(60));
        layout.setBackground(new Background(new BackgroundFill(Color.web("#0a0a1a"), CornerRadii.EMPTY, Insets.EMPTY)));

        StackPane root = new StackPane(layout);
        root.setPrefSize(800, 600);
        root.setBackground(new Background(new BackgroundFill(Color.web("#0a0a1a"), CornerRadii.EMPTY, Insets.EMPTY)));
        return root;
    }

    private Button styledButton(String text, String hexColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        btn.setTextFill(Color.web(hexColor));
        btn.setBackground(new Background(new BackgroundFill(Color.web("#ffffff11"), new CornerRadii(4), Insets.EMPTY)));
        btn.setBorder(new Border(new BorderStroke(Color.web(hexColor + "88"), BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(1))));
        btn.setPadding(new Insets(10, 28, 10, 28));

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
