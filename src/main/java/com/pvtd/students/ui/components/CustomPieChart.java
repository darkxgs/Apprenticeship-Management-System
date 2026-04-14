package com.pvtd.students.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomPieChart extends JPanel {
    private Map<String, Integer> data;
    private String[] labels = { "ناجح", "راسب", "دور ثاني" };

    private int hoveredIndex = -1;

    private static class Slice {
        Shape shape;
        String label;
        int value;
        Color color;
        double startAngle;
        double extentAngle;
    }

    private List<Slice> slices = new ArrayList<>();

    public CustomPieChart(Map<String, Integer> data) {
        this.data = data;
        setOpaque(false);
        setPreferredSize(new Dimension(480, 320));

        ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().setInitialDelay(50);

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int hover = -1;
                for (int i = 0; i < slices.size(); i++) {
                    if (slices.get(i).shape != null && slices.get(i).shape.contains(e.getPoint())) {
                        hover = i;
                        break;
                    }
                }
                if (hover != hoveredIndex) {
                    hoveredIndex = hover;
                    setCursor(new Cursor(hover != -1 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredIndex != -1) {
                    hoveredIndex = -1;
                    repaint();
                }
            }
        });

        calculateSlices();
    }

    private void calculateSlices() {
        int total = 0;
        for (String l : labels)
            total += data.getOrDefault(l, 0);
        if (total == 0)
            total = 1;

        double currentAngle = 90;
        for (String status : labels) {
            int count = data.getOrDefault(status, 0);
            if (count > 0) {
                Slice s = new Slice();
                s.label = status;
                s.value = count;
                s.color = getColor(status);
                s.startAngle = currentAngle;
                s.extentAngle = (count * 360.0) / total;
                slices.add(s);
                currentAngle -= s.extentAngle;
            }
        }
    }

    private Color getColor(String s) {
        if ("ناجح".equals(s))
            return new Color(0x10B981);
        if ("راسب".equals(s))
            return new Color(0xEF4444);
        if ("دور ثاني".equals(s))
            return new Color(0xF97316);
        return new Color(0x64748B);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        if (hoveredIndex != -1) {
            Slice s = slices.get(hoveredIndex);
            return String.format(
                    "<html><div style='padding:5px;font-size:14px;font-family:Segoe UI;text-align:right'><b>%s</b>: %,d</div></html>",
                    s.label, s.value);
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Right Side: Chart
        int chartW = (int) (w * 0.55);
        int chartX = w - chartW;

        int size = Math.min(chartW, h) - 60;
        int cx = chartX + chartW / 2;
        int cy = h / 2;

        for (int i = 0; i < slices.size(); i++) {
            Slice s = slices.get(i);
            boolean hover = (i == hoveredIndex);

            // Pop out effect on hover
            double pop = hover ? 12.0 : 0.0;
            double midAngle = Math.toRadians(s.startAngle - s.extentAngle / 2.0);
            int dx = (int) (Math.cos(midAngle) * pop);
            int dy = (int) (-Math.sin(midAngle) * pop);

            int drawSize = size + (hover ? 8 : 0);
            int x = cx - drawSize / 2 + dx;
            int y = cy - drawSize / 2 + dy;

            Arc2D.Double arc = new Arc2D.Double(x, y, drawSize, drawSize, s.startAngle, -s.extentAngle, Arc2D.PIE);

            if (hover) {
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fill(new Arc2D.Double(x + 3, y + 8, drawSize, drawSize, s.startAngle, -s.extentAngle, Arc2D.PIE));
            }

            g2.setColor(s.color);
            g2.fill(arc);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(4f));
            g2.draw(arc);

            s.shape = new Arc2D.Double(cx - size / 2, cy - size / 2, size, size, s.startAngle, -s.extentAngle,
                    Arc2D.PIE);
        }

        // Left Side: Legend
        int legX = 50;
        int legY = cy - (slices.size() * 40) / 2 + 20;

        for (int i = 0; i < slices.size(); i++) {
            Slice s = slices.get(i);
            boolean hover = (i == hoveredIndex);

            g2.setColor(hover ? new Color(0x0f172a) : new Color(0x475569));
            g2.setFont(new Font("Segoe UI", hover ? Font.BOLD : Font.PLAIN, 15));
            FontMetrics fm = g2.getFontMetrics();

            String valStr = String.format("%,d", s.value);
            g2.drawString(valStr, legX, legY);

            int labelX = legX + 50;
            g2.drawString(s.label, labelX, legY);

            int dotX = labelX + fm.stringWidth(s.label) + 15;
            g2.setColor(s.color);
            g2.fillOval(dotX, legY - 12, 14, 14);

            legY += 40;
        }

        g2.dispose();
    }
}
