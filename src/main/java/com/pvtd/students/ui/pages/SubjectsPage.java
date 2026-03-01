package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Specialization;
import com.pvtd.students.models.Subject;
import com.pvtd.students.services.SpecializationService;
import com.pvtd.students.services.SubjectService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.ui.utils.DropShadowBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * صفحة المواد الدراسية — تعرض كل مادة في كرد منفصلة
 */
public class SubjectsPage extends JPanel {

    private JPanel gridPanel;
    private List<Subject> currentSubjectsList;
    private JComboBox<SpecializationItem> specCombo;
    private JLabel subjectCountLabel;

    private static class SpecializationItem {
        int id;
        String name;

        SpecializationItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

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

        JLabel filterLbl = new JLabel("⎇  اختر التخصص:");
        filterLbl.setFont(UITheme.FONT_HEADER);
        filterLbl.setForeground(UITheme.TEXT_PRIMARY);

        specCombo = new JComboBox<>();
        specCombo.setFont(UITheme.FONT_BODY);
        specCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        specCombo.setPreferredSize(new Dimension(280, 38));
        loadSpecializationsIntoCombo();
        specCombo.addActionListener(e -> loadSubjects());

        subjectCountLabel = new JLabel("", SwingConstants.LEFT);
        subjectCountLabel.setFont(UITheme.FONT_BODY);
        subjectCountLabel.setForeground(UITheme.TEXT_SECONDARY);

        filterBar.add(subjectCountLabel);
        filterBar.add(specCombo);
        filterBar.add(filterLbl);

        headerPanel.add(topHeader, BorderLayout.NORTH);
        headerPanel.add(filterBar, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // ─── Card Grid ────────────────────────────────────────────────────────
        gridPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 16));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(4, 0, 4, 0));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BG_LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        loadSubjects();
    }

    private void showAddSubjectDialog() {
        SpecializationItem sel = (SpecializationItem) specCombo.getSelectedItem();
        if (sel == null) {
            warn("يرجى اختيار تخصص أولاً من القائمة المنسدلة.");
            return;
        }

        JPanel p = new JPanel(new GridLayout(4, 2, 10, 10));
        p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JTextField nameF = new JTextField();
        nameF.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JComboBox<String> typeCombo = new JComboBox<>(new String[] { "نظري", "عملي" });
        typeCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JTextField maxF = new JTextField("100");
        JTextField passF = new JTextField("50");

        p.add(new JLabel("اسم المادة:", SwingConstants.RIGHT));
        p.add(nameF);
        p.add(new JLabel("نوع المادة:", SwingConstants.RIGHT));
        p.add(typeCombo);
        p.add(new JLabel("الدرجة العظمى:", SwingConstants.RIGHT));
        p.add(maxF);
        p.add(new JLabel("درجة النجاح:", SwingConstants.RIGHT));
        p.add(passF);

        if (JOptionPane.showConfirmDialog(this, p, "إضافة مادة جديدة",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String name = nameF.getText().trim();
                String type = (String) typeCombo.getSelectedItem();
                int mx = Integer.parseInt(maxF.getText().trim());
                int ps = Integer.parseInt(passF.getText().trim());
                if (name.isEmpty()) {
                    warn("اسم المادة لا يمكن أن يكون فارغاً.");
                    return;
                }
                if (ps > mx) {
                    warn("درجة النجاح لا يمكن أن تتجاوز الدرجة العظمى.");
                    return;
                }
                SubjectService.addSubject(sel.id, name, type, ps, mx);
                loadSubjects();
            } catch (NumberFormatException ex) {
                warn("يرجى إدخال أرقام صحيحة للدرجات.");
            }
        }
    }

    private void loadSpecializationsIntoCombo() {
        specCombo.removeAllItems();
        List<Specialization> specs = SpecializationService.getSpecializationsByDepartment(
                SpecializationService.getDefaultDepartmentId());
        for (Specialization sp : specs) {
            specCombo.addItem(new SpecializationItem(sp.getId(), sp.getName()));
        }
    }

    private void loadSubjects() {
        gridPanel.removeAll();
        SpecializationItem sel = (SpecializationItem) specCombo.getSelectedItem();
        if (sel == null) {
            subjectCountLabel.setText("");
            gridPanel.revalidate();
            gridPanel.repaint();
            return;
        }
        currentSubjectsList = SubjectService.getSubjectsBySpecialization(sel.id);
        for (Subject sub : currentSubjectsList) {
            gridPanel.add(buildSubjectCard(sub));
        }
        subjectCountLabel.setText(currentSubjectsList.size() + " مادة مسجلة  ");
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildSubjectCard(Subject subject) {
        String typeStr = subject.getType() != null ? subject.getType() : "نظري";
        boolean isPractical = typeStr.contains("عمل");
        Color typeColor = isPractical ? new Color(0x7C3AED) : new Color(0x1D4ED8);
        Color typeBg = isPractical ? new Color(0xEDE9FE) : new Color(0xDBEAFE);

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
        card.setPreferredSize(new Dimension(260, 185));
        card.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 4, 0.06f, 14, UITheme.BG_LIGHT),
                new EmptyBorder(18, 18, 14, 18)));

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
                "<html><div style='text-align:right;'>" + subject.getName() + "</div></html>",
                SwingConstants.RIGHT);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);
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
