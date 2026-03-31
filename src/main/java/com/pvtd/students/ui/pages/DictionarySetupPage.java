package com.pvtd.students.ui.pages;

import com.pvtd.students.services.DictionaryService;
import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.DropShadowBorder;
import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.ui.utils.WrapLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DictionarySetupPage extends JPanel {

    private final AppFrame frame;
    private final String pageTitle;
    private final String category;
    private JPanel gridPanel;

    public DictionarySetupPage(AppFrame frame, String pageTitle, String category) {
        this.frame = frame;
        this.pageTitle = pageTitle;
        this.category = category;

        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_LIGHT);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildGrid(), BorderLayout.CENTER);

        loadCards();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(28, 28, 16, 28));

        JLabel titleLbl = new JLabel("إدارة " + pageTitle, SwingConstants.RIGHT);
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLbl = new JLabel("أضف ، عَدِّل ، أو احذف الخيارات المتاحة لـ " + pageTitle, SwingConstants.RIGHT);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(UITheme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(titleLbl);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subLbl);

        JButton btnAdd = new JButton("+ إضافة جديد");
        btnAdd.setFont(UITheme.FONT_HEADER);
        btnAdd.setBackground(UITheme.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.setPreferredSize(new Dimension(150, 40));
        btnAdd.addActionListener(e -> handleAdd());

        header.add(titleBlock, BorderLayout.EAST);
        header.add(btnAdd, BorderLayout.WEST);
        return header;
    }

    private JScrollPane buildGrid() {
        gridPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setBackground(UITheme.BG_LIGHT);
        gridPanel.setBorder(new EmptyBorder(12, 24, 24, 24));

        JScrollPane scroll = new JScrollPane(gridPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BG_LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void loadCards() {
        gridPanel.removeAll();
        
        List<String> items = DictionaryService.getCombinedItems(category);
        for (String item : items) {
            gridPanel.add(buildCard(item));
        }
        
        if (items.isEmpty()) {
            JLabel empty = new JLabel(
                "<html><div style='text-align:center'>لا توجد بيانات مُضافة بعد.<br>اضغط &laquo;+ إضافة جديد&raquo; للإنشاء.</div></html>",
                SwingConstants.CENTER);
            empty.setFont(new Font("Tahoma", Font.PLAIN, 14));  // Tahoma renders Arabic correctly
            empty.setForeground(UITheme.TEXT_SECONDARY);
            empty.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            gridPanel.add(empty);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel buildCard(String itemName) {
        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setOpaque(false);
        // Fixed width, tall enough for 2 lines of Arabic text
        cardWrapper.setPreferredSize(new Dimension(260, 130));
        cardWrapper.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(Color.BLACK, 6, 0.07f, 16, UITheme.BG_LIGHT),
                new EmptyBorder(0, 0, 0, 0)));

        JPanel cardInner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                // Color line at top
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), 6, 16, 16);
                g2.fillRect(0, 4, getWidth(), 2);
                g2.dispose();
            }
        };
        cardInner.setOpaque(false);
        cardInner.setBorder(new EmptyBorder(18, 18, 16, 18));

        // Name — no width constraint, RTL dir, wraps automatically
        JLabel nameLbl = new JLabel(
            "<html><div dir='rtl' style='text-align:right; line-height:1.5'>" + itemName + "</div></html>",
            SwingConstants.RIGHT);
        nameLbl.setFont(new Font("Tahoma", Font.BOLD, 14));
        nameLbl.setForeground(UITheme.TEXT_PRIMARY);

        cardInner.add(nameLbl, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Edit
        JButton btnEdit = new JButton("تعديل");
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setBackground(UITheme.PRIMARY);
        btnEdit.setFocusPainted(false);
        btnEdit.setPreferredSize(new Dimension(72, 28));
        btnEdit.putClientProperty("JButton.buttonType", "roundRect");
        btnEdit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> handleEdit(itemName));

        // Delete (DANGER)
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
        btnDel.addActionListener(e -> handleDelete(itemName));

        btnRow.add(btnEdit);
        btnRow.add(btnDel);

        cardInner.add(btnRow, BorderLayout.SOUTH);
        cardWrapper.add(cardInner, BorderLayout.CENTER);

        return cardWrapper;
    }

    private void handleAdd() {
        String input = JOptionPane.showInputDialog(this, "أدخل اسم " + pageTitle + " الجديد:", "إضافة", JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            input = input.trim();
            if (DictionaryService.getCombinedItems(category).contains(input)) {
                JOptionPane.showMessageDialog(this, "هذا الاسم موجود بالفعل!", "خطأ", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                String user = frame != null ? frame.getLoggedInUser().getUsername() : "SYSTEM";
                DictionaryService.addItem(category, input, user);
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "حدث خطأ أثناء الحفظ.", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEdit(String oldVal) {
        String newVal = (String) JOptionPane.showInputDialog(this, "تعديل الاسم (سيتم تعديله في جميع بيانات الطلاب أيضاً):",
                "تعديل", JOptionPane.QUESTION_MESSAGE, null, null, oldVal);
        if (newVal != null && !newVal.trim().isEmpty() && !newVal.trim().equals(oldVal)) {
            newVal = newVal.trim();
            if (DictionaryService.getCombinedItems(category).contains(newVal)) {
                JOptionPane.showMessageDialog(this, "هذا الاسم موجود بالفعل!", "خطأ", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                String user = frame != null ? frame.getLoggedInUser().getUsername() : "SYSTEM";
                DictionaryService.renameItem(category, oldVal, newVal, user);
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "حدث خطأ أثناء التعديل.", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete(String val) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "هل أنت متأكد من حذف '" + val + "' ؟\n(سيتم حذفه من القائمة فقط، بيانات الطلاب لن تُحذف)",
                "تأكيد الحذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String user = frame != null ? frame.getLoggedInUser().getUsername() : "SYSTEM";
                DictionaryService.deleteItem(category, val, user);
                loadCards();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "حدث خطأ أثناء الحذف.", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
