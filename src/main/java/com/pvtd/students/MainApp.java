package com.pvtd.students;

import com.formdev.flatlaf.FlatLightLaf;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.LoginFrame;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        System.out.println("Starting Industrial Apprenticeship Diploma System...");

        // Setup Modern UI
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Global Web-Like UI overrides
            UIManager.put("Component.accentColor", UITheme.PRIMARY);
            UIManager.put("Component.focusColor", UITheme.HOVER_PRIMARY);
            UIManager.put("Button.arc", UITheme.ARC_MODERN);
            UIManager.put("Component.arc", UITheme.ARC_MODERN);
            UIManager.put("ProgressBar.arc", UITheme.ARC_MODERN);
            UIManager.put("TextComponent.arc", UITheme.ARC_MODERN);

            // Clean typography and layout spacing
            UIManager.put("defaultFont", UITheme.FONT_BODY);
            UIManager.put("Table.rowHeight", 40);
            UIManager.put("TableHeader.height", 45);
            UIManager.put("TableHeader.font", UITheme.FONT_HEADER);
            UIManager.put("Table.selectionBackground", UITheme.BG_LIGHT);
            UIManager.put("Table.selectionForeground", UITheme.PRIMARY);
            UIManager.put("Table.alternateRowColor", UITheme.BG_LIGHT);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));

        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        // Initialize DB
        System.out.println("Initializing Database Connections...");
        DatabaseConnection.initializeDatabase();

        // Launch UI
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
