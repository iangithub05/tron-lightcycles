package com.example.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class Theme {

    private static final String CSS;

    static {
        java.net.URL url = Theme.class.getResource("/com/example/ui/styles.css");
        CSS = url != null ? url.toExternalForm() : null;
    }

    public static void apply(Parent root) {
        if (CSS != null) root.getStylesheets().add(CSS);
    }

    public static VBox buttonColumn() {
        VBox col = new VBox(6);
        col.setAlignment(Pos.TOP_LEFT);
        return col;
    }

    public static Region spacer(double height) {
        Region r = new Region();
        r.setPrefHeight(height);
        return r;
    }

    public static Region hspacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    public static Region divider() {
        Pane d = new Pane();
        d.getStyleClass().add("section-divider");
        return d;
    }
}