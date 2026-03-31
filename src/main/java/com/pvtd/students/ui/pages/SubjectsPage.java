package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Subject;
import com.pvtd.students.services.SubjectService;
import com.pvtd.students.services.StudentService;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.ui.utils.DropShadowBorder;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * صفحة المواد الدراسية — تعرض كل مادة في كرد منفصلة
 */
public class SubjectsPage extends JPanel {

    private JPanel gridPanel;
    private List<Subject> currentSubjectsList;
    private JComboBox<String> professionCombo;
    private JLabel subjectCountLabel;

    public SubjectsPage(AppFrame frame) {
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(28, 28, 28, 28));
        setBackground(UITheme.BG_LIGHT);

        // ─── Header ───────────────────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout(0, 12));
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel pageTitle = new JLabel("المواد الدراسية", SwingConstants.RIGHT);
        pageTitle.setFont(UITheme.FONT_TITLE);
        pageTitle.setForeground(UITheme.TEXT_PRIMARY);
        pageTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);
        titleBlock.add(pageTitle);

        JButton btnAdd = new JButton("＋ إضافة مادة");
        btnAdd.setFont(UITheme.FONT_HEADER);
        btnAdd.setBackground(UITheme.SUCCESS);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.setPreferredSize(new Dimension(160, 44));
        btnAdd.addActionListener(e -> showAddSubjectDialog());

        JPanel leftArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftArea.setOpaque(false);
        leftArea.add(btnAdd);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.add(titleBlock, BorderLayout.EAST);
        topHeader.add(leftArea, BorderLayout.WEST);

        // ─── Specialization Filter Bar ────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        filterBar.setBackground(UITheme.CARD_BG);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 3, 0.04f, 10, UITheme.BG_LIGHT),
                new EmptyBorder(0, 8, 0, 8)));

        JLabel filterLbl = new JLabel("⎇  اختر المهنة:");
        filterLbl.setFont(UITheme.FONT_HEADER);
        filterLbl.setForeground(UITheme.TEXT_PRIMARY);

        professionCombo = new JComboBox<>();
        professionCombo.setFont(UITheme.FONT_BODY);
        professionCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        professionCombo.setPreferredSize(new Dimension(280, 38));
        loadProfessionsIntoCombo();
        professionCombo.addActionListener(e -> loadSubjects());

        subjectCountLabel = new JLabel("", SwingConstants.LEFT);
        subjectCountLabel.setFont(UITheme.FONT_BODY);
        subjectCountLabel.setForeground(UITheme.TEXT_SECONDARY);

        filterBar.add(subjectCountLabel);
        filterBar.add(professionCombo);
        filterBar.add(filterLbl);

        headerPanel.add(topHeader, BorderLayout.NORTH);
        headerPanel.add(filterBar, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // ─── Card Grid ────────────────────────────────────────────────────────
        gridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(gridWrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BG_LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        loadSubjects();
    }

    private void showAddSubjectDialog() {
        String sel = (String) professionCombo.getSelectedItem();
        if (sel == null || sel.trim().isEmpty()) {
            warn("يرجى اختيار مهنة أولاً من القائمة المنسدلة.");
            return;
        }

        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));
        p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JTextField nameF = new JTextField();
        nameF.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JComboBox<String> typeCombo = new JComboBox<>(new String[] { "نظري", "عملي", "تطبيقي" });
        typeCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField maxF = new JTextField("100");
        JTextField passF = new JTextField("50");
        JTextField orderF = new JTextField("1"); // Display Order Default

        p.add(new JLabel("اسم المادة:", SwingConstants.RIGHT));
        p.add(nameF);
        p.add(new JLabel("نوع المادة:", SwingConstants.RIGHT));
        p.add(typeCombo);
        p.add(new JLabel("الدرجة العظمى:", SwingConstants.RIGHT));
        p.add(maxF);
        p.add(new JLabel("درجة النجاح:", SwingConstants.RIGHT));
        p.add(passF);
        p.add(new JLabel("ترتيب العرض (1, 2, 3..):", SwingConstants.RIGHT));
        p.add(orderF);

        if (JOptionPane.showConfirmDialog(this, p, "إضافة مادة جديدة",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String name = nameF.getText().trim();
                String type = (String) typeCombo.getSelectedItem();
                int mx = Integer.parseInt(maxF.getText().trim());
                int ps = Integer.parseInt(passF.getText().trim());
                int displayOrder = 1;
                try {
                    displayOrder = Integer.parseInt(orderF.getText().trim());
                } catch (NumberFormatException ignored) {}

                if (name.isEmpty()) {
                    warn("اسم المادة لا يمكن أن يكون فارغاً.");
                    return;
                }
                if (ps > mx) {
                    warn("درجة النجاح لا يمكن أن تتجاوز الدرجة العظمى.");
                    return;
                }
                SubjectService.addSubject(sel, name, type, ps, mx, displayOrder);
                loadSubjects();
            } catch (NumberFormatException ex) {
                warn("يرجى إدخال أرقام صحيحة للدرجات.");
            }
        }
    }

    private void loadProfessionsIntoCombo() {
        professionCombo.removeAllItems();
        // Load from both students table (for imports) and Dictionary (for settings)
        List<String> dictProfs = StudentService.getDistinctProfessions();
        
        // Manual query for students list professions
        List<String> studentProfs = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT DISTINCT profession FROM students WHERE profession IS NOT NULL ORDER BY profession");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String p = rs.getString(1);
                if (p != null && !studentProfs.contains(p.trim())) studentProfs.add(p.trim());
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Combine unique
        java.util.Set<String> all = new java.util.TreeSet<>();
        if (dictProfs != null) { for (String s : dictProfs) if (s != null) all.add(s.trim()); }
        for (String s : studentProfs) all.add(s);

        for (String p : all) {
            professionCombo.addItem(p);
        }
    }

    private void loadSubjects() {
        gridPanel.removeAll();
        String sel = (String) professionCombo.getSelectedItem();
        if (sel == null || sel.trim().isEmpty()) {
            subjectCountLabel.setText("");
            gridPanel.revalidate();
            gridPanel.repaint();
            return;
        }
        currentSubjectsList = SubjectService.getSubjectsByProfession(sel);
        for (Subject sub : currentSubjectsList) {
            gridPanel.add(buildSubjectCard(sub));
        }
        subjectCountLabel.setText(currentSubjectsList.size() + " مادة مسجلة  ");
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildSubjectCard(Subject subject) {
        String typeStr = subject.getType() != null ? subject.getType() : "نظري";
        boolean isPractical = typeStr.equals("عملي");
        boolean isApplied   = typeStr.equals("تطبيقي");
        Color typeColor = isApplied ? new Color(0xEA580C)
                        : isPractical ? new Color(0x7C3AED)
                        : new Color(0x1D4ED8);
        Color typeBg    = isApplied ? new Color(0xFEF3C7)
                        : isPractical ? new Color(0xEDE9FE)
                        : new Color(0xDBEAFE);

        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Accent bar
                g2.setColor(typeColor);
                g2.fillRoundRect(0, 0, getWidth(), 6, 6, 6);
                // Body
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 3, getWidth(), getHeight() - 3);
                // Border
                g2.setColor(UITheme.BORDER);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        // Removed fixed preferredSize here so the card's height is determined by its layout naturally.
        card.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 5, 0.05f, 15, UITheme.BG_LIGHT),
                new EmptyBorder(16, 16, 14, 16)));

        // ── Type badge ────────────────────────────────────────────────────────
        JLabel typeBadge = new JLabel("  " + typeStr + "  ", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(typeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        typeBadge.setOpaque(false);
        typeBadge.setForeground(typeColor);
        typeBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        typeBadge.setBorder(new EmptyBorder(3, 6, 3, 6));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(typeBadge, BorderLayout.WEST);
        card.add(topRow, BorderLayout.NORTH);

        // ── Subject Name ──────────────────────────────────────────────────────
        JLabel nameLabel = new JLabel(
            "<html><div dir='rtl' style='text-align:center; line-height:1.4'>" + subject.getName() + "</div></html>",
            SwingConstants.CENTER);
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);
        nameLabel.setBorder(new EmptyBorder(6, 4, 6, 4));
        card.add(nameLabel, BorderLayout.CENTER);

        // ── Marks Row ─────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setOpaque(false);

        JPanel marksRow = new JPanel(new GridLayout(1, 2, 8, 0));
        marksRow.setOpaque(false);
        marksRow.add(makeMarkChip("الدرجة العظمى", String.valueOf(subject.getMaxMark()),
                new Color(0xDBEAFE), new Color(0x1D4ED8)));
        marksRow.add(makeMarkChip("درجة النجاح", String.valueOf(subject.getPassMark()),
                new Color(0xDCFCE7), new Color(0x15803D)));
        footer.add(marksRow, BorderLayout.NORTH);

        // Delete Button
        JButton btnDel = new JButton("حذف المادة");
        btnDel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDel.setForeground(UITheme.DANGER);
        btnDel.setBackground(new Color(0xFEF2F2));
        btnDel.setBorderPainted(false);
        btnDel.setFocusPainted(false);
        btnDel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDel.putClientProperty("JButton.buttonType", "roundRect");
        btnDel.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this,
                    "حذف «" + subject.getName() + "»؟\nسيتم مسح درجات الطلاب المرتبطة.",
                    "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                SubjectService.deleteSubject(subject.getId());
                loadSubjects();
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnDel);
        footer.add(btnPanel, BorderLayout.SOUTH);

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel makeMarkChip(String label, String value, Color bg, Color fg) {
        JPanel chip = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setBorder(new EmptyBorder(4, 8, 4, 8));

        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valLbl.setForeground(fg);

        JLabel txtLbl = new JLabel(label, SwingConstants.CENTER);
        txtLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        txtLbl.setForeground(fg);

        chip.add(valLbl, BorderLayout.CENTER);
        chip.add(txtLbl, BorderLayout.SOUTH);
        return chip;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "تنبيه", JOptionPane.WARNING_MESSAGE);
    }
}
