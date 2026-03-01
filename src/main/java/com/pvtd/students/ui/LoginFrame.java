package com.pvtd.students.ui;

import com.pvtd.students.models.User;
import com.pvtd.students.services.AuthService;
import com.pvtd.students.services.LogService;
import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.extras.components.FlatPasswordField;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {

    private FlatTextField usernameField;
    private FlatPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("نظام إدارة دبلوم التلمذة الصناعية - مصلحة الكفاية الإنتاجية");
        setSize(1000, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximized by default
        setResizable(true); // Allow resizing just in case, but default is max
        setUndecorated(false);
        initComponents();
    }

    private void initComponents() {
        // ─── Root: split into LEFT (branding) + RIGHT (form) ─────────────────
        JPanel root = new JPanel(new GridLayout(1, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Flat neutral BG for right side
                g.setColor(new Color(0xF8FAFC));
                g.fillRect(getWidth() / 2, 0, getWidth() / 2, getHeight());
            }
        };
        root.setBackground(new Color(0xF8FAFC));
        setContentPane(root);

        // ═══════════════════════════════════════════════════════════════
        // LEFT — Branded Illustration Panel
        // ═══════════════════════════════════════════════════════════════
        JPanel brandPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep navy gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(0x0F172A),
                        getWidth(), getHeight(), new Color(0x1E3A5F));
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative glowing circles
                g2.setColor(new Color(0x2563EB, false));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                g2.fillOval(-80, -80, 300, 300);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
                g2.fillOval(getWidth() - 150, getHeight() - 200, 280, 280);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
                g2.fillOval(50, getHeight() / 2, 200, 200);

                // Grid pattern overlay
                g2.setColor(new Color(0xFFFFFF));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.03f));
                g2.setStroke(new BasicStroke(0.5f));
                for (int x = 0; x < getWidth(); x += 40)
                    g2.drawLine(x, 0, x, getHeight());
                for (int y = 0; y < getHeight(); y += 40)
                    g2.drawLine(0, y, getWidth(), y);

                g2.dispose();
            }
        };
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(60, 50, 50, 50));

        // Ministry logo area
        JLabel logoIcon = new JLabel("🏛", SwingConstants.CENTER);
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoIcon.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel orgLabel = new JLabel("جمهورية مصر العربية", SwingConstants.CENTER);
        orgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orgLabel.setForeground(new Color(0x93C5FD));
        orgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ministryLabel = new JLabel("مصلحة الكفاية الإنتاجية", SwingConstants.CENTER);
        ministryLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        ministryLabel.setForeground(Color.WHITE);
        ministryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ministryLabel.setBorder(new EmptyBorder(4, 0, 2, 0));

        JLabel ministryLabel2 = new JLabel("والتدريب المهني", SwingConstants.CENTER);
        ministryLabel2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        ministryLabel2.setForeground(Color.WHITE);
        ministryLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2563EB));
        sep.setPreferredSize(new Dimension(200, 1));
        sep.setMaximumSize(new Dimension(220, 2));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sysTitle = new JLabel("نظام إدارة نتائج", SwingConstants.CENTER);
        sysTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        sysTitle.setForeground(new Color(0x60A5FA));
        sysTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sysTitle.setBorder(new EmptyBorder(20, 0, 6, 0));

        JLabel sysTitle2 = new JLabel("دبلوم التلمذة الصناعية", SwingConstants.CENTER);
        sysTitle2.setFont(new Font("Segoe UI", Font.BOLD, 26));
        sysTitle2.setForeground(new Color(0x60A5FA));
        sysTitle2.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Feature bullets
        JPanel featureList = new JPanel();
        featureList.setLayout(new BoxLayout(featureList, BoxLayout.Y_AXIS));
        featureList.setOpaque(false);
        featureList.setBorder(new EmptyBorder(30, 10, 0, 10));
        featureList.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] features = {
                "•  إدارة بيانات الطلاب والدرجات",
                "•  حساب النتائج تلقائياً",
                "•  إصدار الشهادات والتقارير",
                "•  استيراد بيانات من Excel"
        };
        for (String f : features) {
            JLabel fl = new JLabel(f, SwingConstants.RIGHT);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fl.setForeground(new Color(0xCBD5E1));
            fl.setAlignmentX(Component.CENTER_ALIGNMENT);
            fl.setBorder(new EmptyBorder(5, 0, 5, 0));
            featureList.add(fl);
        }

        // Version tag
        JLabel versionLabel = new JLabel("v2.0  |  ENG.Seif Ashraf, Eng.Seif Ragab", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(0x475569));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandPanel.add(Box.createVerticalGlue());
        brandPanel.add(logoIcon);
        brandPanel.add(orgLabel);
        brandPanel.add(ministryLabel);
        brandPanel.add(ministryLabel2);
        brandPanel.add(Box.createVerticalStrut(18));
        brandPanel.add(sep);
        brandPanel.add(sysTitle);
        brandPanel.add(sysTitle2);
        brandPanel.add(featureList);
        brandPanel.add(Box.createVerticalGlue());
        brandPanel.add(versionLabel);

        // ═══════════════════════════════════════════════════════════════
        // RIGHT — Login Form Panel
        // ═══════════════════════════════════════════════════════════════
        JPanel formOuter = new JPanel(new GridBagLayout());
        formOuter.setBackground(new Color(0xF8FAFC));

        JPanel formCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Subtle shadow
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fill(new RoundRectangle2D.Double(3, 4, getWidth() - 4, getHeight() - 4, 24, 24));
                // White card
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 4, getHeight() - 4, 24, 24));
                g2.dispose();
            }
        };
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setOpaque(false);
        formCard.setBorder(new EmptyBorder(44, 44, 44, 44));
        formCard.setPreferredSize(new Dimension(380, 480));

        // Welcome text
        JLabel welcomeLabel = new JLabel("مرحباً بك", SwingConstants.RIGHT);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(new Color(0x0F172A));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel instructLabel = new JLabel("سجّل دخولك للمتابعة", SwingConstants.RIGHT);
        instructLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instructLabel.setForeground(new Color(0x64748B));
        instructLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        instructLabel.setBorder(new EmptyBorder(4, 0, 32, 0));

        // Username field
        JPanel userWrapper = new JPanel(new BorderLayout());
        userWrapper.setOpaque(false);
        userWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        userWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLbl = new JLabel("اسم المستخدم", SwingConstants.RIGHT);
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        userLbl.setForeground(new Color(0x334155));
        userLbl.setBorder(new EmptyBorder(0, 0, 8, 0));

        usernameField = new FlatTextField();
        usernameField.setPreferredSize(new Dimension(0, 48));
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setPlaceholderText("أدخل اسم المستخدم هنا...");
        usernameField.setShowClearButton(true);
        usernameField.putClientProperty("JComponent.roundRect", true);
        usernameField.putClientProperty("JTextField.padding", new Insets(5, 12, 5, 12));
        usernameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        usernameField.putClientProperty("JTextField.placeholderForeground", new Color(0x94A3B8));

        userWrapper.add(userLbl, BorderLayout.NORTH);
        userWrapper.add(usernameField, BorderLayout.CENTER);

        // Password field
        JPanel passWrapper = new JPanel(new BorderLayout());
        passWrapper.setOpaque(false);
        passWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        passWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        passWrapper.setBorder(new EmptyBorder(20, 0, 0, 0)); // Spacing above password

        JLabel passLbl = new JLabel("كلمة المرور", SwingConstants.RIGHT);
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        passLbl.setForeground(new Color(0x334155));
        passLbl.setBorder(new EmptyBorder(0, 0, 8, 0));

        passwordField = new FlatPasswordField();
        passwordField.setPreferredSize(new Dimension(0, 48));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setPlaceholderText("أدخل كلمة المرور هنا...");
        passwordField.putClientProperty("JTextField.showRevealButton", true);
        passwordField.putClientProperty("JComponent.roundRect", true);
        passwordField.putClientProperty("JTextField.padding", new Insets(5, 12, 5, 12));
        passwordField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        passwordField.putClientProperty("JTextField.placeholderForeground", new Color(0x94A3B8));

        passWrapper.add(passLbl, BorderLayout.NORTH);
        passWrapper.add(passwordField, BorderLayout.CENTER);

        // Status label (shows error inline)
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(0xDC2626));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        statusLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        statusLabel.setVisible(false);

        // Login button
        loginButton = new JButton("تسجيل الدخول") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? new Color(0x1D4ED8)
                        : getModel().isRollover() ? new Color(0x3B82F6)
                                : new Color(0x2563EB);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginButton.setPreferredSize(new Dimension(0, 48));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginButton.setContentAreaFilled(false);
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBorder(new EmptyBorder(20, 0, 0, 0));

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

        // Footer hint
        JLabel hint = new JLabel("اسم المستخدم الافتراضي: admin / admin123", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(new Color(0xCBD5E1));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setBorder(new EmptyBorder(20, 0, 0, 0));

        formCard.add(welcomeLabel);
        formCard.add(instructLabel);
        formCard.add(userWrapper);
        formCard.add(passWrapper);
        formCard.add(statusLabel);
        formCard.add(loginButton);
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
        statusLabel.setText("⚠  " + msg);
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
