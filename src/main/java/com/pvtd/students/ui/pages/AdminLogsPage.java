package com.pvtd.students.ui.pages;

import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.services.LogService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class AdminLogsPage extends JPanel {

    private JTable logsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> actionFilterCombo;
    private JLabel countLabel;

    private static final String[] COLUMNS = {"#", "المستخدم", "الإجراء", "التفاصيل", "التاريخ والوقت"};

    private AppFrame frame;

    public AdminLogsPage(AppFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_LIGHT);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadLogs();
    }

    // ── Header ───────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(new Color(0x0A192F));
        header.setBorder(new EmptyBorder(18, 30, 18, 30));

        // Title
        JLabel title = new JLabel("سجل نشاط النظام", SwingConstants.RIGHT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.EAST);

        // Controls (search + filter + clear)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controls.setOpaque(false);

        // Search box
        searchField = new JTextField(18);
        searchField.setFont(UITheme.FONT_BODY);
        searchField.putClientProperty("JTextField.placeholderText", "بحث...");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        // Action filter combo
        actionFilterCombo = new JComboBox<>(new String[]{
            "كل الإجراءات", "LOGIN", "LOGOUT", "ADD_STUDENT", "EDIT_STUDENT",
            "DELETE_STUDENT", "EXCEL_IMPORT", "SAVE_GRADES", "CLEAR_LOGS"
        });
        actionFilterCombo.setFont(UITheme.FONT_BODY);
        actionFilterCombo.addActionListener(e -> applyFilter());

        // Refresh
        JButton refreshBtn = new JButton("🔄 تحديث");
        refreshBtn.setFont(UITheme.FONT_BODY);
        refreshBtn.setBackground(new Color(0x1E40AF));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.putClientProperty("JButton.buttonType", "roundRect");
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadLogs());

        // Clear all
        JButton clearBtn = new JButton("🗑 مسح السجلات");
        clearBtn.setFont(UITheme.FONT_BODY);
        clearBtn.setBackground(new Color(0xDC2626));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setFocusPainted(false);
        clearBtn.putClientProperty("JButton.buttonType", "roundRect");
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> handleClearLogs());

        // Count label
        countLabel = new JLabel("", SwingConstants.LEFT);
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        countLabel.setForeground(new Color(0x93C5FD));

        controls.add(searchField);
        controls.add(actionFilterCombo);
        controls.add(refreshBtn);
        controls.add(clearBtn);
        controls.add(countLabel);
        header.add(controls, BorderLayout.WEST);

        return header;
    }

    // ── Table Panel ──────────────────────────────────────────────────────────

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        logsTable = new JTable(tableModel);
        logsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        logsTable.setRowHeight(38);
        logsTable.setShowGrid(false);
        logsTable.setIntercellSpacing(new Dimension(0, 0));
        logsTable.setFillsViewportHeight(true);
        logsTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        logsTable.setSelectionBackground(new Color(0xDBEAFE));
        logsTable.setSelectionForeground(new Color(0x1E3A8A));
        logsTable.setBackground(Color.WHITE);
        logsTable.setForeground(new Color(0x1F2937));

        // Header styling
        JTableHeader th = logsTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 14));
        th.setBackground(new Color(0x0A192F));
        th.setForeground(Color.WHITE);
        th.setReorderingAllowed(false);
        th.setBorder(BorderFactory.createEmptyBorder());
        th.setPreferredSize(new Dimension(0, 44));

        // Column widths
        TableColumnModel cm = logsTable.getColumnModel();
        cm.getColumn(0).setPreferredWidth(50);
        cm.getColumn(0).setMaxWidth(60);
        cm.getColumn(1).setPreferredWidth(120);
        cm.getColumn(2).setPreferredWidth(160);
        cm.getColumn(3).setPreferredWidth(500);
        cm.getColumn(4).setPreferredWidth(180);

        // Alternating row renderer with action color-coding
        logsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                }
                setHorizontalAlignment(col == 0 ? CENTER : RIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

                if (col == 2 && val != null) {
                    String action = val.toString();
                    if (!sel) {
                        if (action.contains("DELETE") || action.contains("CLEAR")) {
                            c.setForeground(new Color(0xDC2626));
                        } else if (action.contains("LOGIN") || action.contains("IMPORT")) {
                            c.setForeground(new Color(0x0891B2));
                        } else if (action.contains("ADD") || action.contains("SAVE")) {
                            c.setForeground(new Color(0x16A34A));
                        } else {
                            c.setForeground(new Color(0x1F2937));
                        }
                    }
                } else if (!sel) {
                    c.setForeground(new Color(0x1F2937));
                }
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(logsTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    // ── Data Loading ─────────────────────────────────────────────────────────

    private void loadLogs() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<List<LogService.LogEntry>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<LogService.LogEntry> doInBackground() {
                return LogService.getAllLogs();
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    List<LogService.LogEntry> logs = get();
                    tableModel.setRowCount(0);
                    int i = 1;
                    for (LogService.LogEntry log : logs) {
                        tableModel.addRow(new Object[]{
                            i++,
                            log.username,
                            log.action,
                            log.details,
                            log.timestamp
                        });
                    }
                    updateCountLabel(tableModel.getRowCount());
                    applyFilter();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminLogsPage.this,
                        "فشل تحميل السجلات: " + ex.getMessage(), "خطأ",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void applyFilter() {
        String search = searchField.getText().trim().toLowerCase();
        String actionFilter = (String) actionFilterCombo.getSelectedItem();
        boolean allActions = "كل الإجراءات".equals(actionFilter);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        logsTable.setRowSorter(sorter);

        RowFilter<DefaultTableModel, Integer> rf = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String username = entry.getStringValue(1).toLowerCase();
                String action   = entry.getStringValue(2);
                String details  = entry.getStringValue(3).toLowerCase();
                String ts       = entry.getStringValue(4).toLowerCase();

                boolean matchesSearch = search.isEmpty()
                    || username.contains(search)
                    || action.toLowerCase().contains(search)
                    || details.contains(search)
                    || ts.contains(search);

                boolean matchesAction = allActions || action.equalsIgnoreCase(actionFilter);

                return matchesSearch && matchesAction;
            }
        };

        sorter.setRowFilter(rf);
        updateCountLabel(logsTable.getRowCount());
    }

    private void updateCountLabel(int count) {
        countLabel.setText("  " + count + " سجل");
    }

    private void handleClearLogs() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "هل أنت متأكد من مسح جميع سجلات النظام؟\nلا يمكن التراجع عن هذه العملية.",
            "تأكيد المسح", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String user = frame != null ? frame.getLoggedInUser().getUsername() : "SYSTEM";
                LogService.clearAllLogs(user);
                loadLogs();
                JOptionPane.showMessageDialog(this, "تم مسح السجلات بنجاح.", "تم", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "فشل مسح السجلات: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
