package com.pvtd.students.ui.components;

import com.pvtd.students.ui.utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddAdminDialog extends JDialog {

    private JTextField nameField;
    private JTextField usernameField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JComboBox<String> roleCombo;

    public AddAdminDialog(Window owner) {
        super(owner, "إضافة مدير جديد", ModalityType.APPLICATION_MODAL);
        initComponents();
        setSize(450, 520);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(new EmptyBorder(25, 25, 25, 25));
        contentPane.setBackground(UITheme.BG_LIGHT);

        JLabel headerLabel = new JLabel("تسجيل حساب مدير جديد", SwingConstants.CENTER);
        headerLabel.setFont(UITheme.FONT_TITLE);
        headerLabel.setForeground(UITheme.TEXT_PRIMARY);
        contentPane.add(headerLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(10, 1, 5, 5));
        formPanel.setOpaque(false);
        formPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        nameField = createStyledTextField("الاسم الكامل");
        usernameField = createStyledTextField("اسم المستخدم");
        phoneField = createStyledTextField("رقم الهاتف المحمول");
        passwordField = new JPasswordField();
        passwordField.putClientProperty("JPasswordField.placeholderText", "كلمة المرور");
        passwordField.setFont(UITheme.FONT_BODY);
        passwordField.setPreferredSize(new Dimension(0, 40));

        roleCombo = new JComboBox<>(new String[] { "مدير نظام (Admin)", "مدخل بيانات (Data Entry)" });
        roleCombo.setFont(UITheme.FONT_BODY);
        roleCombo.setPreferredSize(new Dimension(0, 40));
        ((JLabel) roleCombo.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);

        formPanel.add(createLabel("الاسم الكامل:"));
        formPanel.add(nameField);
        formPanel.add(createLabel("اسم المستخدم:"));
        formPanel.add(usernameField);
        formPanel.add(createLabel("رقم الهاتف:"));
        formPanel.add(phoneField);
        formPanel.add(createLabel("كلمة المرور:"));
        formPanel.add(passwordField);
        formPanel.add(createLabel("الصلاحية:"));
        formPanel.add(roleCombo);

        contentPane.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);

        JButton saveBtn = new JButton("تسجيل المستخدم");
        saveBtn.setFont(UITheme.FONT_HEADER);
        saveBtn.setBackground(UITheme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(150, 45));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.putClientProperty("JButton.buttonType", "roundRect");
        saveBtn.addActionListener(e -> saveUser());

        JButton cancelBtn = new JButton("إلغاء");
        cancelBtn.setFont(UITheme.FONT_HEADER);
        cancelBtn.setPreferredSize(new Dimension(100, 45));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.putClientProperty("JButton.buttonType", "roundRect");
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        contentPane.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.RIGHT);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        return lbl;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.FONT_BODY);
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        tf.setPreferredSize(new Dimension(0, 40));
        return tf;
    }

    private void saveUser() {
        String name = nameField.getText().trim();
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "الرجاء تعبئة جميع الحقول المطلوبة.", "خطأ إدخال",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Add actual DB Registration Logic here using AuthService/UserService once
        // ready.
        JOptionPane.showMessageDialog(this, "تم إضافة المستخدم بنجاح! سيتم ربط الواجهة بقاعدة البيانات قريباً.",
                "عملية ناجحة", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
