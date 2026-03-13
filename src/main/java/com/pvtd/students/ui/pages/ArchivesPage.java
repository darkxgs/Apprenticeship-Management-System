package com.pvtd.students.ui.pages;

import com.pvtd.students.services.ArchiveService;
import com.pvtd.students.services.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Archives Page — Allows admin to:
 *   1. Create a new archive group (named year-end snapshot)
 *   2. Archive all students (or by center) into the group
 *   3. Browse & view archived students from past groups
 *   4. Delete old archive groups
 */
public class ArchivesPage extends JPanel {

    private DefaultTableModel groupsModel;
    private JTable groupsTable;
    private DefaultTableModel studentsModel;
    private JTextArea logArea;

    public ArchivesPage() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(0xF1F5F9));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    // ─── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x0A192F));
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("أرشفة الطلاب", SwingConstants.RIGHT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.EAST);

        JLabel subtitle = new JLabel("أنشئ مجموعة أرشيف وانقل طلاب العام المنتهي إليها", SwingConstants.RIGHT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(0x94A3B8));
        header.add(subtitle, BorderLayout.CENTER);

        return header;
    }

    // ─── Body ─────────────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new GridLayout(1, 2, 20, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        body.add(buildLeftPanel());   // Archive Groups list
        body.add(buildRightPanel());  // Students in selected group

        return body;
    }

    // ─── Left: Archive Groups ─────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // --- Create Group Form ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE2E8F0), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel formTitle = new JLabel("إنشاء مجموعة أرشيف جديدة");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setAlignmentX(Component.RIGHT_ALIGNMENT);
        formTitle.setHorizontalAlignment(SwingConstants.RIGHT);

        JTextField nameField = createRtlField("اسم المجموعة (مثال: دفعة 2025)");
        JTextField descField = createRtlField("وصف اختياري");

        JLabel centerLabel = new JLabel("اختر المركز للأرشفة:");
        centerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        centerLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JComboBox<String> centerCombo = new JComboBox<>();
        centerCombo.addItem("الكل (جميع المراكز)");
        for (String c : StudentService.getDistinctCenters()) {
            centerCombo.addItem(c);
        }
        centerCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        centerCombo.setAlignmentX(Component.RIGHT_ALIGNMENT);
        centerCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JTextField noteField = createRtlField("ملاحظة للأرشيف (اختياري)");

        JButton btnCreate = new JButton("إنشاء المجموعة والأرشفة");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCreate.setBackground(new Color(0x0A192F));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setFocusPainted(false);
        btnCreate.setBorderPainted(false);
        btnCreate.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnCreate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnCreate.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnCreate.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "يرجى إدخال اسم المجموعة أولاً");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                "هل أنت متأكد من أرشفة الطلاب؟\nهذا سيحذفهم من قائمة الطلاب الحالية.",
                "تأكيد الأرشفة",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            String desc = descField.getText().trim();
            boolean ok = ArchiveService.createArchiveGroup(name, desc);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "فشل إنشاء المجموعة.");
                return;
            }

            // Get ID of the newly created group
            List<String[]> groups = ArchiveService.getArchiveGroups();
            if (groups.isEmpty()) {
                JOptionPane.showMessageDialog(this, "فشل استرجاع المجموعة الجديدة.");
                return;
            }
            int groupId = Integer.parseInt(groups.get(0)[0]);

            String selectedCenter = centerCombo.getSelectedIndex() == 0 ? "" : (String) centerCombo.getSelectedItem();
            String note = noteField.getText().trim();

            int archived = ArchiveService.archiveStudents(groupId, selectedCenter, note);
            if (archived < 0) {
                JOptionPane.showMessageDialog(this, "حدث خطأ أثناء الأرشفة.");
            } else {
                logArea.append("✓ تمت أرشفة " + archived + " طالب في مجموعة: " + name + "\n");
                nameField.setText("");
                descField.setText("");
                noteField.setText("");
                refreshGroups();
                JOptionPane.showMessageDialog(this, "تمت الأرشفة بنجاح!\n" + archived + " طالب تم نقلهم.");
            }
        });

        formPanel.add(formTitle);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(descField);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(centerLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(centerCombo);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(noteField);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(btnCreate);

        // --- Groups Table ---
        groupsModel = new DefaultTableModel(new String[]{"ID", "اسم المجموعة", "الوصف", "تاريخ الإنشاء"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        groupsTable = new JTable(groupsModel);
        groupsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        groupsTable.setRowHeight(32);
        groupsTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        groupsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupsTable.getColumnModel().getColumn(0).setMinWidth(0);
        groupsTable.getColumnModel().getColumn(0).setMaxWidth(0); // hide ID column

        groupsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = groupsTable.getSelectedRow();
                if (row >= 0) {
                    int groupId = Integer.parseInt(groupsModel.getValueAt(row, 0).toString());
                    loadStudentsForGroup(groupId);
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(groupsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));

        // Delete button
        JButton btnDelete = new JButton("حذف المجموعة المحددة");
        btnDelete.setForeground(Color.RED);
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDelete.setContentAreaFilled(false);
        btnDelete.setBorderPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> {
            int row = groupsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "اختر مجموعة أولاً");
                return;
            }
            int groupId = Integer.parseInt(groupsModel.getValueAt(row, 0).toString());
            String groupName = groupsModel.getValueAt(row, 1).toString();
            int c = JOptionPane.showConfirmDialog(this,
                "هل تريد حذف مجموعة \"" + groupName + "\" وكل الطلاب المؤرشفين فيها؟\nهذا الإجراء لا يمكن التراجع عنه.",
                "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c == JOptionPane.YES_OPTION) {
                ArchiveService.deleteArchiveGroup(groupId);
                refreshGroups();
                studentsModel.setRowCount(0);
            }
        });

        JPanel groupsSection = new JPanel(new BorderLayout(0, 10));
        groupsSection.setOpaque(false);
        JLabel groupsTitle = new JLabel("مجموعات الأرشيف الموجودة", SwingConstants.RIGHT);
        groupsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        groupsSection.add(groupsTitle, BorderLayout.NORTH);
        groupsSection.add(tableScroll, BorderLayout.CENTER);
        groupsSection.add(btnDelete, BorderLayout.SOUTH);

        // Log area
        logArea = new JTextArea(4, 0);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(0xF8FAFC));
        logArea.setForeground(new Color(0x334155));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("سجل العمليات"));

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(groupsSection, BorderLayout.CENTER);
        panel.add(logScroll, BorderLayout.SOUTH);

        refreshGroups();
        return panel;
    }

    // ─── Right: Students in selected archive group ────────────────────────────
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel title = new JLabel("طلاب المجموعة المحددة", SwingConstants.RIGHT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        studentsModel = new DefaultTableModel(
            new String[]{"الاسم", "الرقم السري", "المركز", "المهنة", "الحالة", "الملاحظة", "تاريخ الأرشفة"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable studentsTable = new JTable(studentsModel);
        studentsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentsTable.setRowHeight(30);
        studentsTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JScrollPane scroll = new JScrollPane(studentsTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private void refreshGroups() {
        groupsModel.setRowCount(0);
        for (String[] row : ArchiveService.getArchiveGroups()) {
            groupsModel.addRow(row);
        }
    }

    private void loadStudentsForGroup(int groupId) {
        studentsModel.setRowCount(0);
        for (String[] row : ArchiveService.getArchivedStudents(groupId)) {
            studentsModel.addRow(row);
        }
    }

    private JTextField createRtlField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        tf.setHorizontalAlignment(JTextField.RIGHT);
        tf.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        return tf;
    }
}
