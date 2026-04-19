package com.pvtd.students.ui.components;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

public class QRCodeComponent extends JLabel {

    private String data;
    private int size;

    // Constructor افتراضي
    public QRCodeComponent() {
        this.data = "بيانات الطالب";
        this.size = 150;
        generate();
    }

    // Constructor مخصص
    public QRCodeComponent(String data, int size) {
        this.data = data;
        this.size = size;
        generate();
    }

    // تغيير البيانات
    public void setData(String data) {
        this.data = data;
        generate();
    }

    // ✅ بيانات الطالب (مضاف لها التليفون والدرجات)
    public void setStudentData(String name, String nationalId, String seatNo,
                               String center, String group, String percentage, String phone, String gradesText) {

        String dataStr =
                "📄 بيانات الطالب\n" +
                "------------------------\n" +
                "👤 الاسم: " + name + "\n" +
                "🆔 الرقم القومي: " + nationalId + "\n" +
                "🎓 رقم الجلوس: " + seatNo + "\n" +
                "📱 التليفون: " + (phone != null && !phone.trim().isEmpty() ? phone : "غير مسجل") + "\n" +
                "🏫 المركز: " + center + "\n" +
                "📊 النسبة: " + percentage + "%\n" +
                "👥 المجموعة: " + group + "\n" +
                "------------------------\n" +
                "📚 درجات الطالب:\n" + gradesText;

        setData(dataStr);
    }

    // توليد QR Code
    private void generate() {

        if (data == null || data.isEmpty()) {
            setText("QR");
            setIcon(null);
            return;
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();

            // ✅ مهم جداً عشان العربي
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix matrix = writer.encode(
                    data,
                    BarcodeFormat.QR_CODE,
                    size,
                    size,
                    hints
            );

            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            setText(null);
            setIcon(new ImageIcon(image));

        } catch (Exception e) {
            e.printStackTrace();
            setText("QR");
            setIcon(null);
        }
    }
}