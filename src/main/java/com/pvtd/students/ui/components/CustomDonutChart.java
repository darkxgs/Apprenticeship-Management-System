package com.pvtd.students.ui.components;

import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomDonutChart extends JPanel {
    private Map<String, Integer> data;
    private List<String> statuses;

    // Animation
    private float animationProgress = 0f;
    private Timer animator;

    // Interaction
    private int hoveredSliceIndex = -1;

    private static class Slice {
        Shape shape;
        String label;
        int value;
        Color color;
        double startAngle;
        double extentAngle;

        Slice(String label, int value, Color color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private List<Slice> slices = new ArrayList<>();

    public CustomDonutChart(Map<String, Integer> stats, List<String> dynamicStatuses) {
        this.data = stats;
        this.statuses = dynamicStatuses;
        setOpaque(false);
        setPreferredSize(new Dimension(500, 350));

        // Tooltips
        setToolTipText("");
        ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().setInitialDelay(0);

        // Mouse Move for Hover Expansion Effects
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int newHover = -1;
                for (int i = 0; i < slices.size(); i++) {
                    if (slices.get(i).shape != null && slices.get(i).shape.contains(e.getPoint())) {
                        newHover = i;
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                        break;
                    }
                }
                if (newHover == -1) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                if (newHover != hoveredSliceIndex) {
                    hoveredSliceIndex = newHover;
                    repaint(); // Trigger repaint for active slice pop effect
                }
            }
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredSliceIndex != -1) {
                    hoveredSliceIndex = -1;
                    repaint();
                }
            }
        });

        calculateSlices();

        // Reveal Animation (Arc growing 0 -> 360)
        animator = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationProgress += 0.04f;
                if (animationProgress >= 1f) {
                    animationProgress = 1f;
                    animator.stop();
                }
                repaint();
            }
        });
        animator.start();
    }

    private void calculateSlices() {
        slices.clear();
        int total = data.getOrDefault("total", 1);
        if (total == 0)
            total = 1;

        double currentStartAngle = 90;

        for (String status : statuses) {
            int count = data.getOrDefault(status, 0);
            if (count > 0) {
                Color c = getColorForStatus(status);
                Slice s = new Slice(status, count, c);
                s.startAngle = currentStartAngle;
                s.extentAngle = (count * 360.0) / total;
                slices.add(s);
                currentStartAngle -= s.extentAngle;
            }
        }
    }

    private Color getColorForStatus(String status) {
        if (status.contains("ناجح"))
            return new Color(0x10B981); // Emerald Green
        if (status.contains("راسب"))
            return new Color(0xEF4444); // Red
        if (status.contains("دور ثاني"))
            return new Color(0xF97316); // Orange
        if (status.contains("مؤجل"))
            return new Color(0x3B82F6); // Blue
        if (status.contains("معتذر"))
            return new Color(0xEAB308); // Yellow
        return new Color(0x64748B); // Slate
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        if (hoveredSliceIndex != -1 && hoveredSliceIndex < slices.size()) {
            Slice s = slices.get(hoveredSliceIndex);
            return String.format(
                    "<html><div style='padding:5px; font-size:14px; font-family:var(--font-family); text-align:right'><b>%s</b>: %,d</div></html>",
                    s.label, s.value);
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ── Dimensions configuration (Right: Chart, Left: Legend in RTL mode)
        int width = getWidth();
        int height = getHeight();

        // Chart Area (taking up 55% of width on right side)
        int chartAreaWidth = (int) (width * 0.55);
        int chartAreaX = width - chartAreaWidth; // Push to right for Arabic RTL

        int minSize = Math.min(chartAreaWidth, height);
        int baseSize = minSize - 40;

        // Center of the chart
        int cx = chartAreaX + (chartAreaWidth) / 2;
        int cy = height / 2;

        // Current Reveal degree boundary based on animation
        double revealEndAngle = 90 - (360 * animationProgress);

        for (int i = 0; i < slices.size(); i++) {
            Slice s = slices.get(i);

            // Pop out logic
            boolean isHovered = (i == hoveredSliceIndex);
            int size = isHovered ? baseSize + 14 : baseSize;
            int x = cx - size / 2;
            int y = cy - size / 2;

            // Check if this slice should be painted based on animation progress
            double startDegree = s.startAngle;
            double extentDegree = s.extentAngle;

            // If the start of the slice is fully outside the animated reveal, skip.
            if (startDegree < revealEndAngle && extentDegree < 0)
                continue; // Note extent is negative

            // Clamp extent if it crosses the reveal cutoff
            double renderExtent = -extentDegree; // Math absolute
            if (startDegree - renderExtent < revealEndAngle) {
                renderExtent = startDegree - revealEndAngle; // Partial extent
            }

            if (renderExtent > 0) {
                Arc2D.Double arc = new Arc2D.Double(x, y, size, size, startDegree, -renderExtent, Arc2D.PIE);

                // Dim non-hovered slices slightly when something IS hovered
                if (hoveredSliceIndex != -1 && !isHovered) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                } else {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }

                g2.setColor(s.color);
                g2.fill(arc);

                // Add white spacer strokes between slices exactly like ApexCharts
                if (renderExtent == extentDegree) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.draw(arc);
                }

                // If fully rendered (not mid-animation boundary), record hit area
                if (renderExtent >= -s.extentAngle - 0.1) {
                    int holeSize = (int) (size * 0.65);
                    int hx = cx - holeSize / 2;
                    int hy = cy - holeSize / 2;
                    Ellipse2D.Double holeForHit = new Ellipse2D.Double(hx, hy, holeSize, holeSize);
                    Area sliceArea = new Area(arc);
                    sliceArea.subtract(new Area(holeForHit));
                    s.shape = sliceArea; // update interactive bounds
                }
            }
        }

        // Draw The Center Hole
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        int holeSize = (int) (baseSize * 0.65);
        int hx = cx - holeSize / 2;
        int hy = cy - holeSize / 2;
        Ellipse2D.Double hole = new Ellipse2D.Double(hx, hy, holeSize, holeSize);
        g2.setColor(Color.WHITE); // Assuming card background is white
        g2.fill(hole);

        // Draw Total Text in the middle of donut
        if (animationProgress > 0.5f) { // Reveal text halfway
            String text = String.valueOf(data.getOrDefault("total", 0));
            g2.setColor(new Color(0x334155)); // slate-700
            g2.setFont(new Font("Segoe UI", Font.BOLD, 42));
            FontMetrics fm = g2.getFontMetrics();
            int tx = cx - fm.stringWidth(text) / 2;
            int ty = cy + fm.getAscent() / 2 - fm.getDescent();

            // Fade text in
            float txtAlpha = Math.min((animationProgress - 0.5f) * 2f, 1f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, txtAlpha));
            g2.drawString(text, tx, ty);

            // Total Label under number
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(new Color(0x94A3B8)); // slate-400
            String subText = "إجمالي";
            int sx = cx - g2.getFontMetrics().stringWidth(subText) / 2;
            int sy = ty + 18;
            g2.drawString(subText, sx, sy);
        }

        // ── Legend Area (Left Side) ──────────────────────────────────────────
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animationProgress));
        int legendX = 40;
        int legendY = cy - (slices.size() * 35) / 2 + 10;

        for (int i = 0; i < slices.size(); i++) {
            Slice s = slices.get(i);

            // Hover styling for legend
            boolean isHovered = (i == hoveredSliceIndex);

            // Bullet color
            g2.setColor(s.color);
            g2.fillOval(legendX + 130, legendY - 12, 10, 10);

            // Text Label
            g2.setColor(isHovered ? new Color(0x0f172a) : new Color(0x64748B));
            g2.setFont(new Font("Segoe UI", isHovered ? Font.BOLD : Font.PLAIN, 14));

            // Right align text next to bullet
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s.label, legendX + 115 - fm.stringWidth(s.label), legendY);

            // Value
            g2.setColor(new Color(0x1e293b));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString(String.format("%,d", s.value), legendX - 10, legendY);

            legendY += 35;
        }

        g2.dispose();
    }
}
