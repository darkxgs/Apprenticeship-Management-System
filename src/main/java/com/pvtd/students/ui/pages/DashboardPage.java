package com.pvtd.students.ui.pages;

import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.ui.components.CustomDonutChart;
import com.pvtd.students.services.StudentService;
import com.pvtd.students.models.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.List;

public class DashboardPage extends JPanel {

        public DashboardPage() {
                setLayout(new BorderLayout(24, 24));
                setBorder(new EmptyBorder(24, 24, 24, 24));
                setBackground(UITheme.BG_LIGHT);

                // 1. Top Section - Statistics Cards (Dynamic based on DB Statuses)
                Map<String, Integer> stats = StudentService.getDashboardStats();
                List<String> dynamicStatuses = com.pvtd.students.services.StatusesService.getAllStatuses();

                // Use FlowLayout to allow cards to sit next to each other
                JPanel statsInnerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
                statsInnerPanel.setOpaque(false);

                // Add Total Card first
                statsInnerPanel.add(createStatCard("إجمالي الطلاب", String.format("%,d", stats.get("total")),
                                new Color(41, 128, 185)));

                // Color palette array for dynamic cards
                Color[] palette = {
                                new Color(46, 204, 113), // Green
                                new Color(231, 76, 60), // Red
                                new Color(241, 196, 15), // Yellow
                                new Color(155, 89, 182), // Purple
                                new Color(52, 73, 94), // Dark Blue
                                new Color(230, 126, 34), // Orange
                                new Color(22, 160, 133) // Teal
                };

                int colorIdx = 0;
                for (String status : dynamicStatuses) {
                        int count = stats.getOrDefault(status, 0);
                        statsInnerPanel.add(createStatCard(status, String.format("%,d", count),
                                        palette[colorIdx % palette.length]));
                        colorIdx++;
                }

                // Wrap in a horizontal scroll pane in case there are many statuses
                JScrollPane statsScroll = new JScrollPane(statsInnerPanel);
                statsScroll.setBorder(null);
                statsScroll.setOpaque(false);
                statsScroll.getViewport().setOpaque(false);
                statsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                statsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                statsScroll.getHorizontalScrollBar().setUnitIncrement(16);
                statsScroll.setPreferredSize(new Dimension(0, 150));

                add(statsScroll, BorderLayout.NORTH);

                // 2. Middle Section - Charts and Data
                JPanel centerPanel = new JPanel(new GridLayout(1, 2, 24, 24));
                centerPanel.setOpaque(false);

                // A. Custom Native Donut Chart
                JPanel chartContainer = new JPanel(new BorderLayout());
                chartContainer.setOpaque(false);
                chartContainer.setBorder(BorderFactory.createCompoundBorder(
                                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f, 20,
                                                UITheme.CARD_BG),
                                new EmptyBorder(25, 25, 25, 25)));

                JLabel chartTitle = new JLabel("توزيع حالات الطلاب", SwingConstants.CENTER);
                chartTitle.setFont(UITheme.FONT_CARD_TITLE);
                chartTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
                chartContainer.add(chartTitle, BorderLayout.NORTH);

                CustomDonutChart donutChart = new CustomDonutChart(stats);
                chartContainer.add(donutChart, BorderLayout.CENTER);

                // B. Recent Students Table (Preview)
                JPanel tableContainer = new JPanel(new BorderLayout());
                tableContainer.setOpaque(false);
                tableContainer.setBorder(BorderFactory.createCompoundBorder(
                                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f, 20,
                                                UITheme.CARD_BG),
                                new EmptyBorder(15, 15, 15, 15)));

                JLabel tableTitle = new JLabel("أحدث التعديلات", SwingConstants.RIGHT);
                tableTitle.setFont(UITheme.FONT_CARD_TITLE);
                tableTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
                tableContainer.add(tableTitle, BorderLayout.NORTH);

                String[] columns = { "رقم الجلوس", "الاسم", "الحالة", "المركز" };
                DefaultTableModel model = new DefaultTableModel(columns, 0) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                                return false;
                        }
                };

                List<Student> recentStudents = StudentService.getRecentStudents(5);
                for (Student s : recentStudents) {
                        com.pvtd.students.models.Specialization spec = com.pvtd.students.services.SpecializationService
                                        .getSpecializationById(s.getSpecializationId());
                        model.addRow(new Object[] {
                                        s.getSeatNo(),
                                        s.getName(),
                                        s.getStatus() != null ? s.getStatus() : "غير محدد",
                                        spec != null ? spec.getName() : "-"
                        });
                }

                JTable table = new JTable(model);
                table.setRowHeight(48); // Premium SaaS Row Height
                table.setFont(UITheme.FONT_BODY);
                table.getTableHeader().setFont(UITheme.FONT_HEADER);
                table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                table.setShowGrid(false);
                table.setIntercellSpacing(new Dimension(0, 0));
                table.getColumnModel().getColumn(2)
                                .setCellRenderer(new com.pvtd.students.ui.utils.StatusBadgeRenderer());

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

                JButton clearLogsBtn = new JButton("تفريغ كل سجلات النظام");
                clearLogsBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                clearLogsBtn.setBackground(UITheme.DANGER);
                clearLogsBtn.setForeground(Color.WHITE);
                clearLogsBtn.setFocusPainted(false);
                clearLogsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                clearLogsBtn.setPreferredSize(new Dimension(200, 45));
                clearLogsBtn.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(this,
                                        "هل أنت متأكد من تفريغ كافة سجلات النظام؟ لا يمكن التراجع عن هذه الخطوة.",
                                        "تأكيد تفريغ السجلات",
                                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (confirm == JOptionPane.YES_OPTION) {
                                try {
                                        com.pvtd.students.services.LogService.clearAllLogs();
                                        JOptionPane.showMessageDialog(this, "تم تفريغ السجلات بنجاح.", "نجاح",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(this,
                                                        "حدث خطأ أثناء تفريغ السجلات: " + ex.getMessage(), "خطأ",
                                                        JOptionPane.ERROR_MESSAGE);
                                }
                        }
                });

                bottomPanel.add(clearLogsBtn, BorderLayout.WEST);
                add(bottomPanel, BorderLayout.SOUTH);
        }

        private JPanel createStatCard(String title, String value, Color accent) {
                JPanel card = new JPanel(new BorderLayout());
                card.setOpaque(false);
                card.setBorder(BorderFactory.createCompoundBorder(
                                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f, 20,
                                                UITheme.CARD_BG),
                                new EmptyBorder(24, 24, 24, 24)));

                JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
                titleLabel.setFont(UITheme.FONT_CARD_TITLE);
                titleLabel.setForeground(UITheme.TEXT_SECONDARY);
                titleLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

                JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
                valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
                valueLabel.setForeground(accent);

                card.add(titleLabel, BorderLayout.NORTH);
                card.add(valueLabel, BorderLayout.CENTER);

                // Hover Lift Micro-Animation
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent evt) {
                                card.setBorder(BorderFactory.createCompoundBorder(
                                                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 10, 0.12f,
                                                                20, UITheme.CARD_BG),
                                                new EmptyBorder(20, 24, 28, 24) // Shift card visually up
                                ));
                                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }

                        public void mouseExited(java.awt.event.MouseEvent evt) {
                                card.setBorder(BorderFactory.createCompoundBorder(
                                                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 6, 0.08f,
                                                                20, UITheme.CARD_BG),
                                                new EmptyBorder(24, 24, 24, 24) // Reset to normal
                                ));
                        }
                });

                return card;
        }
}
