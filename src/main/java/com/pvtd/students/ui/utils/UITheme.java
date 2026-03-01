package com.pvtd.students.ui.utils;

import java.awt.Color;
import java.awt.Font;

/**
 * Modern Enterprise SaaS Design System (FlatLaf Integrated)
 */
public class UITheme {
    // Brand Colors
    public static final Color PRIMARY = Color.decode("#2563EB"); // Modern Blue
    public static final Color SUCCESS = Color.decode("#16A34A"); // Green
    public static final Color WARNING = Color.decode("#F59E0B"); // Amber/Orange
    public static final Color DANGER = Color.decode("#DC2626"); // Red

    // Backgrounds & Surfaces
    public static final Color BG_LIGHT = Color.decode("#F8FAFC"); // Slate 50 Main body
    public static final Color BG_SIDEBAR = Color.decode("#0F172A"); // Slate 900
    public static final Color CARD_BG = Color.WHITE;

    // Text Colors
    public static final Color TEXT_PRIMARY = Color.decode("#1E293B"); // Slate 800
    public static final Color TEXT_SECONDARY = Color.decode("#64748B"); // Slate 500
    public static final Color TEXT_SIDEBAR = Color.decode("#E2E8F0"); // Slate 200
    public static final Color TEXT_SIDEBAR_HOVER = Color.WHITE;

    // Interactions
    public static final Color HOVER_SIDEBAR = Color.decode("#1E293B"); // Slate 800 Hover
    public static final Color HOVER_PRIMARY = Color.decode("#1D4ED8"); // Blue 700

    // Border & Dividers
    public static final Color BORDER = Color.decode("#E2E8F0");

    // Fonts (Prioritizing Arabic Sans)
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_CARD_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);

    // Radii
    public static final int ARC_MODERN = 16;
}
