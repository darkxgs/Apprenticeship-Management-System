package com.pvtd.students.ui.pages;

import com.pvtd.students.ui.AppFrame;
import com.pvtd.students.ui.utils.UITheme;
import com.pvtd.students.tools.AccessImageLinker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class AccessLinkerPage extends JPanel {

    private AppFrame frame;
    private File accessFile = null;
    private File imagesFolder = null;
    
    private JLabel dbFileLabel;
    private JLabel folderLabel;

    public AccessLinkerPage(AppFrame frame) {
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

        // --- Header Section ---
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel title = new JLabel("أداة ربط صور الأكسس", SwingConstants.CENTER);
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("هذه الأداة تقوم بقراءة مجلد الصور وإرفاقها تلقائياً بداخل ملف Access (accdb) الخاص بكم.", SwingConstants.CENTER);
        desc.setFont(UITheme.FONT_BODY);
        desc.setForeground(UITheme.TEXT_SECONDARY);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(title);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(desc);
        centerContainer.add(infoPanel, BorderLayout.NORTH);

        // --- Center Section ---
        JPanel centerSection = new JPanel();
        centerSection.setLayout(new BoxLayout(centerSection, BoxLayout.Y_AXIS));
        centerSection.setOpaque(false);
        
        // Buttons Panel
        JPanel choosersPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        choosersPanel.setOpaque(false);
        choosersPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        dbFileLabel = new JLabel("لم يتم اختيار ملف", SwingConstants.CENTER);
        dbFileLabel.setForeground(UITheme.TEXT_SECONDARY);
        dbFileLabel.setFont(UITheme.FONT_BODY);
        dbFileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel dbBox = new JPanel();
        dbBox.setLayout(new BoxLayout(dbBox, BoxLayout.Y_AXIS));
        dbBox.setOpaque(false);
        dbBox.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1, true));
        JButton btnDb = new JButton("اختر ملف الأكسس");
        btnDb.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDb.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(".");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Access DB", "accdb", "mdb"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                accessFile = chooser.getSelectedFile();
                dbFileLabel.setText(accessFile.getName());
                dbFileLabel.setForeground(new Color(0x15803D));
            }
        });
        dbBox.add(Box.createVerticalStrut(10));
        dbBox.add(btnDb);
        dbBox.add(Box.createVerticalStrut(10));
        dbBox.add(dbFileLabel);
        dbBox.add(Box.createVerticalStrut(10));

        folderLabel = new JLabel("لم يتم اختيار مجلد", SwingConstants.CENTER);
        folderLabel.setForeground(UITheme.TEXT_SECONDARY);
        folderLabel.setFont(UITheme.FONT_BODY);
        folderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel folderBox = new JPanel();
        folderBox.setLayout(new BoxLayout(folderBox, BoxLayout.Y_AXIS));
        folderBox.setOpaque(false);
        folderBox.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1, true));
        JButton btnFolder = new JButton("اختر مجلد الصور");
        btnFolder.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnFolder.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(".");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                imagesFolder = chooser.getSelectedFile();
                folderLabel.setText(imagesFolder.getName());
                folderLabel.setForeground(new Color(0x15803D));
            }
        });
        folderBox.add(Box.createVerticalStrut(10));
        folderBox.add(btnFolder);
        folderBox.add(Box.createVerticalStrut(10));
        folderBox.add(folderLabel);
        folderBox.add(Box.createVerticalStrut(10));

        choosersPanel.add(dbBox);
        choosersPanel.add(folderBox);
        
        centerSection.add(choosersPanel);
        centerSection.add(Box.createVerticalStrut(20));

        JButton startBtn = new JButton("🚀 بدء عملية ربط مسارات الصور إجبارياً");
        startBtn.setFont(UITheme.FONT_HEADER);
        startBtn.setForeground(Color.WHITE);
        startBtn.setBackground(UITheme.PRIMARY);
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startBtn.setMaximumSize(new Dimension(380, 48));
        startBtn.addActionListener(e -> {
            if (accessFile == null || imagesFolder == null) {
                JOptionPane.showMessageDialog(this, "يرجى تحديد الملف والمجلد معاً أولاً!", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }
            startBtn.setEnabled(false);
            startBtn.setText("جاري التنفيذ... الرجاء الانتظار");
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void,Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        // Hacky way to access the static processLinking method
                        // We will add public modifier to it in a moment
                        AccessImageLinker.processLinking(accessFile, imagesFolder);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return null;
                }
                @Override
                protected void done() {
                    startBtn.setEnabled(true);
                    startBtn.setText("🚀 بدء عملية ربط مسارات الصور إجبارياً");
                }
            };
            worker.execute();
        });
        
        centerSection.add(startBtn);

        JPanel outerCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        outerCenter.setOpaque(false);
        outerCenter.add(centerSection);
        centerContainer.add(outerCenter, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
    }
}
