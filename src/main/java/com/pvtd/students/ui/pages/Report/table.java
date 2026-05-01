
package com.pvtd.students.ui.pages.Report;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JFrame;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;
/**
 *
 * @author Seif
 */
public class table extends javax.swing.JPanel {

    
    Color headerColor = new Color(191,229,231);

List<StudentRow> students = new ArrayList<>();
List<String> theorySubjects = new ArrayList<>();

public table() {
    initComponents();
    setBackground(Color.WHITE);
    setPreferredSize(new java.awt.Dimension(2000,600));
    loadStudents();
}

private void loadStudents(){

    new SwingWorker<Void,Void>(){

        @Override
        protected Void doInBackground() throws Exception {

            Connection con = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:XE",
                    "system",
                    "123"
            );

            String sql =
            "SELECT id,name,profession_code,profession,national_id,seat_no,secret_no,specialization_id " +
            "FROM students";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){

                int studentId = rs.getInt("id");
                int specializationId = rs.getInt("specialization_id");

                List<SubjectMark> marks = getMarks(con,studentId);

                for(SubjectMark m : marks){
                    if("نظري".equals(m.type)){
                        if(!theorySubjects.contains(m.name)){
                            theorySubjects.add(m.name);
                        }
                    }
                }

                StudentRow row = new StudentRow();

                row.name = rs.getString("name");
                row.professionCode = rs.getString("profession_code");
                row.profession = rs.getString("profession");
                row.nationalId = rs.getString("national_id");
                row.seatNo = rs.getString("seat_no");
                row.secretNo = rs.getString("secret_no");

                row.marks = marks;

                students.add(row);

            }

            con.close();
            return null;
        }

        @Override
        protected void done(){
            repaint();
        }

    }.execute();

}

private List<SubjectMark> getMarks(Connection con,int studentId) throws Exception{

    List<SubjectMark> list = new ArrayList<>();

    String sql =
    "SELECT sub.name,sub.subject_type,sg.obtained_mark,sub.max_mark " +
    "FROM student_grades sg " +
    "JOIN subjects sub ON sg.subject_id=sub.id " +
    "WHERE sg.student_id=?";

    PreparedStatement ps = con.prepareStatement(sql);
    ps.setInt(1, studentId);

    ResultSet rs = ps.executeQuery();

    while(rs.next()){

        SubjectMark m = new SubjectMark();

        m.name = rs.getString("name");
        m.type = rs.getString("subject_type");
        m.mark = rs.getInt("obtained_mark");
        m.max = rs.getInt("max_mark");

        list.add(m);
    }

    return list;
}

@Override
protected void paintComponent(Graphics g){

    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;

    int x = 20;
    int y = 200;
    int h = 60;

    drawHeader(g2,x,y,40,h,"م"); x+=40;
    drawHeader(g2,x,y,180,h,"الاسم"); x+=180;
    drawHeader(g2,x,y,100,h,"كود الحرفة"); x+=100;
    drawHeader(g2,x,y,150,h,"الحرفة"); x+=150;
    drawHeader(g2,x,y,150,h,"الرقم القومي"); x+=150;
    drawHeader(g2,x,y,100,h,"رقم الجلوس"); x+=100;
    drawHeader(g2,x,y,100,h,"الرقم السري"); x+=100;

    for(String subject : theorySubjects){

        drawHeader(g2,x,y,80,h,subject);
        x+=80;

    }

    drawHeader(g2,x,y,120,h,"مجموع النظري"); x+=120;
    drawHeader(g2,x,y,100,h,"عملي"); x+=100;
    drawHeader(g2,x,y,100,h,"تطبيقي"); x+=100;
    drawHeader(g2,x,y,150,h,"مجموع عملي وتطبيقي"); x+=150;
    drawHeader(g2,x,y,120,h,"المجموع الكلي"); x+=120;
    drawHeader(g2,x,y,100,h,"حالة");

    int rowY = y + h;
    int index = 1;

    for(StudentRow s : students){

        int col = 20;

        drawCell(g2,col,rowY,40,40,String.valueOf(index)); col+=40;
        drawCell(g2,col,rowY,180,40,s.name); col+=180;
        drawCell(g2,col,rowY,100,40,s.professionCode); col+=100;
        drawCell(g2,col,rowY,150,40,s.profession); col+=150;
        drawCell(g2,col,rowY,150,40,s.nationalId); col+=150;
        drawCell(g2,col,rowY,100,40,s.seatNo); col+=100;
        drawCell(g2,col,rowY,100,40,s.secretNo); col+=100;

        int theoryTotal = 0;
        int practical = 0;
        int applied = 0;

        for(String subject : theorySubjects){

            int mark = 0;

            for(SubjectMark m : s.marks){
                if(m.name.equals(subject)){
                    mark = m.mark;
                    theoryTotal += mark;
                }
            }

            drawCell(g2,col,rowY,80,40,String.valueOf(mark));
            col+=80;

        }

        for(SubjectMark m : s.marks){

            if("عملي".equals(m.type))
                practical += m.mark;

            if("تطبيقي".equals(m.type))
                applied += m.mark;

        }

        int practicalTotal = practical + applied;
        int total = theoryTotal + practicalTotal;

        drawCell(g2,col,rowY,120,40,String.valueOf(theoryTotal)); col+=120;
        drawCell(g2,col,rowY,100,40,String.valueOf(practical)); col+=100;
        drawCell(g2,col,rowY,100,40,String.valueOf(applied)); col+=100;
        drawCell(g2,col,rowY,150,40,String.valueOf(practicalTotal)); col+=150;
        drawCell(g2,col,rowY,120,40,String.valueOf(total)); col+=120;

        String grade;

        if(total >= 85) grade = "ممتاز";
        else if(total >= 75) grade = "جيد جدا";
        else if(total >= 65) grade = "جيد";
        else if(total >= 50) grade = "مقبول";
        else grade = "راسب";

        drawCell(g2,col,rowY,100,40,grade);

        rowY += 40;
        index++;

    }

}

private void drawHeader(Graphics2D g2,int x,int y,int w,int h,String text){

    g2.setColor(headerColor);
    g2.fillRect(x,y,w,h);

    g2.setColor(Color.BLACK);
    g2.drawRect(x,y,w,h);

    FontMetrics fm = g2.getFontMetrics();

    int tx = x + (w - fm.stringWidth(text))/2;
    int ty = y + (h + fm.getAscent())/2 - 3;

    g2.drawString(text,tx,ty);
}

private void drawCell(Graphics2D g2,int x,int y,int w,int h,String text){

    g2.setColor(Color.WHITE);
    g2.fillRect(x,y,w,h);

    g2.setColor(Color.BLACK);
    g2.drawRect(x,y,w,h);

    g2.drawString(text,x+10,y+25);
}

class StudentRow{

    String name;
    String professionCode;
    String profession;
    String nationalId;
    String seatNo;
    String secretNo;

    List<SubjectMark> marks;

}

class SubjectMark{

    String name;
    String type;

    int mark;
    int max;

}
  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
