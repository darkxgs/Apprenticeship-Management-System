package com.pvtd.students.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

public class SplashScreenFrame extends JFrame {

    private float masterAlpha = 0f;
    private float logoScale = 0.5f;
    private float textAlpha = 0f;
    private float bgOffset = 0f;

    private Image logoImg;
    private String appFont;

    public SplashScreenFrame(Runnable onComplete) {
        setUndecorated(true);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0)); // Transparent background

        // Force the splash screen to the front and keep it on top
        setAlwaysOnTop(true);
        toFront();
        requestFocus();

        // Find best font
        appFont = "Segoe UI";
        String[] preferred = { "Cairo", "Tajawal", "IBM Plex Arabic", "Segoe UI" };
        for (String f : preferred) {
            for (String av : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                if (av.equalsIgnoreCase(f)) {
                    appFont = f;
                    break;
                }
            }
            if (!appFont.equals("Segoe UI"))
                break;
        }

        prepareLogo();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Fade in overall window alpha
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, masterAlpha));

                // Background Card
                RoundRectangle2D rect = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30);
                g2.clip(rect);

                // 1. Animated Gradient Background
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x0A192F),
                        getWidth(), getHeight(), new Color(0x112240));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // 2. Animated Background Particles/Glows
                g2.setColor(new Color(0x64FFDA, false));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, masterAlpha * 0.08f));
                int cx1 = (int) (Math.sin(bgOffset) * 100 - 150);
                int cy1 = (int) (Math.cos(bgOffset) * 100 - 100);
                g2.fillOval(cx1, cy1, 500, 500);

                g2.setColor(new Color(0x3B82F6, false));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, masterAlpha * 0.12f));
                int cx2 = (int) (getWidth() - 300 + Math.cos(bgOffset * 0.8) * 80);
                int cy2 = (int) (getHeight() - 250 + Math.sin(bgOffset * 0.8) * 80);
                g2.fillOval(cx2, cy2, 600, 600);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, masterAlpha));

                // 3. Draw Logo with Scale, Alpha, and Glow
                int logoBaseSize = 180;
                int currentLogoSize = (int) (logoBaseSize * logoScale);
                int lx = (getWidth() - currentLogoSize) / 2;
                int ly = (getHeight() - currentLogoSize) / 2 - 50; // shift up a bit

                // Logo Glow
                if (logoScale > 0.8f) {
                    float glowAlpha = Math.min(1f, (logoScale - 0.8f) * 5f) * masterAlpha * 0.3f;
                    g2.setComposite(
                            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, Math.min(1, glowAlpha))));
                    g2.setColor(new Color(255, 255, 255));
                    g2.fillOval(getWidth() / 2 - 120, ly + currentLogoSize / 2 - 120, 240, 240);
                }

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, masterAlpha));
                if (logoImg != null) {
                    g2.drawImage(logoImg, lx, ly, currentLogoSize, currentLogoSize, null);
                }

                // 4. Draw Texts with fade in
                if (textAlpha > 0) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                            Math.max(0, Math.min(1, textAlpha * masterAlpha))));

                    String orgName = "مصلحة الكفاية الإنتاجية والتدريب المهني";
                    g2.setFont(new Font(appFont, Font.PLAIN, 22));
                    g2.setColor(new Color(0x94A3B8));
                    FontMetrics fm = g2.getFontMetrics();
                    int ox = (getWidth() - fm.stringWidth(orgName)) / 2;
                    int oy = ly + currentLogoSize + 40;
                    g2.drawString(orgName, ox, oy);

                    String sysName = "نظام إدارة نتائج دبلوم التلمذة الصناعية";
                    g2.setFont(new Font(appFont, Font.BOLD, 34));
                    g2.setColor(Color.WHITE);
                    FontMetrics fm2 = g2.getFontMetrics();
                    int sx = (getWidth() - fm2.stringWidth(sysName)) / 2;
                    int sy = oy + 50;
                    g2.drawString(sysName, sx, sy);
                }

                // 5. Loading Bar Indicator (Subtle)
                int barWidth = 300;
                int bx = (getWidth() - barWidth) / 2;
                int by = getHeight() - 40;
                g2.setColor(new Color(255, 255, 255, 20)); // Track
                g2.fillRoundRect(bx, by, barWidth, 4, 4, 4);

                g2.setColor(new Color(0x3B82F6)); // Progress
                // Progress is based on textAlpha and masterAlpha sequence roughly
                float progress = Math.min(1f, (textAlpha * 0.8f) + (masterAlpha > 0.9f && textAlpha == 0 ? 0.2f : 0));
                g2.fillRoundRect(bx, by, (int) (barWidth * progress), 4, 4, 4);

                // Outline bounds
                g2.setColor(new Color(255, 255, 255, 30));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        setContentPane(panel);

        // Animation Timeline
        // Phase 1: Fade In & Logo Scale up
        // Phase 2: Text Fade in
        // Phase 3: Hold
        // Phase 4: Fade Out -> call onComplete

        Timer animator = new Timer(16, null); // ~60fps
        animator.addActionListener(new ActionListener() {
            int ticks = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                ticks++;
                bgOffset += 0.01f;

                if (ticks < 40) {
                    // Fade in master & scale logo
                    masterAlpha = Math.min(1f, masterAlpha + 0.05f);
                    logoScale = Math.min(1f, logoScale + 0.015f); // 0.5 -> 1.0
                } else if (ticks >= 40 && ticks < 100) {
                    // Logo is settled, fade in text
                    masterAlpha = 1f;
                    logoScale = 1f;
                    textAlpha = Math.min(1f, textAlpha + 0.03f);
                } else if (ticks >= 100 && ticks < 280) {
                    // Hold and show full progress (~2.9s)
                    textAlpha = 1f;
                } else if (ticks >= 280 && ticks < 320) {
                    // Fade out
                    masterAlpha = Math.max(0f, masterAlpha - 0.025f);
                } else if (ticks >= 320) {
                    // Done
                    animator.stop();
                    dispose();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
                panel.repaint();
            }
        });
        animator.start();
    }

    private void prepareLogo() {
        java.net.URL logoUrlOuter = getClass().getClassLoader().getResource("logo.jpg");
        if (logoUrlOuter != null) {
            ImageIcon icon = new ImageIcon(logoUrlOuter);
            Image rawImg = icon.getImage();

            // Mask white to transparent
            ImageFilter filter = new RGBImageFilter() {
                public int markerRGB = Color.WHITE.getRGB() | 0xFF000000;

                @Override
                public final int filterRGB(int x, int y, int rgb) {
                    if ((rgb | 0xFF000000) == markerRGB) {
                        return 0x00FFFFFF & rgb;
                    } else if (Math.abs(new Color(rgb).getRed() - 255) < 15
                            && Math.abs(new Color(rgb).getGreen() - 255) < 15
                            && Math.abs(new Color(rgb).getBlue() - 255) < 15) {
                        return 0x00FFFFFF & rgb;
                    }
                    return rgb;
                }
            };
            ImageProducer ip = new FilteredImageSource(rawImg.getSource(), filter);
            logoImg = Toolkit.getDefaultToolkit().createImage(ip);
        }
    }
}
