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

    // Fixed color mapping for known statuses mimicking Tailwind UI Badges
    private static final Map<String, Color[]> FIXED_COLORS = new HashMap<>();

    static {
        FIXED_COLORS.put("ناجح", new Color[] { new Color(0xD1FAE5), new Color(0x059669) }); // Emerald
        FIXED_COLORS.put("راسب", new Color[] { new Color(0xFEE2E2), new Color(0xDC2626) }); // Red
        FIXED_COLORS.put("دور ثاني", new Color[] { new Color(0xFFEDD5), new Color(0xEA580C) }); // Orange
        FIXED_COLORS.put("مؤجل", new Color[] { new Color(0xDBEAFE), new Color(0x2563EB) }); // Blue
        FIXED_COLORS.put("معتذر", new Color[] { new Color(0xFEF9C3), new Color(0xCA8A04) }); // Yellow
        FIXED_COLORS.put("غائب", new Color[] { new Color(0xFCE7F3), new Color(0xDB2777) }); // Pink
        FIXED_COLORS.put("منقطع", new Color[] { new Color(0xF1F5F9), new Color(0x475569) }); // Slate
        FIXED_COLORS.put("مفصول", new Color[] { new Color(0xFEE2E2), new Color(0x991B1B) }); // Dark Red
        FIXED_COLORS.put("تحت الدراسة", new Color[] { new Color(0xF3E8FF), new Color(0x7E22CE) }); // Purple
        FIXED_COLORS.put("غير محدد", new Color[] { new Color(0xF8FAFC), new Color(0x94A3B8) }); // Light Slate
    }

    // Auto-generated palette for unknown dynamic statuses
    private static final Color[][] AUTO_PALETTE = {
            { new Color(0xCCFBF1), new Color(0x0F766E) }, // Teal
            { new Color(0xE0E7FF), new Color(0x4338CA) }, // Indigo
            { new Color(0xFFEDD5), new Color(0xC2410C) }, // Orange
            { new Color(0xFAE8FF), new Color(0xA21CAF) }, // Fuchsia
    };

    private static final Map<String, Color[]> dynamicCache = new HashMap<>();
    private static int paletteIndex = 0;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {

        String status = value != null ? value.toString().trim() : "غير محدد";
        if (status.isEmpty())
            status = "غير محدد";

        // Resolve colors dynamically
        Color[] colors = FIXED_COLORS.get(status);
        if (colors == null) {
            // Check if string contains key keywords
            if (status.contains("ناجح"))
                colors = FIXED_COLORS.get("ناجح");
            else if (status.contains("راسب"))
                colors = FIXED_COLORS.get("راسب");
            else if (status.contains("دور ثاني"))
                colors = FIXED_COLORS.get("دور ثاني");
            else if (status.contains("مؤجل"))
                colors = FIXED_COLORS.get("مؤجل");
            else if (status.contains("معتذر"))
                colors = FIXED_COLORS.get("معتذر");
            else {
                colors = dynamicCache.computeIfAbsent(status, k -> {
                    Color[] c = AUTO_PALETTE[paletteIndex % AUTO_PALETTE.length];
                    paletteIndex++;
                    return c;
                });
            }
        }

        final Color pillBg = colors[0];
        final Color pillFg = colors[1];

        // Row background logic matching DashboardPage table hover selection
        final Color rowBg = isSelected ? new Color(0xf5f7fb) : Color.WHITE;

        JPanel cell = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw row background
                g2.setColor(rowBg);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Draw pill badge
                int pH = 28;
                int pW = Math.min(100, getWidth() - 20); // padding horizontally
                int px = (getWidth() - pW) / 2;
                int py = (getHeight() - pH) / 2;

                g2.setColor(pillBg);
                g2.fillRoundRect(px, py, pW, pH, pH, pH); // fully rounded border-radius 20px eqv
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
