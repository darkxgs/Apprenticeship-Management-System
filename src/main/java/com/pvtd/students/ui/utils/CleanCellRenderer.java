package com.pvtd.students.ui.utils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CleanCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

        // Match the row hover logic deployed in DashboardPage
        if (isSelected) {
            c.setBackground(new Color(0xf5f7fb));
            c.setForeground(new Color(0x1e293b));
        } else {
            c.setBackground(Color.WHITE);
            c.setForeground(new Color(0x334155));
        }

        // Add padding to text
        setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        // Remove focus rectangle
        if (hasFocus) {
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        }

        return c;
    }
}
