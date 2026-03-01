package com.pvtd.students.ui.components;

import com.pvtd.students.ui.utils.UITheme;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;

public class Topbar extends JPanel {

    public Topbar() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(15, 24, 15, 24)));
        setBackground(UITheme.BG_LIGHT);

        // Right side: Active page title and Search placeholder
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        rightPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("لوحة التحكم");
        titleLabel.setFont(UITheme.FONT_CARD_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JTextField search = new JTextField();
        search.setPreferredSize(new Dimension(300, 35));
        search.setFont(UITheme.FONT_BODY);
        search.putClientProperty("JTextField.placeholderText", "ابحث في النظام...");
        search.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        try {
            FlatSVGIcon searchIcon = new FlatSVGIcon("icons/search.svg", 16, 16);
            searchIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> UITheme.TEXT_SECONDARY));
            // In RTL, standard clients usually put leading icon on the right
            search.putClientProperty("JTextField.trailingIcon", searchIcon);
        } catch (Exception e) {
        }

        rightPanel.add(titleLabel);
        rightPanel.add(search);

        add(rightPanel, BorderLayout.EAST);

        // Left side profiles
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JButton profileBtn = new JButton("مسؤول النظام");
        profileBtn.setFont(UITheme.FONT_HEADER);
        profileBtn.setForeground(UITheme.TEXT_PRIMARY);
        profileBtn.putClientProperty("JButton.buttonType", "borderless");
        profileBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            FlatSVGIcon avatarIcon = new FlatSVGIcon("icons/avatar.svg", 24, 24);
            avatarIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> UITheme.TEXT_PRIMARY));
            profileBtn.setIcon(avatarIcon);
            profileBtn.setIconTextGap(8);
        } catch (Exception e) {
        }

        JButton notifs = new JButton("");
        notifs.putClientProperty("JButton.buttonType", "borderless");
        notifs.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            FlatSVGIcon bellIcon = new FlatSVGIcon("icons/bell.svg", 22, 22);
            bellIcon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> UITheme.TEXT_SECONDARY));
            notifs.setIcon(bellIcon);
        } catch (Exception e) {
        }

        profileBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "إعدادات حساب المدير ستتوفر قريباً", "الملف الشخصي",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        notifs.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "لا توجد إشعارات جديدة حالياً", "الإشعارات",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        leftPanel.add(notifs);
        leftPanel.add(profileBtn);

        add(leftPanel, BorderLayout.WEST);
    }
}
