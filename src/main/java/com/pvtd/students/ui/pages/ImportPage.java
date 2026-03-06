package com.pvtd.students.ui.pages;

import com.pvtd.students.services.ExcelService;
import com.pvtd.students.ui.utils.UITheme;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class ImportPage extends JPanel {

    private File profileFolder = null;
    private File frontIdFolder = null;
    private File backIdFolder = null;

    private JLabel profileLabel;
    private JLabel frontIdLabel;
    private JLabel backIdLabel;
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

        JLabel desc = new JLabel("الرجاء تحميل ملف الإكسل ومجلدات الصور (اختياري لربط الصور تلقائياً).",
                SwingConstants.CENTER);
        desc.setFont(UITheme.FONT_BODY);
        desc.setForeground(UITheme.TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(iconLbl);
        infoPanel.add(title);
        infoPanel.add(desc);

        centerContainer.add(infoPanel, BorderLayout.NORTH);

        // Upload Action section
        JPanel uploadPanel = new JPanel(new GridLayout(2, 2, 20, 30));
        uploadPanel.setOpaque(false);
        uploadPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        // 1. Profile Folder
        JPanel profileBox = new JPanel();
        profileBox.setLayout(new BoxLayout(profileBox, BoxLayout.Y_AXIS));
        profileBox.setOpaque(false);
        JButton btnProfile = createFolderBtn("مجلد الصور الشخصية", e -> handleFolderSelect(1));
        profileLabel = createFolderLbl();
        profileBox.add(btnProfile);
        profileBox.add(Box.createVerticalStrut(5));
        profileBox.add(profileLabel);

        // 2. Front ID Folder
        JPanel frontIdBox = new JPanel();
        frontIdBox.setLayout(new BoxLayout(frontIdBox, BoxLayout.Y_AXIS));
        frontIdBox.setOpaque(false);
        JButton btnFrontId = createFolderBtn("مجلد صور وجه البطاقة", e -> handleFolderSelect(2));
        frontIdLabel = createFolderLbl();
        frontIdBox.add(btnFrontId);
        frontIdBox.add(Box.createVerticalStrut(5));
        frontIdBox.add(frontIdLabel);

        // 3. Back ID Folder
        JPanel backIdBox = new JPanel();
        backIdBox.setLayout(new BoxLayout(backIdBox, BoxLayout.Y_AXIS));
        backIdBox.setOpaque(false);
        JButton btnBackId = createFolderBtn("مجلد صور ظهر البطاقة", e -> handleFolderSelect(3));
        backIdLabel = createFolderLbl();
        backIdBox.add(btnBackId);
        backIdBox.add(Box.createVerticalStrut(5));
        backIdBox.add(backIdLabel);

        // 4. Excel File Upload
        JPanel excelBox = new JPanel();
        excelBox.setLayout(new BoxLayout(excelBox, BoxLayout.Y_AXIS));
        excelBox.setOpaque(false);

        JButton uploadBtn = new JButton("اختيار ملف وبدء الرفع");
        uploadBtn.setFont(UITheme.FONT_HEADER);
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setBackground(UITheme.PRIMARY);
        uploadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadBtn.putClientProperty("JButton.buttonType", "roundRect");
        uploadBtn.addActionListener(e -> handleExcelUpload());
        excelBox.add(uploadBtn);

        uploadPanel.add(profileBox);
        uploadPanel.add(frontIdBox);
        uploadPanel.add(backIdBox);
        uploadPanel.add(excelBox);

        JPanel outerUploadBox = new JPanel(new FlowLayout(FlowLayout.CENTER));
        outerUploadBox.setOpaque(false);
        outerUploadBox.add(uploadPanel);

        centerContainer.add(outerUploadBox, BorderLayout.CENTER);

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

    private JButton createFolderBtn(String text, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(new Color(0x1F2937));
        btn.setBackground(new Color(0xE5E7EB));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.addActionListener(listener);
        return btn;
    }

    private JLabel createFolderLbl() {
        JLabel lbl = new JLabel("لم يتم اختيار مجلد", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private void handleFolderSelect(int type) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("اختر مجلد الصور");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            if (type == 1) {
                profileFolder = selectedFolder;
                profileLabel.setText(selectedFolder.getName());
                profileLabel.setForeground(new Color(0x15803D));
            } else if (type == 2) {
                frontIdFolder = selectedFolder;
                frontIdLabel.setText(selectedFolder.getName());
                frontIdLabel.setForeground(new Color(0x15803D));
            } else if (type == 3) {
                backIdFolder = selectedFolder;
                backIdLabel.setText(selectedFolder.getName());
                backIdLabel.setForeground(new Color(0x15803D));
            }
        }
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
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            statusLabel.setText("جاري التجهيز لبدء الاستيراد...");

            // Background Thread processing
            SwingWorker<Integer, String> worker = new SwingWorker<>() {
                @Override
                protected Integer doInBackground() {
                    return ExcelService.importStudentsFromExcel(selectedFile, profileFolder, frontIdFolder,
                            backIdFolder,
                            (current, total, msg) -> {
                                int pct = (int) (((double) current / total) * 100);
                                setProgress(pct);
                                publish(msg + " (" + current + "/" + total + ")");
                            });
                }

                @Override
                protected void process(java.util.List<String> chunks) {
                    if (!chunks.isEmpty()) {
                        statusLabel.setText(chunks.get(chunks.size() - 1));
                    }
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    progressBar.setValue(100);

                    try {
                        int count = get();
                        if (count >= 0) {
                            statusLabel.setText("تم الانتهاء! تم معالجة " + count + " سجل بنجاح.");
                            JOptionPane.showMessageDialog(ImportPage.this, "تم استيراد/تحديث " + count + " سجل بنجاح.",
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

            worker.addPropertyChangeListener(evt -> {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            });

            worker.execute();
        }
    }
}
