package com.pvtd.students.ui.pages;

import com.pvtd.students.models.Department;
import com.pvtd.students.services.DepartmentService;
import com.pvtd.students.services.SpecializationService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.ui.utils.DropShadowBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * مراكز التدريب (Training Centers / Departments)
 * Displays each center as a card showing specialization count and student
 * count.
 * Context: مصلحة الكفاية الإنتاجية والتدريب المهني — نظام دبلوم التلمذة
 * الصناعية
 */
public class DepartmentsPage extends JPanel {

        private JPanel gridPanel;
        private JLabel summaryLabel;

        public DepartmentsPage(AppFrame frame) {
                setLayout(new BorderLayout(0, 20));
                setBorder(new EmptyBorder(28, 28, 28, 28));
                setBackground(UITheme.BG_LIGHT);

                // ─── Header ───────────────────────────────────────────────────────────
                JPanel header = new JPanel(new BorderLayout());
                header.setOpaque(false);

                JPanel titleBlock = new JPanel();
                titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
                titleBlock.setOpaque(false);
                titleBlock.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

                JLabel titleLabel = new JLabel("مراكز التدريب المهني", SwingConstants.RIGHT);
                titleLabel.setFont(UITheme.FONT_TITLE);
                titleLabel.setForeground(UITheme.TEXT_PRIMARY);
                titleLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

                JLabel subLabel = new JLabel("مصلحة الكفاية الإنتاجية والتدريب المهني", SwingConstants.RIGHT);
                subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                subLabel.setForeground(new Color(0x64748B));
                subLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
                subLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

                summaryLabel = new JLabel("", SwingConstants.RIGHT);
                summaryLabel.setFont(UITheme.FONT_BODY);
                summaryLabel.setForeground(new Color(0x94A3B8));
                summaryLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
                summaryLabel.setBorder(new EmptyBorder(2, 0, 0, 0));

                titleBlock.add(titleLabel);
                titleBlock.add(subLabel);
                titleBlock.add(summaryLabel);

                JButton btnAdd = new JButton("＋ إضافة مركز");
                btnAdd.setFont(UITheme.FONT_HEADER);
                btnAdd.setBackground(UITheme.SUCCESS);
                btnAdd.setForeground(Color.WHITE);
                btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btnAdd.putClientProperty("JButton.buttonType", "roundRect");
                btnAdd.setPreferredSize(new Dimension(150, 42));
                btnAdd.addActionListener(e -> showAddDialog());

                JPanel leftArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                leftArea.setOpaque(false);
                leftArea.add(btnAdd);

                header.add(titleBlock, BorderLayout.EAST);
                header.add(leftArea, BorderLayout.WEST);
                add(header, BorderLayout.NORTH);

                // ─── Grid of Center Cards ─────────────────────────────────────────────
                gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
                gridPanel.setOpaque(false);

                JScrollPane scroll = new JScrollPane(gridPanel);
                scroll.setBorder(BorderFactory.createEmptyBorder());
                scroll.getViewport().setBackground(UITheme.BG_LIGHT);
                scroll.getVerticalScrollBar().setUnitIncrement(16);
                add(scroll, BorderLayout.CENTER);

                loadCenters();
        }

        // ─── Load all training centers as cards ───────────────────────────────────
        private void loadCenters() {
                gridPanel.removeAll();
                List<Department> depts = DepartmentService.getAllDepartments();
                for (Department d : depts) {
                        gridPanel.add(buildCenterCard(d));
                }
                summaryLabel.setText(depts.size() + " مركز مسجل");
                gridPanel.revalidate();
                gridPanel.repaint();
        }

        // ─── Individual Center Card ───────────────────────────────────────────────
        private JPanel buildCenterCard(Department dept) {
                int specCount = DepartmentService.getSpecializationCount(dept.getId());
                int studentCount = DepartmentService.getStudentCountByDepartment(dept.getId());

                // Outer card with shadow
                JPanel card = new JPanel(new BorderLayout(0, 0)) {
                        @Override
                        protected void paintComponent(Graphics g) {
                                Graphics2D g2 = (Graphics2D) g.create();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                // Top accent bar
                                g2.setColor(UITheme.PRIMARY);
                                g2.fillRoundRect(0, 0, getWidth(), 8, 8, 8);
                                g2.setColor(UITheme.CARD_BG);
                                g2.fillRect(0, 4, getWidth(), getHeight() - 4);
                                g2.dispose();
                        }
                };
                card.setOpaque(false);
                card.setBorder(BorderFactory.createCompoundBorder(
                                new DropShadowBorder(Color.BLACK, 6, 0.08f, 18, UITheme.CARD_BG),
                                new EmptyBorder(16, 20, 20, 20)));

                // Top section: Icon + Name
                JPanel topRow = new JPanel(new BorderLayout(12, 0));
                topRow.setOpaque(false);
                topRow.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                topRow.setBorder(new EmptyBorder(8, 0, 16, 0));

                JLabel iconLbl = new JLabel("🏭", SwingConstants.CENTER);
                iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
                iconLbl.setPreferredSize(new Dimension(50, 50));

                JPanel nameBlock = new JPanel();
                nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS));
                nameBlock.setOpaque(false);

                JLabel nameLbl = new JLabel(dept.getName(), SwingConstants.RIGHT);
                nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
                nameLbl.setForeground(UITheme.TEXT_PRIMARY);
                nameLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

                String desc = dept.getDescription() != null && !dept.getDescription().isEmpty()
                                ? dept.getDescription()
                                : "مركز تدريب مهني مسجل";
                JLabel descLbl = new JLabel("<html><div style='text-align:right'>" + desc + "</div></html>",
                                SwingConstants.RIGHT);
                descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                descLbl.setForeground(new Color(0x94A3B8));
                descLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
                descLbl.setBorder(new EmptyBorder(3, 0, 0, 0));

                nameBlock.add(nameLbl);
                nameBlock.add(descLbl);

                topRow.add(iconLbl, BorderLayout.WEST);
                topRow.add(nameBlock, BorderLayout.CENTER);

                // ─── Stats Row ────────────────────────────────────────────────────────
                JPanel statsRow = new JPanel(new GridLayout(1, 2, 10, 0));
                statsRow.setOpaque(false);
                statsRow.setBorder(new EmptyBorder(0, 0, 16, 0));
                statsRow.add(buildStatChip(String.valueOf(specCount), "تخصص", new Color(0xEFF6FF),
                                new Color(0x2563EB)));
                statsRow.add(buildStatChip(String.valueOf(studentCount), "طالب", new Color(0xF0FDF4),
                                new Color(0x16A34A)));

                // ─── Action Buttons ───────────────────────────────────────────────────
                JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
                btnRow.setOpaque(false);

                JButton btnView = new JButton("التخصصات");
                styleCardBtn(btnView, UITheme.PRIMARY, new Color(0xEFF6FF));
                btnView.addActionListener(e -> {
                        // Navigate to SpecializationsPage — future: could filter by this dept
                        JOptionPane.showMessageDialog(this,
                                        "عرض تخصصات: " + dept.getName() + "\nعدد التخصصات: " + specCount,
                                        "التخصصات", JOptionPane.INFORMATION_MESSAGE);
                });

                JButton btnDelete = new JButton("حذف");
                styleCardBtn(btnDelete, UITheme.DANGER, new Color(0xFEF2F2));
                btnDelete.addActionListener(e -> {
                        if (studentCount > 0) {
                                JOptionPane.showMessageDialog(this,
                                                "لا يمكن حذف المركز لأنه يحتوي على " + studentCount + " طالب مسجل.",
                                                "تحذير", JOptionPane.WARNING_MESSAGE);
                                return;
                        }
                        int confirm = JOptionPane.showConfirmDialog(this,
                                        "حذف مركز «" + dept.getName() + "»؟\n" +
                                                        (specCount > 0 ? "⚠ سيتم حذف " + specCount + " تخصص مرتبط به."
                                                                        : ""),
                                        "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (confirm == JOptionPane.YES_OPTION) {
                                DepartmentService.deleteDepartment(dept.getId());
                                loadCenters();
                        }
                });

                btnRow.add(btnView);
                btnRow.add(btnDelete);

                card.add(topRow, BorderLayout.NORTH);
                card.add(statsRow, BorderLayout.CENTER);
                card.add(btnRow, BorderLayout.SOUTH);

                // Hover lift effect
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                                card.setBorder(BorderFactory.createCompoundBorder(
                                                new DropShadowBorder(Color.BLACK, 10, 0.13f, 18, UITheme.CARD_BG),
                                                new EmptyBorder(16, 20, 20, 20)));
                                card.repaint();
                        }

                        @Override
                        public void mouseExited(java.awt.event.MouseEvent e) {
                                card.setBorder(BorderFactory.createCompoundBorder(
                                                new DropShadowBorder(Color.BLACK, 6, 0.08f, 18, UITheme.CARD_BG),
                                                new EmptyBorder(16, 20, 20, 20)));
                                card.repaint();
                        }
                });

                return card;
        }

        // ─── Stat Chip ────────────────────────────────────────────────────────────
        private JPanel buildStatChip(String value, String label, Color bg, Color accent) {
                JPanel chip = new JPanel(new BorderLayout(0, 2));
                chip.setBackground(bg);
                chip.setOpaque(true);
                chip.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(
                                                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60), 1,
                                                true),
                                new EmptyBorder(8, 12, 8, 12)));

                JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
                valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
                valLbl.setForeground(accent);

                JLabel lbl = new JLabel(label, SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lbl.setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));

                chip.add(valLbl, BorderLayout.CENTER);
                chip.add(lbl, BorderLayout.SOUTH);
                return chip;
        }

        private void styleCardBtn(JButton btn, Color fg, Color bg) {
                btn.setFont(UITheme.FONT_BODY);
                btn.setForeground(fg);
                btn.setBackground(bg);
                btn.setOpaque(true);
                btn.setFocusPainted(false);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.putClientProperty("JButton.buttonType", "roundRect");
        }

        // ─── Add Center Dialog ────────────────────────────────────────────────────
        private void showAddDialog() {
                JPanel p = new JPanel(new GridLayout(2, 2, 10, 10));
                p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                JTextField nameF = new JTextField();
                nameF.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                JTextField descF = new JTextField();
                descF.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                p.add(new JLabel("اسم المركز:", SwingConstants.RIGHT));
                p.add(nameF);
                p.add(new JLabel("وصف مختصر:", SwingConstants.RIGHT));
                p.add(descF);

                if (JOptionPane.showConfirmDialog(this, p, "إضافة مركز تدريب جديد",
                                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                        String name = nameF.getText().trim();
                        if (name.isEmpty()) {
                                JOptionPane.showMessageDialog(this, "اسم المركز مطلوب.", "تنبيه",
                                                JOptionPane.WARNING_MESSAGE);
                                return;
                        }
                        if (DepartmentService.addDepartment(name, descF.getText().trim())) {
                                loadCenters();
                        } else {
                                JOptionPane.showMessageDialog(this, "حدث خطأ أثناء الإضافة.", "خطأ",
                                                JOptionPane.ERROR_MESSAGE);
                        }
                }
        }
}
