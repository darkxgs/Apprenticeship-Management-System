package com.pvtd.students.ui.pages;

import com.pvtd.students.services.ExcelService;
import com.pvtd.students.ui.utils.UITheme;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class ImportPage extends JPanel {

    private JProgressBar progressBar;
    private JLabel statusLabel;

    public ImportPage() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 40, 40, 40));
        setBackground(UITheme.BG_LIGHT);

        // Create a central card-like container
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.setBackground(UITheme.CARD_BG);
        centerContainer.setOpaque(false);
        centerContainer.setBorder(BorderFactory.createCompoundBorder(
                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 10, 0.08f, 20, UITheme.CARD_BG),
                new EmptyBorder(40, 40, 40, 40)));

        // Info Section
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel iconLbl = new JLabel();
        try {
            FlatSVGIcon icon = new FlatSVGIcon("icons/import.svg", 64, 64);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> UITheme.PRIMARY));
            iconLbl.setIcon(icon);
        } catch (Exception e) {
        }
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLbl.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("استيراد كشوف الطلاب (Excel)", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel desc = new JLabel("الرجاء تحميل ملف الإكسل الذي يحتوي على أرقام الجلوس، الأسماء، والأرقام القومية.",
                SwingConstants.CENTER);
        desc.setFont(UITheme.FONT_BODY);
        desc.setForeground(UITheme.TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(iconLbl);
        infoPanel.add(title);
        infoPanel.add(desc);

        centerContainer.add(infoPanel, BorderLayout.NORTH);

        // Upload Action section
        JPanel uploadPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        uploadPanel.setOpaque(false);

        JButton uploadBtn = new JButton("اختيار ملف وبدء الرفع");
        uploadBtn.setFont(UITheme.FONT_HEADER);
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setBackground(UITheme.PRIMARY);
        uploadBtn.setPreferredSize(new Dimension(250, 50));
        uploadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadBtn.putClientProperty("JButton.buttonType", "roundRect");

        uploadBtn.addActionListener(e -> handleExcelUpload());

        uploadPanel.add(uploadBtn);
        centerContainer.add(uploadPanel, BorderLayout.CENTER);

        // Progress Panel
        JPanel progressPanel = new JPanel(new BorderLayout(0, 10));
        progressPanel.setOpaque(false);

        statusLabel = new JLabel("في انتظار الملف...", SwingConstants.CENTER);
        statusLabel.setFont(UITheme.FONT_BODY);
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(0, 20));
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        progressPanel.add(statusLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        centerContainer.add(progressPanel, BorderLayout.SOUTH);

        add(centerContainer, BorderLayout.CENTER);
    }

    private void handleExcelUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("اختر ملف إكسل الطلاب");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // UI Prep
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            statusLabel.setText("جاري استيراد البيانات من " + selectedFile.getName() + "...");

            // Background Thread processing
            SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                @Override
                protected Integer doInBackground() {
                    return ExcelService.importStudentsFromExcel(selectedFile);
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(100);

                    try {
                        int count = get();
                        if (count >= 0) {
                            statusLabel.setText("تم الانتهاء! تم معالجة " + count + " سجل بنجاح.");
                            JOptionPane.showMessageDialog(ImportPage.this, "تم استيراد " + count + " سجل بنجاح.",
                                    "نجاح",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            statusLabel.setText("فشل معالجة الملف.");
                            JOptionPane.showMessageDialog(ImportPage.this, "حدث خطأ أثناء استيراد الملف.", "خطأ",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        statusLabel.setText("فشل معالجة الملف.");
                    }
                }
            };
            worker.execute();
        }
    }
}
