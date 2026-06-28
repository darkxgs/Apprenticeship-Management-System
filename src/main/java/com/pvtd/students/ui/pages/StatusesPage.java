package com.pvtd.students.ui.pages;

import com.pvtd.students.services.StatusesService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.DropShadowBorder;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatusesPage extends JPanel {

    private final AppFrame frame;
    private JPanel gridPanel;

    // Semantic Color map - [bgColor, accentColor]
    private static final Color[][] PALETTE = {
            { new Color(0xDBEAFE), new Color(0x2563EB) }, // blue
            { new Color(0xDCFCE7), new Color(0x059669) }, // green
            { new Color(0xFEE2E2), new Color(0xDC2626) }, // red
            { new Color(0xFEF3C7), new Color(0xD97706) }, // amber
            { new Color(0xEDE9FE), new Color(0x7C3AED) }, // violet
            { new Color(0xFCE7F3), new Color(0xBE185D) }, // pink
            { new Color(0xF1F5F9), new Color(0x475569) }, // slate
            { new Color(0xFFEDD5), new Color(0xEA580C) }, // orange
    };

    // Color labels that are shown in the color picker combo
    private static final String[] COLOR_NAMES = {
            "ازرق", "اخضر", "احمر", "ذهبي", "بنفسجي", "وردي", "رمادي", "برتقالي"
    };

    public StatusesPage(AppFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_LIGHT);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildGrid(), BorderLayout.CENTER);

        loadCards();
    }

    // ── Header ─────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JLabel titleLbl = new JLabel("إدارة حالات الطلاب", SwingConstants.RIGHT);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLbl = new JLabel("أضف أو احذف الحالات التي تُستخدم في تقييم الطلاب — يمكنك ربط كل حالة برقم سالب يُستخدم عند إدخال الدرجات", SwingConstants.RIGHT);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(titleLbl);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subLbl);

        JButton btnAdd = new JButton("+ إضافة حالة جديدة");
        btnAdd.setFont(UITheme.FONT_HEADER);
        btnAdd.setBackground(UITheme.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.setPreferredSize(new Dimension(190, 40));
        btnAdd.addActionListener(e -> handleAdd());

        header.add(titleBlock, BorderLayout.EAST);
        header.add(btnAdd, BorderLayout.WEST);
        return header;
    }

    // ── Card Grid ────────────────────────────────────────────────────────────────────
    private JScrollPane buildGrid() {
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, 5, 16, 16));
        gridPanel.setOpaque(false);
        gridPanel.setBackground(UITheme.BG_LIGHT);
        gridPanel.setBorder(new EmptyBorder(16, 24, 24, 24));

        // TO PREVENT VERTICAL STRETCHING: Wrap gridPanel in a panel that keeps it at the top
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(topWrapper,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BG_LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        scroll.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int availableWidth = scroll.getViewport().getWidth();
                int cardWidth = 270 + 16;
                int cols = Math.max(1, availableWidth / cardWidth);
                if (gridPanel.getLayout() instanceof GridLayout) {
                    ((GridLayout) gridPanel.getLayout()).setColumns(cols);
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }
            }
        });

        return scroll;
    }

    private void loadCards() {
        gridPanel.removeAll();
        java.util.Map<String, Integer> counts = com.pvtd.students.services.StudentService.getDashboardStats();

        LinkedHashMap<String, Integer> statusesWithCodes = StatusesService.getAllStatusesWithCodes();

        // Also ensure core statuses appear
        if (!statusesWithCodes.containsKey("ناجح")) statusesWithCodes.put("ناجح", null);
        if (!statusesWithCodes.containsKey("راسب")) statusesWithCodes.put("راسب", null);
        if (!statusesWithCodes.containsKey("دور ثاني")) statusesWithCodes.put("دور ثاني", null);

        for (Map.Entry<String, Integer> entry : statusesWithCodes.entrySet()) {
            String status = entry.getKey();
            Integer code = entry.getValue();
            Color[] colors = getSemanticColor(status);
            gridPanel.add(buildCard(status, code, colors[0], colors[1], counts.getOrDefault(status, 0)));
        }
        if (statusesWithCodes.isEmpty()) {
            gridPanel.setLayout(new BorderLayout());
            JLabel empty = new JLabel(
                "<html><div style='text-align:center'>لا توجد حالات مُضافة بعد.<br>اضغط &laquo;+ إضافة حالة جديدة&raquo; لإنشاء أول حالة.</div></html>",
                SwingConstants.CENTER);
            empty.setFont(new Font("Tahoma", Font.PLAIN, 15));
            empty.setForeground(UITheme.TEXT_SECONDARY);
            empty.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            gridPanel.add(empty, BorderLayout.CENTER);
        } else {
            if (!(gridPanel.getLayout() instanceof GridLayout)) {
                gridPanel.setLayout(new GridLayout(0, 5, 16, 16));
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private Color[] getSemanticColor(String status) {
        if (status.contains("ناجح"))
            return new Color[] { new Color(0xDCFCE7), new Color(0x059669) };
        if (status.contains("راسب"))
            return new Color[] { new Color(0xFEE2E2), new Color(0xDC2626) };
        if (status.contains("دور ثاني"))
            return new Color[] { new Color(0xFFEDD5), new Color(0xEA580C) };
        if (status.contains("مؤجل"))
            return new Color[] { new Color(0xDBEAFE), new Color(0x2563EB) };
        if (status.contains("معتذر"))
            return new Color[] { new Color(0xF1F5F9), new Color(0x475569) };
        if (status.contains("غائب"))
            return new Color[] { new Color(0xFEF3C7), new Color(0xD97706) };
        if (status.contains("محروم") || status.contains("مفصول"))
            return new Color[] { new Color(0xFCE7F3), new Color(0xBE185D) };
        int hash = Math.abs(status.hashCode()) % PALETTE.length;
        return PALETTE[hash];
    }

    private JPanel buildCard(String statusName, Integer statusCode, Color bgColor, Color accentColor, int studentCount) {
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setOpaque(false);
        // Fixed height to prevent stretching
        cardWrapper.setPreferredSize(new Dimension(270, 150));
        cardWrapper.setMinimumSize(new Dimension(270, 150));
        cardWrapper.setMaximumSize(new Dimension(270, 150));
        cardWrapper.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 6, 0.07f, 16, UITheme.BG_LIGHT),
                new EmptyBorder(0, 0, 0, 0)));

        JPanel cardInner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Base white card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Colored top bar
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 10, 16, 16);
                g2.fillRect(0, 6, getWidth(), 4);
                g2.dispose();
            }
        };
        cardInner.setOpaque(false);
        cardInner.setBorder(new EmptyBorder(18, 16, 14, 16));

        // Top: Status Name + Code Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JTextArea nameLbl = new JTextArea(statusName);
        nameLbl.setFont(new Font("Tahoma", Font.BOLD, 14));
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);
        nameLbl.setBackground(Color.WHITE);
        nameLbl.setOpaque(false);
        nameLbl.setEditable(false);
        nameLbl.setFocusable(false);
        nameLbl.setLineWrap(true);
        nameLbl.setWrapStyleWord(true);
        nameLbl.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        nameLbl.setBorder(null);

        // Code badge - shows the negative number
        JPanel codeBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        codeBadge.setOpaque(false);
        codeBadge.setLayout(new BorderLayout());

        if (statusCode != null) {
            JLabel codeLabel = new JLabel(String.valueOf(statusCode), SwingConstants.CENTER);
            codeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            codeLabel.setForeground(accentColor);
            codeBadge.add(codeLabel, BorderLayout.CENTER);
            codeBadge.setPreferredSize(new Dimension(40, 24));
            codeBadge.setToolTipText("كود الحالة: عند إدخال " + statusCode + " كدرجة، يتم تعيين الحالة تلقائياً");
        } else {
            // Colored dot badge (no code)
            JPanel dotInner = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(accentColor);
                    int cx = getWidth() / 2, cy = getHeight() / 2;
                    g2.fillOval(cx - 4, cy - 4, 8, 8);
                    g2.dispose();
                }
            };
            dotInner.setOpaque(false);
            codeBadge.add(dotInner, BorderLayout.CENTER);
            codeBadge.setPreferredSize(new Dimension(28, 22));
            codeBadge.setToolTipText("بدون كود — لا يمكن تعيين هذه الحالة تلقائياً عبر الدرجات");
        }

        topRow.add(nameLbl, BorderLayout.EAST);
        topRow.add(codeBadge, BorderLayout.WEST);

        // Middle: Code info label
        JPanel middleRow = new JPanel(new BorderLayout());
        middleRow.setOpaque(false);
        middleRow.setBorder(new EmptyBorder(6, 0, 0, 0));

        String codeText = statusCode != null
                ? "كود الدرجة: " + statusCode
                : "بدون كود";
        JLabel codeInfoLbl = new JLabel(codeText, SwingConstants.RIGHT);
        codeInfoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        codeInfoLbl.setForeground(statusCode != null ? accentColor : new Color(0xA0AEC0));
        middleRow.add(codeInfoLbl, BorderLayout.EAST);

        // Bottom: Count label + Buttons
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.setBorder(new EmptyBorder(6, 0, 0, 0));

        JLabel countLbl = new JLabel("الطلاب: " + studentCount, SwingConstants.LEFT);
        countLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLbl.setForeground(UITheme.TEXT_SECONDARY);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);

        // Edit: filled primary
        JButton btnEdit = new JButton("تعديل");
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setBackground(accentColor);
        btnEdit.setFocusPainted(false);
        btnEdit.setPreferredSize(new Dimension(72, 28));
        btnEdit.putClientProperty("JButton.buttonType", "roundRect");
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> handleEdit(statusName, statusCode));

        // Delete: danger outline
        JButton btnDel = new JButton("حذف") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(UITheme.DANGER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnDel.setContentAreaFilled(false);
        btnDel.setBorderPainted(false);
        btnDel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDel.setForeground(UITheme.DANGER);
        btnDel.setFocusPainted(false);
        btnDel.setPreferredSize(new Dimension(65, 28));
        btnDel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDel.addActionListener(e -> handleDelete(statusName));

        btnRow.add(btnEdit);
        btnRow.add(btnDel);

        bottomRow.add(countLbl, BorderLayout.EAST);
        bottomRow.add(btnRow, BorderLayout.WEST);

        // Assemble card
        JPanel centerBlock = new JPanel();
        centerBlock.setOpaque(false);
        centerBlock.setLayout(new BoxLayout(centerBlock, BoxLayout.Y_AXIS));
        centerBlock.add(middleRow);

        cardInner.add(topRow, BorderLayout.NORTH);
        cardInner.add(centerBlock, BorderLayout.CENTER);
        cardInner.add(bottomRow, BorderLayout.SOUTH);
        cardWrapper.add(cardInner, BorderLayout.CENTER);

        // Hover effect
        cardWrapper.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cardWrapper.setBorder(BorderFactory.createCompoundBorder(
                        new DropShadowBorder(Color.BLACK, 12, 0.12f, 18, UITheme.BG_LIGHT),
                        new EmptyBorder(0, 0, 0, 0)));
                cardWrapper.revalidate();
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                cardWrapper.setBorder(BorderFactory.createCompoundBorder(
                        new DropShadowBorder(Color.BLACK, 6, 0.07f, 16, UITheme.BG_LIGHT),
                        new EmptyBorder(0, 0, 0, 0)));
                cardWrapper.revalidate();
            }
        });

        return cardWrapper;
    }

    // ── Actions ────────────────────────────────────────────────────────────────
    private void handleAdd() {
        JPanel p = new JPanel(new GridLayout(3, 2, 10, 10));
        p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField nameField = new JTextField();
        nameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField codeField = new JTextField();
        codeField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        codeField.setToolTipText("رقم سالب مثل -6 أو -7 — اتركه فارغاً إذا لم تحتاج تعيين تلقائي");

        JComboBox<String> colorCombo = new JComboBox<>(COLOR_NAMES);
        colorCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        // Render each item with its color swatch
        colorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int idx, boolean sel,
                    boolean focus) {
                super.getListCellRendererComponent(list, value, idx, sel, focus);
                if (idx >= 0 && idx < PALETTE.length)
                    setBackground(PALETTE[idx][0]);
                return this;
            }
        });

        p.add(new JLabel("اسم الحالة:", SwingConstants.RIGHT));
        p.add(nameField);
        p.add(new JLabel("كود الدرجة (رقم سالب):", SwingConstants.RIGHT));
        p.add(codeField);
        p.add(new JLabel("اللون:", SwingConstants.RIGHT));
        p.add(colorCombo);

        int result = JOptionPane.showConfirmDialog(this, p, "إضافة حالة جديدة", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                warn("اسم الحالة لا يمكن أن يكون فارغاً.");
                return;
            }

            Integer code = parseCode(codeField.getText().trim());
            if (code != null && code >= 0) {
                warn("كود الحالة يجب أن يكون رقماً سالباً (مثل -6).");
                return;
            }

            // Check code uniqueness
            if (code != null) {
                String existing = StatusesService.getStatusNameByCode(code);
                if (existing != null) {
                    warn("الكود " + code + " مستخدم بالفعل للحالة: «" + existing + "»");
                    return;
                }
            }

            try {
                String user = frame != null ? frame.getLoggedInUser().getUsername() : "SYSTEM";
                StatusesService.addStatus(name, code, user);
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "حدث خطأ: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete(String statusName) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "هل أنت متأكد من حذف الحالة: «" + statusName + "»؟\nسيؤثر ذلك على الطلاب الحاملين لهذه الحالة.",
                "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String user = frame != null ? frame.getLoggedInUser().getUsername() : "SYSTEM";
                StatusesService.deleteStatus(statusName, user);
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "لا يمكن حذف هذه الحالة: " + ex.getMessage(), "خطأ",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEdit(String oldName, Integer oldCode) {
        JPanel p = new JPanel(new GridLayout(2, 2, 10, 10));
        p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField nameField = new JTextField(oldName);
        nameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField codeField = new JTextField(oldCode != null ? String.valueOf(oldCode) : "");
        codeField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        codeField.setToolTipText("رقم سالب مثل -6 أو -7 — اتركه فارغاً إذا لم تحتاج تعيين تلقائي");

        p.add(new JLabel("اسم الحالة:", SwingConstants.RIGHT));
        p.add(nameField);
        p.add(new JLabel("كود الدرجة (رقم سالب):", SwingConstants.RIGHT));
        p.add(codeField);

        int result = JOptionPane.showConfirmDialog(this, p, "تعديل الحالة", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            if (newName.isEmpty()) {
                warn("اسم الحالة لا يمكن أن يكون فارغاً.");
                return;
            }

            Integer newCode = parseCode(codeField.getText().trim());
            if (newCode != null && newCode >= 0) {
                warn("كود الحالة يجب أن يكون رقماً سالباً (مثل -6).");
                return;
            }

            // Check code uniqueness (excluding current status)
            if (newCode != null) {
                String existing = StatusesService.getStatusNameByCode(newCode);
                if (existing != null && !existing.equals(oldName)) {
                    warn("الكود " + newCode + " مستخدم بالفعل للحالة: «" + existing + "»");
                    return;
                }
            }

            try {
                String user = frame != null ? frame.getLoggedInUser().getUsername() : "SYSTEM";
                StatusesService.updateStatus(oldName, newName, newCode, user);
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "حدث خطأ أثناء التعديل: " + ex.getMessage(), "خطأ",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Parses a code string. Returns null if empty, or the parsed integer.
     */
    private Integer parseCode(String text) {
        if (text == null || text.isEmpty()) return null;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            warn("الكود يجب أن يكون رقماً صحيحاً.");
            return 0; // Will be caught by the >= 0 check
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "تنبيه", JOptionPane.WARNING_MESSAGE);
    }
}
