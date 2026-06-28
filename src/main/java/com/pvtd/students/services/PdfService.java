package com.pvtd.students.services;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.pvtd.students.models.Student;
import com.pvtd.students.models.Subject;
import com.pvtd.students.ui.utils.UITheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;

public class PdfService {

    public static String generatePassCertificate(Student student) throws Exception {
        String destPath = getStudentPdfPath(student, "شهادة_نجاح");

        // 1. Create a high-resolution JPanel representing the certificate
        JPanel certPanel = new JPanel(new BorderLayout());
        certPanel.setBackground(Color.WHITE);
        certPanel.setSize(800, 1131); // A4 roughly at 96 DPI
        certPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY, 10),
                new EmptyBorder(40, 40, 40, 40)));

        // Header
        JPanel header = new JPanel(new GridLayout(3, 1));
        header.setBackground(Color.WHITE);
        JLabel title1 = new JLabel("جمهورية مصر العربية", SwingConstants.CENTER);
        title1.setFont(new Font("Tahoma", Font.BOLD, 20));
        JLabel title2 = new JLabel("مصلحة الكفاية الإنتاجية والتدريب المهني", SwingConstants.CENTER);
        title2.setFont(new Font("Tahoma", Font.BOLD, 22));
        JLabel title3 = new JLabel("شهادة نجاح وتخرج", SwingConstants.CENTER);
        title3.setFont(new Font("Tahoma", Font.BOLD, 28));
        title3.setForeground(UITheme.PRIMARY);

        header.add(title1);
        header.add(title2);
        header.add(title3);
        certPanel.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(40, 0, 40, 0));

        String bodyText = String.format(
                "<html><div style='text-align:center; font-family:tahoma; font-size:18px; line-height:2.0;' dir='rtl'>"
                        +
                        "تشهد إدارة المركز بأن الطالب / <b>%s</b><br>" +
                        "المولود في <b>%s-%s-%s</b> ، والرقم القومي: <b>%s</b><br>" +
                        "رقم الجلوس: <b>%s</b> ، التخصص: <b>%s</b><br><br>" +
                        "قد أتم بنجاح متطلبات التخرج واجتاز جميع الاختبارات المقررة بحالة: " +
                        "<b style='color:green;'>ناجح</b>.<br>" +
                        "ونتمنى له دوام التوفيق والنجاح في مسيرته المهنية.</div></html>",
                student.getNationalId(), student.getSeatNo(), student.getProfession());

        JLabel bodyLabel = new JLabel(bodyText, SwingConstants.CENTER);
        bodyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(bodyLabel);

        certPanel.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new GridLayout(1, 2));
        footer.setBackground(Color.WHITE);
        JLabel sign1 = new JLabel("شئون الطلبة", SwingConstants.CENTER);
        sign1.setFont(new Font("Tahoma", Font.BOLD, 18));
        JLabel sign2 = new JLabel("مدير المركز", SwingConstants.CENTER);
        sign2.setFont(new Font("Tahoma", Font.BOLD, 18));
        footer.add(sign1);
        footer.add(sign2);
        certPanel.add(footer, BorderLayout.SOUTH);

        // 2. Render to Image
        certPanel.doLayout();
        BufferedImage image = new BufferedImage(certPanel.getWidth(), certPanel.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        certPanel.printAll(g2d);
        g2d.dispose();

        // 3. Write Image to PDF
        writeImageToPdf(image, destPath);
        return destPath;
    }

    public static String generateStudentForm(Student student) throws Exception {
        String destPath = getStudentPdfPath(student, "استمارة_بيانات");

        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setSize(800, 1131);
        formPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("استمارة بيانات ودرجات الطالب", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.BOLD, 26));
        formPanel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(0, 1, 0, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(30, 0, 30, 0));

        Font f = new Font("Tahoma", Font.PLAIN, 18);
        content.add(createRightAlignedLabel("الاسم: " + student.getName(), f));
        content.add(createRightAlignedLabel("الرقم القومي: " + student.getNationalId(), f));
        content.add(createRightAlignedLabel("رقم الجلوس: " + student.getSeatNo(), f));
        content.add(createRightAlignedLabel("مركز التدريب: " + student.getCenterName(), f));
        content.add(createRightAlignedLabel("المهنة: " + student.getProfession(), f));
        content.add(createRightAlignedLabel("الحالة: " + student.getStatus(), f));

        // Add Grades
        content.add(createRightAlignedLabel(" ", f));
        content.add(createRightAlignedLabel("--- بيان الدرجات ---", new Font("Tahoma", Font.BOLD, 18)));

        java.util.List<Subject> subjects = SubjectService.getSubjectsByProfession(student.getProfession());
        Map<Integer, Integer> grades = student.getGrades();
        
        // --- Group subjects by parent to merge 30/70 split ---
        java.util.List<Subject> parentSubjects = new java.util.ArrayList<>();
        java.util.Map<Integer, java.util.List<Subject>> childrenMap = new java.util.HashMap<>();
        for (Subject s : subjects) {
            if (s.getParentSubjectId() == null) {
                parentSubjects.add(s);
            } else {
                childrenMap.computeIfAbsent(s.getParentSubjectId(), k -> new java.util.ArrayList<>()).add(s);
            }
        }

        int totalMax = 0;
        int totalAttained = 0;

        for (Subject sub : parentSubjects) {
            int score = 0;
            int subMax = 0;
            
            if (childrenMap.containsKey(sub.getId())) {
                // Composite: sum grades and max marks of all children
                for (Subject child : childrenMap.get(sub.getId())) {
                    score += (grades != null && grades.containsKey(child.getId())) ? grades.get(child.getId()) : 0;
                    subMax += child.getMaxMark();
                }
            } else {
                score = (grades != null && grades.containsKey(sub.getId())) ? grades.get(sub.getId()) : 0;
                subMax = sub.getMaxMark();
            }
            
            totalMax += subMax;
            totalAttained += score;
            content.add(createRightAlignedLabel(sub.getName() + " : " + score + " / " + subMax, f));
        }

        content.add(createRightAlignedLabel("-------------------", f));
        content.add(createRightAlignedLabel("المجموع الكلي: " + totalAttained + " / " + totalMax,
                new Font("Tahoma", Font.BOLD, 18)));

        formPanel.add(content, BorderLayout.CENTER);

        formPanel.doLayout();
        BufferedImage image = new BufferedImage(formPanel.getWidth(), formPanel.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        formPanel.printAll(g2d);
        g2d.dispose();

        writeImageToPdf(image, destPath);
        return destPath;
    }

    private static JLabel createRightAlignedLabel(String text, Font font) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(font);
        l.setBackground(Color.WHITE);
        l.setOpaque(true);
        return l;
    }

    private static String getStudentPdfPath(Student student, String prefix) {
        String safeId = (student.getNationalId() != null && !student.getNationalId().trim().isEmpty())
                ? student.getNationalId().trim().replaceAll("[^a-zA-Z0-9.-]", "_")
                : "unknown_id";

        String userHome = System.getProperty("user.home");
        File studentFolder = new File(userHome, ".student_mgmt/students/" + safeId);
        if (!studentFolder.exists()) {
            studentFolder.mkdirs();
        }

        String safeName = student.getName().replaceAll("[^a-zA-Z0-9.\u0600-\u06FF\\s-]", "_").trim();
        File dest = new File(studentFolder, prefix + "_" + safeName + ".pdf");
        return dest.getAbsolutePath();
    }

    private static void writeImageToPdf(BufferedImage image, String destPath) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        ImageData imageData = ImageDataFactory.create(baos.toByteArray());

        PdfWriter writer = new PdfWriter(destPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(0, 0, 0, 0);

        Image pdfImage = new Image(imageData);
        pdfImage.setWidth(PageSize.A4.getWidth());
        pdfImage.setHeight(PageSize.A4.getHeight());

        document.add(pdfImage);
        document.close();
    }
}
