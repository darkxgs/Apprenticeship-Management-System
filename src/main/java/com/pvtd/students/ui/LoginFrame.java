package com.pvtd.students.ui;

import com.pvtd.students.models.User;
import com.pvtd.students.services.AuthService;
import com.pvtd.students.services.LogService;
import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.extras.components.FlatPasswordField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

public class LoginFrame extends JFrame {

    private FlatTextField usernameField;
    private FlatPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("نظام إدارة نتائج دبلوم التلمذة الصناعية - مصلحة الكفاية الإنتاجية");
        setSize(1000, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        // Setup aggressive window focus
        setAlwaysOnTop(true);
        toFront();
        requestFocus();

        // Remove always-on-top after a short delay so the OS has time to process the
        // focus
        Timer focusTimer = new Timer(500, e -> setAlwaysOnTop(false));
        focusTimer.setRepeats(false);
        focusTimer.start();

        initComponents();
    }

    private void initComponents() {
        // Preferred Modern Font (Cairo > Tajawal > IBM Plex Arabic > Segoe UI)
        String fallbackFont = "Segoe UI";
        String[] preferred = { "Cairo", "Tajawal", "IBM Plex Arabic", "Segoe UI" };
        for (String f : preferred) {
            boolean exists = false;
            for (String av : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
                if (av.equalsIgnoreCase(f)) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                fallbackFont = f;
                break;
            }
        }
        final String APP_FONT = fallbackFont;

        // ─── Root: split into LEFT (branding) + RIGHT (form) ─────────────────
        JPanel root = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0xF8FAFC)); // Lighter/cleaner premium BG
                g.fillRect(getWidth() / 2, 0, getWidth() / 2, getHeight());
            }
        };
        root.setBackground(new Color(0xF8FAFC));
        setContentPane(root);

        // ═══════════════════════════════════════════════════════════════
        // LEFT — Premium Animated Gradient Panel
        // ═══════════════════════════════════════════════════════════════
        JPanel brandPanel = new JPanel() {
            private float offset = 0f;
            {
                Timer t = new Timer(50, e -> {
                    offset += 0.005f;
                    if (offset > Math.PI * 2)
                        offset = 0f;
                    repaint();
                });
                t.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep modern enterprise blue gradient
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(0x0A192F),
                        getWidth(), getHeight(), new Color(0x112240));
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle animated glowing circles
                int w = getWidth();
                int h = getHeight();

                int cx1 = (int) (Math.sin(offset) * 50 - 100);
                int cy1 = (int) (Math.cos(offset) * 50 - 100);
                g2.setColor(new Color(0x64FFDA, false));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
                g2.fillOval(cx1, cy1, 400, 400);

                int cx2 = (int) (w - 250 + Math.cos(offset * 0.8) * 40);
                int cy2 = (int) (h - 300 + Math.sin(offset * 0.8) * 40);
                g2.setColor(new Color(0x1D4ED8, false));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                g2.fillOval(cx2, cy2, 500, 500);

                // Grid pattern overlay (sleeker)
                g2.setColor(new Color(0xFFFFFF));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.02f));
                g2.setStroke(new BasicStroke(0.5f));
                for (int x = 0; x < w; x += 50)
                    g2.drawLine(x, 0, x, h);
                for (int y = 0; y < h; y += 50)
                    g2.drawLine(0, y, w, y);

                g2.dispose();
            }
        };
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(80, 60, 60, 60));

        // Logo with transparent mask logic
        JLabel logoIcon = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle glow behind logo
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(getWidth() / 2 - 80, getHeight() / 2 - 80, 160, 160);
                g2.dispose();
                super.paintComponent(g);
            }
        };

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
                        return 0x00FFFFFF & rgb; // transparent
                    } else if (Math.abs(new Color(rgb).getRed() - 255) < 15
                            && Math.abs(new Color(rgb).getGreen() - 255) < 15
                            && Math.abs(new Color(rgb).getBlue() - 255) < 15) {
                        return 0x00FFFFFF & rgb; // smooth edge white removal
                    }
                    return rgb;
                }
            };
            ImageProducer ip = new FilteredImageSource(rawImg.getSource(), filter);
            Image transparentImg = Toolkit.getDefaultToolkit().createImage(ip);

            Image scaledImg = transparentImg.getScaledInstance(140, 140, Image.SCALE_SMOOTH);
            logoIcon.setIcon(new ImageIcon(scaledImg));
        }
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoIcon.setBorder(new EmptyBorder(0, 0, 30, 0));

        // Organization - Smaller, lighter above title
        JLabel orgLabel = new JLabel("مصلحة الكفاية الإنتاجية والتدريب المهني", SwingConstants.CENTER);
        orgLabel.setFont(new Font(APP_FONT, Font.PLAIN, 18));
        orgLabel.setForeground(new Color(0x94A3B8));
        orgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        orgLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // System Title - Most prominent, tighter line height
        // Using HTML for multi-line to control line-height better
        JLabel sysTitle = new JLabel("<html><div style='text-align: center; line-height: 1.2;'>"
                + "نظام إدارة نتائج<br>دبلوم التلمذة الصناعية"
                + "</div></html>", SwingConstants.CENTER);
        sysTitle.setFont(new Font(APP_FONT, Font.BOLD, 42));
        sysTitle.setForeground(new Color(0xFFFFFF));
        sysTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sysTitle.setBorder(new EmptyBorder(0, 0, 55, 0));

        // Feature bullets - Icon left, Text right (aligned RTL)
        JPanel featureList = new JPanel();
        featureList.setLayout(new BoxLayout(featureList, BoxLayout.Y_AXIS));
        featureList.setOpaque(false);
        featureList.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] features = {
                "إدارة بيانات الطلاب والدرجات",
                "حساب النتائج تلقائياً",
                "إصدار الشهادات النهائية",
                "استيراد البيانات من Excel"
        };
        for (String f : features) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
            row.setOpaque(false);

            JLabel textLbl = new JLabel(f, SwingConstants.RIGHT);
            textLbl.setFont(new Font(APP_FONT, Font.PLAIN, 17));
            textLbl.setForeground(new Color(0xCBD5E1));

            JLabel iconLbl = new JLabel("", SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Green circle
                    g2.setColor(new Color(0x10B981));
                    g2.fillOval(0, 0, getWidth(), getHeight());

                    // Draw tick mark programmatically instead of using fonts
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.drawLine(cx - 3, cy, cx - 1, cy + 3);
                    g2.drawLine(cx - 1, cy + 3, cx + 4, cy - 3);

                    g2.dispose();
                }
            };
            iconLbl.setPreferredSize(new Dimension(18, 18));

            row.add(textLbl);
            row.add(iconLbl); // RTL: text then icon visually puts icon on the right, but wait,
                              // FlowLayout.RIGHT puts first component on the right.
                              // So row.add(textLbl); row.add(iconLbl) means textLbl is on the right, iconLbl
                              // on the left.
                              // Wait, FlowRight means Right-to-Left visually if component orientation is RTL.
                              // Let's explicitly set orientation.
            row.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            // In RTL, the first added component is on the far right. So icon first, then
            // text -> Icon on right, Text on left.
            // But the user requested "icon text بدل icon في الطرف" meaning Icon then Text
            // (RTL: Icon on right, Text on left).
            row.removeAll();
            row.add(iconLbl);
            row.add(textLbl);

            featureList.add(row);
            featureList.add(Box.createVerticalStrut(14));
        }

        brandPanel.add(Box.createVerticalGlue());
        brandPanel.add(logoIcon);
        brandPanel.add(orgLabel);
        brandPanel.add(sysTitle);
        brandPanel.add(featureList);
        brandPanel.add(Box.createVerticalGlue());

        // ═══════════════════════════════════════════════════════════════
        // RIGHT — Premium Large Glass-like Card Panel
        // ═══════════════════════════════════════════════════════════════
        JPanel formOuter = new JPanel(new GridBagLayout());
        formOuter.setBackground(new Color(0xF8FAFC));

        JPanel formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep multi-layered premium shadow
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fill(new RoundRectangle2D.Double(0, 25, getWidth(), getHeight() - 25, 24, 24));
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fill(new RoundRectangle2D.Double(0, 5, getWidth(), getHeight() - 5, 24, 24));

                // Pure white card
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight() - 8, 24, 24));
                g2.dispose();
            }
        };
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setOpaque(false);
        formCard.setBorder(new EmptyBorder(50, 40, 50, 40));
        formCard.setPreferredSize(new Dimension(480, 560)); // Wider 480px

        // Welcome text
        JLabel welcomeLabel = new JLabel("تسجيل الدخول", SwingConstants.RIGHT);
        welcomeLabel.setFont(new Font(APP_FONT, Font.BOLD, 32));
        welcomeLabel.setForeground(new Color(0x0F172A));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel instructLabel = new JLabel("أدخل بيانات الاعتماد للمتابعة", SwingConstants.RIGHT);
        instructLabel.setFont(new Font(APP_FONT, Font.PLAIN, 16));
        instructLabel.setForeground(new Color(0x64748B));
        instructLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        instructLabel.setBorder(new EmptyBorder(6, 0, 40, 0));

        // Inputs generator helper
        class InputHelper {
            JPanel createGroup(String label, JTextField field, String iconChar) {
                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setOpaque(false);
                wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
                wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel lbl = new JLabel(label, SwingConstants.RIGHT);
                lbl.setFont(new Font(APP_FONT, Font.BOLD, 15));
                lbl.setForeground(new Color(0x334155));
                lbl.setBorder(new EmptyBorder(0, 0, 10, 0));

                field.setPreferredSize(new Dimension(0, 50));
                field.setFont(new Font(APP_FONT, Font.PLAIN, 16));

                // Premium Input Styles
                field.setBackground(Color.WHITE);
                field.setForeground(new Color(0x1E293B));

                if (field instanceof FlatTextField) {
                    ((FlatTextField) field).putClientProperty("JComponent.roundRect", true);
                    ((FlatTextField) field).putClientProperty("JTextField.padding", new Insets(5, 12, 5, 40));
                    ((FlatTextField) field).putClientProperty("JTextField.placeholderForeground", new Color(0x94A3B8));
                } else if (field instanceof FlatPasswordField) {
                    ((FlatPasswordField) field).putClientProperty("JComponent.roundRect", true);
                    ((FlatPasswordField) field).putClientProperty("JTextField.padding", new Insets(5, 12, 5, 40));
                    ((FlatPasswordField) field).putClientProperty("JTextField.placeholderForeground",
                            new Color(0x94A3B8));
                    ((FlatPasswordField) field).putClientProperty("JTextField.showRevealButton", true);
                }

                // Outline Focus Effect styling via FlatLaf Client Properties
                field.putClientProperty("JComponent.outline", new Color(0xE3E8EF));
                field.putClientProperty("Component.focusWidth", 4);
                field.putClientProperty("Component.focusColor", new Color(59, 130, 246, 38)); // 0.15 alpha approx
                field.putClientProperty("Component.focusedBorderColor", new Color(0x3B82F6));

                field.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

                // Add built-in trailing icon via FlatLaf
                JLabel iconLbl = new JLabel(iconChar, SwingConstants.CENTER);
                iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                iconLbl.setForeground(new Color(0x94A3B8));
                field.putClientProperty("JTextField.trailingComponent", iconLbl);

                wrapper.add(lbl, BorderLayout.NORTH);
                wrapper.add(field, BorderLayout.CENTER);
                return wrapper;
            }
        }

        InputHelper ih = new InputHelper();
        usernameField = new FlatTextField();
        usernameField.setPlaceholderText("اسم المستخدم");
        JPanel userWrapper = ih.createGroup("اسم المستخدم", usernameField, "👤");

        passwordField = new FlatPasswordField();
        passwordField.setPlaceholderText("كلمة المرور");
        JPanel passWrapper = ih.createGroup("كلمة المرور", passwordField, "🔒");
        passWrapper.setBorder(new EmptyBorder(16, 0, 0, 0));

        // Status label
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font(APP_FONT, Font.BOLD, 14));
        statusLabel.setForeground(new Color(0xEF4444));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        statusLabel.setBorder(new EmptyBorder(15, 0, 0, 0));
        statusLabel.setVisible(false);

        // Gradient Login button with hover effect
        loginButton = new JButton("تسجيل الدخول") {
            private float hoverAlpha = 0f;
            private int yOffset = 0;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        yOffset = -1; // translateY(-1px)
                        Timer t = new Timer(15, ae -> {
                            hoverAlpha = Math.min(1f, hoverAlpha + 0.1f);
                            repaint();
                            if (hoverAlpha >= 1f)
                                ((Timer) ae.getSource()).stop();
                        });
                        t.start();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        yOffset = 0;
                        Timer t = new Timer(15, ae -> {
                            hoverAlpha = Math.max(0f, hoverAlpha - 0.1f);
                            repaint();
                            if (hoverAlpha <= 0f)
                                ((Timer) ae.getSource()).stop();
                        });
                        t.start();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int height = getHeight() - 5;

                // Deep shadow on hover
                if (hoverAlpha > 0) {
                    g2.setColor(new Color(59, 130, 246, (int) (89 * hoverAlpha))); // 0.35 alpha max
                    g2.fill(new RoundRectangle2D.Double(2, 8 + yOffset, getWidth() - 4, height, 16, 16));
                }

                // Gradient background (Linear 135deg ish)
                GradientPaint gp = new GradientPaint(0, yOffset, new Color(0x3B82F6), getWidth(), height + yOffset,
                        new Color(0x2563EB));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, yOffset, getWidth(), height, 16, 16));

                // Hover brightness overlay
                if (hoverAlpha > 0) {
                    g2.setColor(new Color(1f, 1f, 1f, hoverAlpha * 0.15f));
                    g2.fill(new RoundRectangle2D.Double(0, yOffset, getWidth(), height, 16, 16));
                }

                // Press overlay
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0, 0, 0, 0.15f));
                    g2.fill(new RoundRectangle2D.Double(0, yOffset, getWidth(), height, 16, 16));
                }

                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (height + fm.getAscent() - fm.getDescent()) / 2 + yOffset;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        loginButton.setFont(new Font(APP_FONT, Font.BOLD, 18));
        loginButton.setPreferredSize(new Dimension(0, 56)); // Taller button
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBorder(new EmptyBorder(24, 0, 0, 0));

        loginButton.addActionListener(e -> handleLogin());

        KeyAdapter enter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    handleLogin();
            }
        };
        usernameField.addKeyListener(enter);
        passwordField.addKeyListener(enter);

        // Footer hint - moved to bottom of outer layout to detach from card if wanted,
        // but user wanted it inside card at bottom
        JLabel hint = new JLabel("اسم المستخدم الافتراضي: admin / admin123", SwingConstants.CENTER);
        hint.setFont(new Font(APP_FONT, Font.PLAIN, 12));
        hint.setForeground(new Color(0x94A3B8));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setBorder(new EmptyBorder(30, 0, 0, 0));

        formCard.add(welcomeLabel);
        formCard.add(instructLabel);
        formCard.add(userWrapper);
        formCard.add(passWrapper);
        formCard.add(loginButton);
        formCard.add(statusLabel); // Moved status under button or just keep space
        formCard.add(Box.createVerticalGlue()); // Push hint to bottom
        formCard.add(hint);

        formOuter.add(formCard);

        root.add(brandPanel);
        root.add(formOuter);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        statusLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            showError("يرجى إدخال اسم المستخدم وكلمة المرور");
            return;
        }

        loginButton.setText("جاري التحقق...");
        loginButton.setEnabled(false);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return AuthService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        LogService.logAction(user.getUsername(), "LOGIN", "تسجيل دخول ناجح.");
                        LoginFrame.this.dispose();
                        SwingUtilities.invokeLater(() -> new AppFrame(user).setVisible(true));
                    } else {
                        showError("اسم المستخدم أو كلمة المرور غير صحيحة");
                        passwordField.setText("");
                        passwordField.requestFocus();
                        loginButton.setText("تسجيل الدخول");
                        loginButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    showError("خطأ في الاتصال بقاعدة البيانات");
                    loginButton.setText("تسجيل الدخول");
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void showError(String msg) {
        statusLabel.setText("⚠ " + msg);
        statusLabel.setVisible(true);
        // Shake animation
        Timer shake = new Timer(30, null);
        final int[] count = { 0 };
        final int[] dir = { 1 };
        shake.addActionListener(e -> {
            setLocation(getX() + 6 * dir[0], getY());
            dir[0] *= -1;
            if (++count[0] >= 8) {
                shake.stop();
                setLocation(getX() - (count[0] % 2 == 0 ? 0 : 3), getY());
            }
        });
        shake.start();
    }
}
