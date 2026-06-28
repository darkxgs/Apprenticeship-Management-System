package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Subject;
import com.pvtd.students.services.SubjectService;
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
import java.util.List;
import java.util.ArrayList;

/**
 * صفحة المواد الدراسية — تعرض كل مادة في كرد منفصلة
 */
public class SubjectsPage extends JPanel {

    private JPanel gridPanel;
    private List<Subject> currentSubjectsList;
    private JComboBox<String> profGroupCombo;
    private JComboBox<String> professionCombo;
    private JLabel subjectCountLabel;

    public SubjectsPage(AppFrame frame) {
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(28, 28, 28, 28));
        setBackground(UITheme.BG_LIGHT);

        // 1. Initialize gridPanel first to avoid NullPointer in loadSubjects
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, 4, 16, 16));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // 2. Build Header and UI
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

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        filterBar.setBackground(UITheme.CARD_BG);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 3, 0.04f, 10, UITheme.BG_LIGHT),
                new EmptyBorder(0, 8, 0, 8)));

        JLabel filterGroupLbl = new JLabel("⎇  اختر المجموعة المهنيه:");
        filterGroupLbl.setFont(UITheme.FONT_HEADER);
        filterGroupLbl.setForeground(UITheme.TEXT_PRIMARY);

        profGroupCombo = new JComboBox<>();
        profGroupCombo.setFont(UITheme.FONT_BODY);
        profGroupCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        profGroupCombo.setPreferredSize(new Dimension(280, 38));
        loadProfGroupsIntoCombo();

        JLabel filterProfLbl = new JLabel("⎇  اختر المهنة:");
        filterProfLbl.setFont(UITheme.FONT_HEADER);
        filterProfLbl.setForeground(UITheme.TEXT_PRIMARY);

        professionCombo = new JComboBox<>();
        professionCombo.setFont(UITheme.FONT_BODY);
        professionCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        professionCombo.setPreferredSize(new Dimension(280, 38));

        profGroupCombo.addActionListener(e -> loadProfessionsIntoCombo());
        professionCombo.addActionListener(e -> loadSubjects());

        subjectCountLabel = new JLabel("", SwingConstants.LEFT);
        subjectCountLabel.setFont(UITheme.FONT_BODY);
        subjectCountLabel.setForeground(UITheme.TEXT_SECONDARY);

        filterBar.add(subjectCountLabel);
        filterBar.add(professionCombo);
        filterBar.add(filterProfLbl);
        filterBar.add(profGroupCombo);
        filterBar.add(filterGroupLbl);

        // Preload first profession list
        if (profGroupCombo.getItemCount() > 0) {
            profGroupCombo.setSelectedIndex(0);
        }

        headerPanel.add(topHeader, BorderLayout.NORTH);
        headerPanel.add(filterBar, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // 3. Setup Scroll Pane with Grid
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(gridWrapper,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BG_LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        scroll.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int availableWidth = scroll.getViewport().getWidth();
                int cardWidth = 240 + 20;
                int cols = Math.max(1, availableWidth / cardWidth);
                if (gridPanel.getLayout() instanceof GridLayout) {
                    ((GridLayout) gridPanel.getLayout()).setColumns(cols);
                    gridPanel.revalidate();
                }
            }
        });

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
        JTextField orderF = new JTextField("1");

        p.add(new JLabel("اسم المادة:", SwingConstants.RIGHT));
        p.add(nameF);
        p.add(new JLabel("نوع المادة:", SwingConstants.RIGHT));
        p.add(typeCombo);
        p.add(new JLabel("الدرجة العظمى:", SwingConstants.RIGHT));
        p.add(maxF);
        p.add(new JLabel("درجة النجاح:", SwingConstants.RIGHT));
        p.add(passF);
        p.add(new JLabel("ترتيب العرض:", SwingConstants.RIGHT));
        p.add(orderF);

        if (JOptionPane.showConfirmDialog(this, p, "إضافة مادة جديدة",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String name = nameF.getText().trim();
                String type = (String) typeCombo.getSelectedItem();
                int mx = Integer.parseInt(maxF.getText().trim());
                int ps = Integer.parseInt(passF.getText().trim());
                int displayOrder = 1;
                try { displayOrder = Integer.parseInt(orderF.getText().trim()); } catch (Exception ignored) {}

                if (name.isEmpty()) {
                    warn("اسم المادة لا يمكن أن يكون فارغاً.");
                    return;
                }
                
                // Validate limits
                if (currentSubjectsList != null) {
                    if (currentSubjectsList.size() >= 6) {
                        warn("الحد الأقصى 6 مواد أساسية.");
                        return;
                    }
                }
                
                SubjectService.addSubject(sel, name, type, ps, mx, displayOrder);
                loadSubjects();
            } catch (NumberFormatException ex) {
                warn("يرجى إدخال أرقام صحيحة للدرجات.");
            }
        }
    }

    private void loadProfGroupsIntoCombo() {
        profGroupCombo.removeAllItems();
        java.util.Set<String> groups = new java.util.TreeSet<>();
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT name FROM professional_groups ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String pg = rs.getString(1);
                if (pg != null && !pg.trim().isEmpty()) groups.add(pg.trim());
            }
        } catch (Exception e) {}

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT DISTINCT professional_group FROM students WHERE professional_group IS NOT NULL");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String pg = rs.getString(1);
                if (pg != null && !pg.trim().isEmpty()) groups.add(pg.trim());
            }
        } catch (Exception e) {}

        for (String g : groups) {
            profGroupCombo.addItem(g);
        }
    }

    private void loadProfessionsIntoCombo() {
        professionCombo.removeAllItems();
        String selGroup = (String) profGroupCombo.getSelectedItem();
        if (selGroup == null || selGroup.trim().isEmpty()) return;

        java.util.Set<String> all = new java.util.TreeSet<>();

        // Load from DB relation
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT p.name FROM professions p " +
                 "JOIN professional_groups pg ON p.professional_group_id = pg.id " +
                 "WHERE pg.name = ? ORDER BY p.name")) {
            ps.setString(1, selGroup);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String p = rs.getString(1);
                    if (p != null && !p.trim().isEmpty()) all.add(p.trim());
                }
            }
        } catch (Exception e) {}

        // Load from students table (historical data)
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT DISTINCT profession FROM students WHERE professional_group = ? AND profession IS NOT NULL")) {
            ps.setString(1, selGroup);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String p = rs.getString(1);
                    if (p != null && !p.trim().isEmpty()) all.add(p.trim());
                }
            }
        } catch (Exception e) {}

        for (String p : all) {
            professionCombo.addItem(p);
        }
        
        if (professionCombo.getItemCount() > 0) {
            professionCombo.setSelectedIndex(0);
        } else {
            loadSubjects(); // trigger clear
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
        if (currentSubjectsList.isEmpty()) {
            SubjectService.autoGenerateStandardSubjects(sel);
            currentSubjectsList = SubjectService.getSubjectsByProfession(sel);
        }

        // Only render top-level subjects (parentSubjectId == null) in the grid
        java.util.Set<Integer> childIds = new java.util.HashSet<>();
        for (Subject s : currentSubjectsList) {
            if (s.getParentSubjectId() != null) childIds.add(s.getId());
        }

        int topLevelCount = 0;
        for (Subject sub : currentSubjectsList) {
            if (childIds.contains(sub.getId())) continue; // skip children
            java.util.List<Subject> children = SubjectService.getChildrenOf(sub.getId());
            gridPanel.add(buildSubjectCard(sub, children));
            topLevelCount++;
        }
        subjectCountLabel.setText(topLevelCount + " مادة مسجلة  ");
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildSubjectCard(Subject subject, List<Subject> children) {
        // Always show as regular card, composite info is in edit dialog only
        return buildSubjectCard(subject);
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
        // Fixed size to prevent stretching and maintain grid alignment
        card.setPreferredSize(new Dimension(240, 220));
        card.setMinimumSize(new Dimension(240, 220));
        card.setMaximumSize(new Dimension(240, 220));
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
        // Use JTextArea for subject name ( JLabel HTML breaks Arabic words)
        JTextArea nameLabel = new JTextArea(subject.getName());
        nameLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);
        nameLabel.setBackground(Color.WHITE);
        nameLabel.setOpaque(false);
        nameLabel.setEditable(false);
        nameLabel.setFocusable(false);
        nameLabel.setLineWrap(true);
        nameLabel.setWrapStyleWord(true);
        nameLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
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

        // Edit Button
        JButton btnEdit = new JButton("تعديل ✎");
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEdit.setForeground(new Color(0x1E3A8A)); // Dark Blue
        btnEdit.setBackground(new Color(0xE0F2FE)); // Light Blue
        btnEdit.setBorderPainted(false);
        btnEdit.setFocusPainted(false);
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.putClientProperty("JButton.buttonType", "roundRect");
        btnEdit.addActionListener(e -> showEditSubjectDialog(subject, (String) professionCombo.getSelectedItem()));

        // Delete Button
        JButton btnDel = new JButton("حذف");
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

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(btnDel);
        btnPanel.add(btnEdit);
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

    private void showEditSubjectDialog(Subject subject, String selProfession) {
        // Check if this subject has children (composite)
        List<Subject> children = SubjectService.getChildrenOf(subject.getId());
        boolean isComposite = !children.isEmpty();
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        // Basic fields panel
        JPanel basicPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        basicPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        JTextField nameF = new JTextField(subject.getName());
        nameF.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JComboBox<String> typeCombo = new JComboBox<>(new String[] { "نظري", "عملي", "تطبيقي" });
        typeCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        typeCombo.setSelectedItem(subject.getType() != null ? subject.getType() : "نظري");

        JTextField maxF = new JTextField(String.valueOf(subject.getMaxMark()));
        JTextField passF = new JTextField(String.valueOf(subject.getPassMark()));
        JTextField orderF = new JTextField(String.valueOf(subject.getDisplayOrder()));

        basicPanel.add(new JLabel("اسم المادة:", SwingConstants.RIGHT));
        basicPanel.add(nameF);
        basicPanel.add(new JLabel("نوع المادة:", SwingConstants.RIGHT));
        basicPanel.add(typeCombo);
        basicPanel.add(new JLabel("الدرجة العظمى:", SwingConstants.RIGHT));
        basicPanel.add(maxF);
        basicPanel.add(new JLabel("درجة النجاح:", SwingConstants.RIGHT));
        basicPanel.add(passF);
        basicPanel.add(new JLabel("ترتيب العرض:", SwingConstants.RIGHT));
        basicPanel.add(orderF);
        
        mainPanel.add(basicPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Composite checkbox and fields
        JCheckBox compositeCheck = new JCheckBox("تقسيم المادة إلى جزئين (30/70)", isComposite);
        compositeCheck.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        compositeCheck.setFont(UITheme.FONT_BODY);
        
        JPanel compositePanel = new JPanel(new GridLayout(4, 2, 10, 10));
        compositePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        compositePanel.setBorder(BorderFactory.createTitledBorder("تفاصيل التقسيم"));
        
        JTextField subName1F = new JTextField(isComposite && children.size() > 0 ? children.get(0).getSubName() : "نظري");
        subName1F.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JTextField mark1F = new JTextField(isComposite && children.size() > 0 ? String.valueOf(children.get(0).getMaxMark()) : "30");
        
        JTextField subName2F = new JTextField(isComposite && children.size() > 1 ? children.get(1).getSubName() : "تحريري");
        subName2F.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JTextField mark2F = new JTextField(isComposite && children.size() > 1 ? String.valueOf(children.get(1).getMaxMark()) : "70");
        
        compositePanel.add(new JLabel("اسم الجزء الأول:", SwingConstants.RIGHT));
        compositePanel.add(subName1F);
        compositePanel.add(new JLabel("درجة الجزء الأول:", SwingConstants.RIGHT));
        compositePanel.add(mark1F);
        compositePanel.add(new JLabel("اسم الجزء الثاني:", SwingConstants.RIGHT));
        compositePanel.add(subName2F);
        compositePanel.add(new JLabel("درجة الجزء الثاني:", SwingConstants.RIGHT));
        compositePanel.add(mark2F);
        
        compositePanel.setVisible(isComposite);
        
        compositeCheck.addActionListener(e -> {
            compositePanel.setVisible(compositeCheck.isSelected());
            mainPanel.revalidate();
            mainPanel.repaint();
            // Resize dialog
            Window window = SwingUtilities.getWindowAncestor(mainPanel);
            if (window != null) window.pack();
        });
        
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        checkPanel.add(compositeCheck);
        mainPanel.add(checkPanel);
        mainPanel.add(compositePanel);

        if (JOptionPane.showConfirmDialog(this, mainPanel, "تعديل مادة", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String name = nameF.getText().trim();
                String type = (String) typeCombo.getSelectedItem();
                int mx = Integer.parseInt(maxF.getText().trim());
                int ps = Integer.parseInt(passF.getText().trim());
                int displayOrder = subject.getDisplayOrder();
                try { displayOrder = Integer.parseInt(orderF.getText().trim()); } catch (NumberFormatException ignored) {}

                if (name.isEmpty()) { warn("اسم المادة لا يمكن أن يكون فارغاً."); return; }
                
                // Update main subject
                SubjectService.updateSubject(subject.getId(), name, type, ps, mx, displayOrder, null, null);
                
                // Handle composite
                if (compositeCheck.isSelected()) {
                    String sn1 = subName1F.getText().trim();
                    String sn2 = subName2F.getText().trim();
                    int m1 = Integer.parseInt(mark1F.getText().trim());
                    int m2 = Integer.parseInt(mark2F.getText().trim());
                    
                    if (sn1.isEmpty() || sn2.isEmpty()) {
                        warn("يرجى إدخال أسماء للجزئين.");
                        return;
                    }
                    
                    // Remove old children and create new ones
                    SubjectService.disableComposite(subject.getId());
                    SubjectService.addSubject(selProfession, name, type, (int)(m1 * 0.5), m1, 1, subject.getId(), sn1);
                    SubjectService.addSubject(selProfession, name, type, (int)(m2 * 0.5), m2, 2, subject.getId(), sn2);
                } else {
                    // Remove composite if unchecked
                    SubjectService.disableComposite(subject.getId());
                }
                
                loadSubjects();
            } catch (NumberFormatException ex) {
                warn("يرجى إدخال أرقام صحيحة للدرجات.");
            }
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "تنبيه", JOptionPane.WARNING_MESSAGE);
    }
}
