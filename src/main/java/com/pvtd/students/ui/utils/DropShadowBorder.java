package com.pvtd.students.ui.utils;

import javax.swing.border.Border;
import java.awt.*;

/**
 * Modern Drop Shadow Border imitating CSS box-shadow for SaaS Panels
 */
public class DropShadowBorder implements Border {
    private final int shadowSize;
    private final float shadowOpacity;
    private final Color shadowColor;
    private final int cornerRadius;
    private final Color backgroundColor;

    public DropShadowBorder(Color shadowColor, int shadowSize, float shadowOpacity, int cornerRadius,
            Color backgroundColor) {
        this.shadowColor = shadowColor;
        this.shadowSize = shadowSize;
        this.shadowOpacity = shadowOpacity;
        this.cornerRadius = cornerRadius;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Shadows with decreasing opacity
        for (int i = 0; i < shadowSize; i++) {
            float opacity = shadowOpacity * (1.0f - ((float) i / shadowSize));
            Color cStyle = new Color(
                    shadowColor.getRed(),
                    shadowColor.getGreen(),
                    shadowColor.getBlue(),
                    (int) (opacity * 255));
            g2.setColor(cStyle);
            int offset = shadowSize - i;
            g2.fillRoundRect(
                    x + offset,
                    y + offset + (shadowSize / 2),
                    width - (offset * 2),
                    height - (offset * 2) - (shadowSize / 2),
                    cornerRadius,
                    cornerRadius);
        }

        // Draw the main card background on top using the exact corner radius
        if (backgroundColor != null) {
            g2.setColor(backgroundColor);
            g2.fillRoundRect(
                    x + shadowSize,
                    y + shadowSize,
                    width - (shadowSize * 2),
                    height - (shadowSize * 2) - (shadowSize / 2),
                    cornerRadius,
                    cornerRadius);
        }
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(shadowSize, shadowSize, shadowSize + (shadowSize / 2), shadowSize);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
