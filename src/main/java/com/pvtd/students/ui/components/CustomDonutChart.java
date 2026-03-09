package com.pvtd.students.ui.components;

import com.pvtd.students.ui.utils.UITheme;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

public class CustomDonutChart extends JPanel {
    private Map<String, Integer> data;
    private Color[] colors = {
            new Color(46, 204, 113), // Success Green
            new Color(241, 196, 15), // Warning Yellow
            new Color(231, 76, 60) // Danger Red
    };

    private static class Slice {
        Shape shape;
        String tooltip;

        Slice(Shape shape, String tooltip) {
            this.shape = shape;
            this.tooltip = tooltip;
        }
    }

    private List<Slice> slices = new ArrayList<>();

    public CustomDonutChart(Map<String, Integer> stats) {
        this.data = stats;
        setOpaque(false);
        setPreferredSize(new Dimension(300, 300));
        setToolTipText(""); // Required trick to force ToolTipManager to listen!
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        for (Slice slice : slices) {
            if (slice.shape.contains(e.getPoint())) {
                return slice.tooltip;
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int total = data.getOrDefault("total", 1);
        if (total == 0)
            total = 1; // Prevent div by zero if DB is empty

        int passed = data.getOrDefault("passed", 0);
        int secondTry = data.getOrDefault("second_try", 0);
        int failed = data.getOrDefault("failed", 0);

        int[] values = { passed, secondTry, failed };

        int width = getWidth();
        int height = getHeight();
        int minSize = Math.min(width, height);
        int x = (width - minSize) / 2 + 10;
        int y = (height - minSize) / 2 + 10;
        int size = minSize - 20;

        double startAngle = 90; // Start at top

        int holeSize = (int) (size * 0.65); // 65% of full size
        int hx = x + (size - holeSize) / 2;
        int hy = y + (size - holeSize) / 2;
        Ellipse2D.Double hole = new Ellipse2D.Double(hx, hy, holeSize, holeSize);
        Area holeArea = new Area(hole);

        slices.clear();
        String[] labels = { "ناجح", "دور ثاني", "راسب" };

        // Draw Slices using Arc2D
        for (int i = 0; i < values.length; i++) {
            double extent = (values[i] * 360.0) / total;
            Arc2D.Double arc = new Arc2D.Double(x, y, size, size, startAngle, -extent, Arc2D.PIE);
            g2.setColor(colors[i]);
            g2.fill(arc);

            // Build interactive Hit Area
            Area sliceArea = new Area(arc);
            sliceArea.subtract(holeArea);

            // Tooltip format: "ناجح: 15"
            String tooltipText = String.format(
                    "<html><div style='padding:5px; font-size:14px; font-family:var(--font-family)'><b>%s</b>: %,d</div></html>",
                    labels[i], values[i]);
            slices.add(new Slice(sliceArea, tooltipText));

            startAngle -= extent;
        }

        g2.setColor(UITheme.CARD_BG);
        g2.fill(hole);

        // Draw Total Text in the middle
        String text = String.valueOf(data.getOrDefault("total", 0));
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 48));
        FontMetrics fm = g2.getFontMetrics();
        int tx = width / 2 - fm.stringWidth(text) / 2;
        int ty = height / 2 + fm.getAscent() / 2 - fm.getDescent();
        g2.drawString(text, tx, ty);

        g2.dispose();
    }
}
