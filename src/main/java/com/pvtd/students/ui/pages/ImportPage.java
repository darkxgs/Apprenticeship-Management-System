package com.pvtd.students.ui.pages;

import com.pvtd.students.services.ExcelService;
import com.pvtd.students.ui.AppFrame;
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
    private JCheckBox includeImagesCheck;
    private JPanel imageFoldersPanel;

    private AppFrame frame;

    public ImportPage(AppFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(40, 40, 40, 40));
        setBackground(UITheme.BG_LIGHT);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 20));
        centerContainer.setBackground(UITheme.CARD_BG);
        centerContainer.setOpaque(false);
        centerContainer.setBorder(BorderFactory.createCompoundBorder(
                new com.pvtd.students.ui.utils.DropShadowBorder(Color.BLACK, 10, 0.08f, 20, UITheme.CARD_BG),
                new EmptyBorder(40, 40, 40, 40)));

        // --- Header / Info Section ---
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel iconLbl = new JLabel();
        try {
            FlatSVGIcon icon = new FlatSVGIcon("icons/import.svg", 64, 64);
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> UITheme.PRIMARY));
            iconLbl.setIcon(icon);
        } catch (Exception e) {
            /* icon optional */ }
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLbl.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("استيراد كشوف الطلاب (Excel)", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("اختر ملف الإكسل لاستيراد بيانات الطلاب. يمكنك ربط مجلدات الصور اختيارياً.",
                SwingConstants.CENTER);
        desc.setFont(UITheme.FONT_BODY);
        desc.setForeground(UITheme.TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(iconLbl);
        infoPanel.add(title);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(desc);
        centerContainer.add(infoPanel, BorderLayout.NORTH);

        // --- Center Panel (Images Toggle + Folders) ---
        JPanel centerSection = new JPanel();
        centerSection.setLayout(new BoxLayout(centerSection, BoxLayout.Y_AXIS));
        centerSection.setOpaque(false);

        // Optional images checkbox
        includeImagesCheck = new JCheckBox("ربط مجلدات الصور (اختياري)");
        includeImagesCheck.setFont(UITheme.FONT_HEADER);
        includeImagesCheck.setForeground(UITheme.TEXT_PRIMARY);
        includeImagesCheck.setOpaque(false);
        includeImagesCheck.setAlignmentX(Component.CENTER_ALIGNMENT);
        includeImagesCheck.setSelected(false);
        includeImagesCheck.addActionListener(e -> {
            imageFoldersPanel.setVisible(includeImagesCheck.isSelected());
            if (!includeImagesCheck.isSelected()) {
                // Clear selections if user unchecks
                profileFolder = null;
                frontIdFolder = null;
                backIdFolder = null;
                profileLabel.setText("لم يتم اختيار مجلد");
                frontIdLabel.setText("لم يتم اختيار مجلد");
                backIdLabel.setText("لم يتم اختيار مجلد");
                profileLabel.setForeground(UITheme.TEXT_SECONDARY);
                frontIdLabel.setForeground(UITheme.TEXT_SECONDARY);
                backIdLabel.setForeground(UITheme.TEXT_SECONDARY);
            }
            revalidate();
            repaint();
        });

        // Image Folders Panel (hidden by default)
        imageFoldersPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        imageFoldersPanel.setOpaque(false);
        imageFoldersPanel.setVisible(false);
        imageFoldersPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // 1. Profile Folder
        JPanel profileBox = buildFolderBox("📷 صور شخصية", 1);

        // 2. Front ID Folder
        JPanel frontIdBox = buildFolderBox("🪪 وجه البطاقة", 2);

        // 3. Back ID Folder
        JPanel backIdBox = buildFolderBox("🪪 ظهر البطاقة", 3);

        imageFoldersPanel.add(profileBox);
        imageFoldersPanel.add(frontIdBox);
        imageFoldersPanel.add(backIdBox);

        // Hint label for image naming rule
        JLabel imageHint = new JLabel(
                "<html><center>تأكد أن أسماء الصور في المجلدات تطابق الرقم القومي لكل طالب<br>(مثال: 29901010111222.jpg)</center></html>",
                SwingConstants.CENTER);
        imageHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        imageHint.setForeground(new Color(0x9CA3AF));
        imageHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageFoldersPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        centerSection.add(Box.createVerticalStrut(15));
        centerSection.add(includeImagesCheck);
        centerSection.add(imageFoldersPanel);
        centerSection.add(Box.createVerticalStrut(5));

        // Image naming hint — only shown alongside image folders
        JPanel hintWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        hintWrapper.setOpaque(false);
        hintWrapper.add(imageHint);
        centerSection.add(hintWrapper);

        centerSection.add(Box.createVerticalStrut(20));

        // Excel Upload Button
        JButton uploadBtn = new JButton("📂  اختيار ملف إكسل وبدء الاستيراد");
        uploadBtn.setFont(UITheme.FONT_HEADER);
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setBackground(UITheme.PRIMARY);
        uploadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadBtn.putClientProperty("JButton.buttonType", "roundRect");
        uploadBtn.setPreferredSize(new Dimension(340, 48));
        uploadBtn.setMaximumSize(new Dimension(400, 48));
        uploadBtn.addActionListener(e -> handleExcelUpload());
        centerSection.add(uploadBtn);

        JPanel outerCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        outerCenter.setOpaque(false);
        outerCenter.add(centerSection);
        centerContainer.add(outerCenter, BorderLayout.CENTER);

        // --- Progress Section ---
        JPanel progressPanel = new JPanel(new BorderLayout(0, 8));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        statusLabel = new JLabel("في انتظار ملف الإكسل...", SwingConstants.CENTER);
        statusLabel.setFont(UITheme.FONT_BODY);
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(0, 22));
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        progressPanel.add(statusLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        centerContainer.add(progressPanel, BorderLayout.SOUTH);

        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel buildFolderBox(String label, int type) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB), 1, true),
                new EmptyBorder(12, 12, 12, 12)));

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btn = new JButton("اختيار مجلد");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(new Color(0xE5E7EB));
        btn.setForeground(new Color(0x1F2937));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.addActionListener(e -> handleFolderSelect(type));

        JLabel statusLbl = createFolderLbl();
        if (type == 1)
            profileLabel = statusLbl;
        else if (type == 2)
            frontIdLabel = statusLbl;
        else
            backIdLabel = statusLbl;

        box.add(lbl);
        box.add(Box.createVerticalStrut(8));
        box.add(btn);
        box.add(Box.createVerticalStrut(4));
        box.add(statusLbl);
        return box;
    }

    private JLabel createFolderLbl() {
        JLabel lbl = new JLabel("لم يتم اختيار مجلد", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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
            String folderName = selectedFolder.getName();
            if (type == 1) {
                profileFolder = selectedFolder;
                profileLabel.setText(folderName);
                profileLabel.setForeground(new Color(0x15803D));
            } else if (type == 2) {
                frontIdFolder = selectedFolder;
                frontIdLabel.setText(folderName);
                frontIdLabel.setForeground(new Color(0x15803D));
            } else {
                backIdFolder = selectedFolder;
                backIdLabel.setText(folderName);
                backIdLabel.setForeground(new Color(0x15803D));
            }
        }
    }

    private void handleExcelUpload() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("اختر ملف إكسل الطلاب");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (.xlsx)", "xlsx"));

        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File selectedFile = fileChooser.getSelectedFile();

        // Determine image folders (null if images toggle is off)
        File pf = includeImagesCheck.isSelected() ? profileFolder : null;
        File ff = includeImagesCheck.isSelected() ? frontIdFolder : null;
        File bf = includeImagesCheck.isSelected() ? backIdFolder : null;

        // Warn if images checkbox is on but no folders selected
        if (includeImagesCheck.isSelected() && pf == null && ff == null && bf == null) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "قمت بتفعيل خيار ربط الصور ولكنك لم تختر أي مجلد.\nهل تريد الاستمرار بدون ربط صور؟",
                    "تنبيه", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION)
                return;
        }

        // UI Prep
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        progressBar.setVisible(true);
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        statusLabel.setText("جاري التجهيز لبدء الاستيراد...");

        final String username = frame != null ? frame.getLoggedInUser().getUsername() : "SYSTEM";
        final File finalPf = pf, finalFf = ff, finalBf = bf;

        SwingWorker<Integer, String> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                return ExcelService.importStudentsFromExcel(selectedFile, finalPf, finalFf, finalBf, username,
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
                        statusLabel.setText("✅ تم الانتهاء! تم معالجة " + count + " سجل بنجاح.");
                        JOptionPane.showMessageDialog(ImportPage.this,
                                "تم استيراد/تحديث " + count + " طالب بنجاح.",
                                "نجاح", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusLabel.setText("❌ فشل معالجة الملف.");
                        JOptionPane.showMessageDialog(ImportPage.this,
                                "حدث خطأ أثناء استيراد الملف. تأكد من تنسيق الملف وأنه xlsx.",
                                "خطأ", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    statusLabel.setText("❌ فشل معالجة الملف.");
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
