package com.pvtd.students.ui.utils;

import com.pvtd.students.services.StatusesService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table cell renderer that draws a pill-shaped colored badge for student
 * status.
 * Supports dynamic statuses loaded from the database with auto-assigned colors.
 */
public class StatusBadgeRenderer extends DefaultTableCellRenderer {

    // Fixed color mapping for known statuses
    private static final Map<String, Color[]> FIXED_COLORS = new HashMap<>();

    static {
        FIXED_COLORS.put("ناجح", new Color[] { new Color(0xDCFCE7), new Color(0x16A34A) });
        FIXED_COLORS.put("راسب", new Color[] { new Color(0xFEE2E2), new Color(0xDC2626) });
        FIXED_COLORS.put("دور ثاني", new Color[] { new Color(0xFEF3C7), new Color(0xD97706) });
        FIXED_COLORS.put("مستجد", new Color[] { new Color(0xDBEAFE), new Color(0x2563EB) });
        FIXED_COLORS.put("غائب", new Color[] { new Color(0xFCE7F3), new Color(0xBE185D) });
        FIXED_COLORS.put("منقطع", new Color[] { new Color(0xF3F4F6), new Color(0x6B7280) });
        FIXED_COLORS.put("مفصول", new Color[] { new Color(0xFEE2E2), new Color(0x991B1B) });
        FIXED_COLORS.put("تحت الدراسة", new Color[] { new Color(0xEDE9FE), new Color(0x7C3AED) });
        FIXED_COLORS.put("غير محدد", new Color[] { new Color(0xF1F5F9), new Color(0x94A3B8) });
    }

    // Auto-generated palette for unknown dynamic statuses
    private static final Color[][] AUTO_PALETTE = {
            { new Color(0xDCFCE7), new Color(0x15803D) },
            { new Color(0xDBEAFE), new Color(0x1D4ED8) },
            { new Color(0xFEF3C7), new Color(0xB45309) },
            { new Color(0xEDE9FE), new Color(0x6D28D9) },
            { new Color(0xFCE7F3), new Color(0xBE185D) },
            { new Color(0xF0FDF4), new Color(0x166534) },
            { new Color(0xFFF7ED), new Color(0xC2410C) },
            { new Color(0xE0F2FE), new Color(0x0369A1) },
    };

    // Cache auto-assigned colors so same status always gets same color
    private final Map<String, Color[]> dynamicCache = new HashMap<>();
    private int paletteIndex = 0;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {

        String status = value != null ? value.toString().trim() : "غير محدد";
        if (status.isEmpty())
            status = "غير محدد";

        // Resolve colors
        Color[] colors = FIXED_COLORS.get(status);
        if (colors == null) {
            colors = dynamicCache.computeIfAbsent(status, k -> {
                Color[] c = AUTO_PALETTE[paletteIndex % AUTO_PALETTE.length];
                paletteIndex++;
                return c;
            });
        }
        final Color pillBg = colors[0];
        final Color pillFg = colors[1];

        // Row background
        final Color rowBg = isSelected
                ? table.getSelectionBackground()
                : (row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));

        JPanel cell = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Row fill
                g2.setColor(rowBg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Pill
                int pH = 26, pW = Math.min(90, getWidth() - 16);
                int px = (getWidth() - pW) / 2;
                int py = (getHeight() - pH) / 2;
                g2.setColor(pillBg);
                g2.fillRoundRect(px, py, pW, pH, pH, pH);
                g2.dispose();
            }
        };
        cell.setLayout(new BorderLayout());
        cell.setOpaque(false);

        JLabel lbl = new JLabel(status, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(pillFg);
        cell.add(lbl, BorderLayout.CENTER);

        return cell;
    }
}
