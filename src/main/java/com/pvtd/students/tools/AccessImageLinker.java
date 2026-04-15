package com.pvtd.students.tools;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AccessImageLinker {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {}

        // Set default Arabic Font for UI
        UIManager.put("Label.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));

        JOptionPane.showMessageDialog(null, 
            "أهلاً بك في أداة ربط الصور بقاعدة بيانات أكسيس!\n\n" +
            "الخطوة الأولى: اختر ملف الأكسس الخاص بك (.accdb)\n" +
            "الخطوة الثانية: سيُطلب منك تحديد المجلد الذي يحتوي على الصور.", 
            "شاشة الترحيب", JOptionPane.INFORMATION_MESSAGE);

        JFileChooser accdbChooser = new JFileChooser(".");
        accdbChooser.setDialogTitle("اختر ملف الأكسس (مثال: الدور الاول2022_Backup.accdb)");
        accdbChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Access DB", "accdb", "mdb"));
        if (accdbChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        File dbFile = accdbChooser.getSelectedFile();

        JFileChooser folderChooser = new JFileChooser(".");
        folderChooser.setDialogTitle("اختر المجلد الذي يحتوي على صور الطلاب");
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (folderChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        File imageFolder = folderChooser.getSelectedFile();

        processLinking(dbFile, imageFolder);
        System.exit(0);
    }

    public static void processLinking(File dbFile, File imageFolder) {
        Map<String, File> imageCache = new HashMap<>();
        scanFolderRecursively(imageFolder, imageCache);
        
        if (imageCache.isEmpty()) {
             JOptionPane.showMessageDialog(null, "المجلد الذي اخترته لا يحتوي على أي صور!", "تنبيه", JOptionPane.WARNING_MESSAGE);
             return;
        }

        String dbUrl = "jdbc:ucanaccess://" + dbFile.getAbsolutePath() + ";memory=true";
        int matched = 0;
        int notFound = 0;
        int emptyIds = 0;
        String exampleFailedId = "";

        boolean showedAttachError = false;

        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            conn.setAutoCommit(false); 
            
            String selectQuery = "SELECT dofaa FROM t_stu_data1";
            String updateQuery = "UPDATE t_stu_data1 SET pic = ? WHERE dofaa = ?";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
                 PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                 ResultSet rs = selectStmt.executeQuery()) {

                int batchCounter = 0;
                while (rs.next()) {
                    String natId = rs.getString(1);
                    if (natId == null || natId.trim().isEmpty()) {
                        emptyIds++;
                        continue;
                    }

                    // Fix Scientific Notation and .0 for numeric Access columns
                    if (natId.contains("E") || natId.contains("e") || natId.contains(".")) {
                        try {
                            double d = rs.getDouble(1);
                            natId = new java.text.DecimalFormat("#").format(d);
                        } catch (Exception ignored) {}
                    }

                    String cleanId = natId.replaceAll("\\s+", "").toLowerCase();
                    File img = imageCache.get(cleanId);
                    
                    if (img != null) {
                        try {
                            String imgName = img.getName();
                            String ext = "";
                            int dotIndex = imgName.lastIndexOf('.');
                            if(dotIndex > 0) ext = imgName.substring(dotIndex + 1);
                            
                            byte[] fileData = java.nio.file.Files.readAllBytes(img.toPath());
                            net.ucanaccess.complex.Attachment[] atts = new net.ucanaccess.complex.Attachment[] {
                                new net.ucanaccess.complex.Attachment(null, imgName, ext, fileData, java.time.LocalDateTime.now(), 0)
                            };
                            
                            updateStmt.setObject(1, atts);
                            updateStmt.setString(2, natId);
                            updateStmt.executeUpdate();
                            matched++;
                            
                            batchCounter++;
                            if (batchCounter >= 50) {
                                conn.commit();
                                batchCounter = 0;
                            }
                        } catch (Throwable ex) {
                            if (!showedAttachError) {
                                JOptionPane.showMessageDialog(null, "فشلت محاولة رفع صورة بداخل الأكسيس:\nالرقم القومي: " + natId + "\nالخطأ: " + ex.toString(), "خطأ تقني", JOptionPane.ERROR_MESSAGE);
                                showedAttachError = true;
                            }
                            notFound++;
                        }
                    } else {
                        notFound++;
                        if (exampleFailedId.isEmpty()) exampleFailedId = natId;
                    }
                }
                conn.commit();
            }
            String msg = "تمت العملية بنجاح!\n\n" +
                         "تم ربط " + matched + " صورة بمساراتها الأوتوماتيكية.\n" +
                         "تعذر إيجاد صور لعدد: " + notFound + " طلاب.\n" +
                         "طلاب تم تخطيهم (لا يوجد لديهم رقم كومي): " + emptyIds + " طالب.\n" +
                         (notFound > 0 ? "\nمثال لرقم لم نجد صورته في المجلد أو فشلت إضافة صورته: " + exampleFailedId : "");
            JOptionPane.showMessageDialog(null, msg, "ملخص العملية", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.sql.SQLException sex) {
            sex.printStackTrace();
            
            if (sex.getMessage() != null && sex.getMessage().toLowerCase().contains("object not found")) {
                try (Connection conn = DriverManager.getConnection(dbUrl)) {
                    java.sql.DatabaseMetaData metaData = conn.getMetaData();
                    StringBuilder sb = new StringBuilder("يبدو أن اسم العمود مختلف في الأكسيس الخاص بك.\n\nهذه هي الأعمدة المتاحة في جدول t_stu_data1:\n\n");
                    try (ResultSet columns = metaData.getColumns(null, null, "t_stu_data1".toUpperCase(), null)) {
                        while (columns.next()) {
                            sb.append("- ").append(columns.getString("COLUMN_NAME")).append("\n");
                        }
                    }
                    if (sb.toString().contains("- ")) {
                        JOptionPane.showMessageDialog(null, sb.toString(), "ابحث عن الاسم الصحيح", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e2) {}
            } else {
                JOptionPane.showMessageDialog(null, "حدث خطأ غير متوقع أثناء محاولة حفظ المسار!\n\n١. الأغلب أن ملف الأكسيس (accdb) مفتوح حالياً.. يرجى إغلاقه تماماً قبل التشغيل!\n٢. أو أن حقل pic ليس نصاً (Short Text) بل مرفقات (Attachment).\n\nتفاصيل الخطأ:\n" + sex.getMessage(), "خطأ في الحفظ", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "انغلق البرنامج بسبب طارئ داخلي (ربما نقص ذاكرة):\n" + e.toString(), "عطل جذري", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void scanFolderRecursively(File dir, java.util.Map<String, File> cache) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanFolderRecursively(f, cache);
            } else {
                String name = f.getName();
                int dotIndex = name.lastIndexOf('.');
                String base = (dotIndex == -1) ? name : name.substring(0, dotIndex);
                String norm = base.trim().replaceAll("\\s+", "").toLowerCase();
                if (!norm.isEmpty()) cache.put(norm, f);
                
                norm = base.replaceAll("[^a-z0-9\u0621-\u064A\u0660-\u0669.-]", "_").toLowerCase();
                if (!norm.isEmpty()) cache.put(norm, f);
            }
        }
    }
}
