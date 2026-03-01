package com.pvtd.students.ui.pages;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPage extends JPanel {

    public SettingsPage() {
        setLayout(new BorderLayout(24, 24));
        setBorder(new EmptyBorder(30, 30, 30, 30));
        setBackground(UITheme.BG_LIGHT);

        JLabel sectionTitle = new JLabel("إعدادات النظام", SwingConstants.RIGHT);
        sectionTitle.setFont(UITheme.FONT_TITLE);
        sectionTitle.setForeground(UITheme.TEXT_PRIMARY);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        contentPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f, 20, UITheme.CARD_BG),
                new EmptyBorder(30, 30, 30, 30)));

        JLabel titleLabel = new JLabel("إعدادات النظام", SwingConstants.RIGHT); // Keeping if needed internally
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel themeLabel = new JLabel("المظهر العام (Theme):", SwingConstants.RIGHT);
        themeLabel.setFont(UITheme.FONT_HEADER);
        themeLabel.setForeground(UITheme.TEXT_SECONDARY);
        themeLabel.setBorder(new EmptyBorder(10, 0, 15, 0));
        themeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnPanel.setOpaque(false);

        JButton lightMode = new JButton("Light Mode ☀️");
        lightMode.setFont(UITheme.FONT_HEADER);
        lightMode.setPreferredSize(new Dimension(150, 45));
        lightMode.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lightMode.putClientProperty("JButton.buttonType", "roundRect");
        lightMode.addActionListener(e -> switchTheme(new FlatLightLaf()));

        JButton darkMode = new JButton("Dark Mode \uD83C\uDF19");
        darkMode.setFont(UITheme.FONT_HEADER);
        darkMode.setPreferredSize(new Dimension(150, 45));
        darkMode.setCursor(new Cursor(Cursor.HAND_CURSOR));
        darkMode.putClientProperty("JButton.buttonType", "roundRect");
        darkMode.addActionListener(e -> switchTheme(new FlatDarkLaf()));

        btnPanel.add(darkMode);
        btnPanel.add(lightMode);

        contentPanel.add(themeLabel);
        contentPanel.add(btnPanel);

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(sectionTitle, BorderLayout.NORTH);
        container.add(contentPanel, BorderLayout.CENTER);

        add(container, BorderLayout.NORTH);
    }

    private void switchTheme(LookAndFeel laf) {
        try {
            UIManager.setLookAndFeel(laf);
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}
