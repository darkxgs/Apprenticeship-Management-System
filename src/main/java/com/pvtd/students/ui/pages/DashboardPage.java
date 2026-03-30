package com.pvtd.students.ui.pages;

import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.ui.components.CustomPieChart;
import com.pvtd.students.services.StudentService;
import com.pvtd.students.models.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.List;

public class DashboardPage extends JPanel {

        private AppFrame parentFrame;

        public DashboardPage(AppFrame frame) {
                this.parentFrame = frame;
                setLayout(new BorderLayout(24, 24));
                setBorder(new EmptyBorder(24, 24, 24, 24));
                setBackground(UITheme.BG_LIGHT);

                // 1. Top Section - Statistics Cards
                Map<String, Integer> stats = StudentService.getDashboardStats();

                JPanel statsInnerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
                statsInnerPanel.setOpaque(false);

                // Explicit 4 cards requested by the user
                statsInnerPanel.add(createStatCard("إجمالي الطلاب",
                                String.format("%,d", stats.getOrDefault("total", 0)), new Color(41, 128, 185)));
                statsInnerPanel.add(createStatCard("ناجح", String.format("%,d", stats.getOrDefault("ناجح", 0)),
                                new Color(0x10B981)));
                statsInnerPanel.add(createStatCard("راسب", String.format("%,d", stats.getOrDefault("راسب", 0)),
                                new Color(0xEF4444)));
                statsInnerPanel.add(createStatCard("دور ثاني", String.format("%,d", stats.getOrDefault("دور ثاني", 0)),
                                new Color(0xF97316)));

                JScrollPane statsScroll = new JScrollPane(statsInnerPanel);
                statsScroll.setBorder(null);
                statsScroll.setOpaque(false);
                statsScroll.getViewport().setOpaque(false);
                statsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                statsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                statsScroll.getHorizontalScrollBar().setUnitIncrement(16);
                statsScroll.setPreferredSize(new Dimension(0, 160));

                add(statsScroll, BorderLayout.NORTH);

                // 2. Middle Section - Charts and Data
                JPanel centerPanel = new JPanel(new GridLayout(1, 2, 24, 24));
                centerPanel.setOpaque(false);

                // A. Pie Chart
                JPanel chartContainer = new JPanel(new BorderLayout());
                chartContainer.setOpaque(false);
                chartContainer.setBorder(BorderFactory.createCompoundBorder(
                                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f, 20,
                                                UITheme.CARD_BG),
                                new EmptyBorder(25, 25, 25, 25)));

                JLabel chartTitle = new JLabel("توزيع حالات الطلاب", SwingConstants.CENTER);
                chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
                chartTitle.setForeground(new Color(0x1e293b));
                chartTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
                chartContainer.add(chartTitle, BorderLayout.NORTH);

                CustomPieChart pieChart = new CustomPieChart(stats);
                chartContainer.add(pieChart, BorderLayout.CENTER);

                // B. Recent Students Table
                JPanel tableContainer = new JPanel(new BorderLayout());
                tableContainer.setOpaque(false);
                tableContainer.setBorder(BorderFactory.createCompoundBorder(
                                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f, 20,
                                                UITheme.CARD_BG),
                                new EmptyBorder(15, 15, 15, 15)));

                JLabel tableTitle = new JLabel("أحدث التعديلات", SwingConstants.RIGHT);
                tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
                tableTitle.setForeground(new Color(0x1e293b));
                tableTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
                tableContainer.add(tableTitle, BorderLayout.NORTH);

                // Important data only
                String[] columns = { "رقم الجلوس", "الاسم", "الحالة" };
                DefaultTableModel model = new DefaultTableModel(columns, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                                return false;
                        }
                };

                List<Student> recentStudents = StudentService.getRecentStudents(7);
                for (Student s : recentStudents) {
                        model.addRow(new Object[] {
                                        s.getSeatNo(),
                                        s.getName(),
                                        s.getStatus() != null ? s.getStatus() : "غير محدد"
                        });
                }

                JTable table = new JTable(model) {
                        @Override
                        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row,
                                        int column) {
                                Component c = super.prepareRenderer(renderer, row, column);
                                if (!isRowSelected(row)) {
                                        c.setBackground(Color.WHITE);
                                }
                                return c;
                        }
                };
                table.setRowHeight(55);
                table.setFont(UITheme.FONT_BODY);

                // Header styling & Center Alignment for a cleaner look
                DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
                headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
                headerRenderer.setBackground(new Color(0xf8fafc));
                headerRenderer.setForeground(new Color(0x475569));
                headerRenderer.setFont(UITheme.FONT_HEADER);
                headerRenderer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xe2e8f0)));

                for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
                        table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
                }

                table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                table.setShowGrid(false);
                table.setIntercellSpacing(new Dimension(0, 0));

                // Status Badge Pill for Column 2 (Status in RTL, actually right-most physically
                // if LTR, but visually leftmost)
                table.getColumnModel().getColumn(2)
                                .setCellRenderer(new com.pvtd.students.ui.utils.StatusBadgeRenderer());

                // Adjust alignment for main columns
                DefaultTableCellRenderer centerRenderer = new com.pvtd.students.ui.utils.CleanCellRenderer();
                centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
                table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Seating No
                table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Name

                // Highlight Rows
                table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                        int lastHoveredRow = -1;

                        public void mouseMoved(java.awt.event.MouseEvent e) {
                                int row = table.rowAtPoint(e.getPoint());
                                if (row != lastHoveredRow) {
                                        if (row > -1) {
                                                table.setRowSelectionInterval(row, row);
                                                table.setSelectionBackground(new Color(0xf5f7fb));
                                                table.setSelectionForeground(UITheme.TEXT_PRIMARY);
                                        }
                                        lastHoveredRow = row;
                                }
                        }
                });

                table.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseExited(java.awt.event.MouseEvent e) {
                                table.clearSelection();
                        }
                });

                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                scrollPane.getViewport().setBackground(UITheme.CARD_BG);
                tableContainer.add(scrollPane, BorderLayout.CENTER);

                centerPanel.add(chartContainer);
                centerPanel.add(tableContainer);

                add(centerPanel, BorderLayout.CENTER);

                // 3. Bottom Section - System Logs Management
                JPanel bottomPanel = new JPanel(new BorderLayout());
                bottomPanel.setOpaque(false);
                bottomPanel.setBorder(new EmptyBorder(24, 0, 0, 0));

                JButton clearLogsBtn = new JButton("تفريغ السجلات");
                clearLogsBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                clearLogsBtn.setBackground(new Color(0xEF4444));
                clearLogsBtn.setForeground(Color.WHITE);
                clearLogsBtn.setFocusPainted(false);
                clearLogsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                clearLogsBtn.setPreferredSize(new Dimension(200, 45));
                clearLogsBtn.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                                clearLogsBtn.setBackground(new Color(0xDC2626));
                        }

                        public void mouseExited(java.awt.event.MouseEvent e) {
                                clearLogsBtn.setBackground(new Color(0xEF4444));
                        }
                });
                clearLogsBtn.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this,
                                        "هل أنت متأكد من تفريغ كافة سجلات النظام؟ لا يمكن التراجع عن هذه الخطوة.",
                                        "تأكيد تفريغ السجلات",
                                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (confirm == JOptionPane.YES_OPTION) {
                                try {
                                        String user = parentFrame != null ? parentFrame.getLoggedInUser().getUsername() : "SYSTEM";
                                        com.pvtd.students.services.LogService.clearAllLogs(user);
                                        JOptionPane.showMessageDialog(this, "تم تفريغ السجلات بنجاح.", "نجاح",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(this,
                                                        "حدث خطأ أثناء تفريغ السجلات: " + ex.getMessage(), "خطأ",
                                                        JOptionPane.ERROR_MESSAGE);
                                }
                        }
                });

                bottomPanel.add(clearLogsBtn, BorderLayout.SOUTH);
                add(bottomPanel, BorderLayout.SOUTH);
        }

        private JPanel createStatCard(String title, String value, Color accent) {
                JPanel cardWrapper = new JPanel(new BorderLayout());
                cardWrapper.setOpaque(false);
                cardWrapper.setPreferredSize(new Dimension(230, 115));

                cardWrapper.setBorder(BorderFactory.createCompoundBorder(
                                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 10, 0.08f, 20,
                                                UITheme.CARD_BG),
                                new EmptyBorder(16, 20, 16, 20)));

                // To prevent vertical overlap, we use a 2-row layout
                JPanel contentPanel = new JPanel(new GridLayout(2, 1, 0, 8));
                contentPanel.setOpaque(false);

                // Header: Title
                JPanel headPanel = new JPanel(new BorderLayout());
                headPanel.setOpaque(false);

                JLabel titleLabel = new JLabel(title, SwingConstants.RIGHT);
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
                titleLabel.setForeground(new Color(0x475569));

                // Colored dot indicator
                JPanel dotPanel = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                                super.paintComponent(g);
                                Graphics2D g2 = (Graphics2D) g;
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(accent);
                                g2.fillOval(4, 5, 12, 12);
                        }
                };
                dotPanel.setOpaque(false);
                dotPanel.setPreferredSize(new Dimension(20, 20));

                headPanel.add(titleLabel, BorderLayout.EAST);
                headPanel.add(dotPanel, BorderLayout.WEST);

                // Value
                JLabel valueLabel = new JLabel(value, SwingConstants.RIGHT);
                valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
                valueLabel.setForeground(accent);

                contentPanel.add(headPanel);
                contentPanel.add(valueLabel);

                cardWrapper.add(contentPanel, BorderLayout.CENTER);

                cardWrapper.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent evt) {
                                cardWrapper.setBorder(BorderFactory.createCompoundBorder(
                                                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 15, 0.12f,
                                                                20, UITheme.CARD_BG),
                                                new EmptyBorder(14, 20, 18, 20)));
                        }

                        public void mouseExited(java.awt.event.MouseEvent evt) {
                                cardWrapper.setBorder(BorderFactory.createCompoundBorder(
                                                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 10, 0.08f,
                                                                20, UITheme.CARD_BG),
                                                new EmptyBorder(16, 20, 16, 20)));
                        }
                });

                return cardWrapper;
        }
}
