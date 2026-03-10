
package com.pvtd.students.ui.components;

import javax.swing.*;
import java.awt.image.BufferedImage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
public class QRCodeComponent extends JLabel{
    private String data;
    private int size;
  public QRCodeComponent() {
        
        this.data = "رقم البطاقه هنا عشان يدخله علي الشهاده";
        this.size = 150;
        generate();
    
    }
    public QRCodeComponent(String data, int size) {
        this.data = data;
        this.size = size;
        generate();
    }

    public void setData(String data) {
        this.data = data;
        generate();
    }

   private void generate() {

    if (data == null || data.isEmpty()) {
        setText("QR");
        return;
    }

    try {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(
                data,
                BarcodeFormat.QR_CODE,
                size,
                size
        );

        BufferedImage image =
                MatrixToImageWriter.toBufferedImage(matrix);

        setText(null);
        setIcon(new ImageIcon(image));

    } catch (Exception e) {
        setText("QR");
    }
}
}
