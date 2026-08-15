package com.pvtd.students.ui.components;




import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Vector;

public class GradesTableComponent extends JScrollPane {

    JTable table;
    DefaultTableModel model;

    public GradesTableComponent() {

        table = new JTable();
        model = new DefaultTableModel();

        table.setModel(model);

        setViewportView(table);

        table.setRowHeight(30);
        table.setFont(new java.awt.Font("Tahoma", 0, 14));
        table.setShowGrid(true);
    }

    public void loadStudent(Connection con , String seatNo) {

        try {

            model.setRowCount(0);
            model.setColumnCount(0);

            // نجيب الطالب
            String studentSql =
                    "SELECT id , specialization_id FROM students WHERE seat_no = ?";

            PreparedStatement psStudent = con.prepareStatement(studentSql);
            psStudent.setString(1, seatNo);
            ResultSet rsStudent = psStudent.executeQuery();

            if (!rsStudent.next()) return;

            int studentId = rsStudent.getInt("id");
            int specId = rsStudent.getInt("specialization_id");

            // نجيب المواد
            String subjectsSql =
                    "SELECT id , name FROM subjects WHERE specialization_id = ?";

            PreparedStatement psSubjects = con.prepareStatement(subjectsSql);
            psSubjects.setInt(1, specId);
            ResultSet rsSubjects = psSubjects.executeQuery();

            Vector<Integer> subjectIds = new Vector<>();

            model.addColumn("البيان");

            while (rsSubjects.next()) {

                model.addColumn(rsSubjects.getString("name"));
                subjectIds.add(rsSubjects.getInt("id"));
            }

            model.addColumn("المجموع");

            Vector<Object> row = new Vector<>();

            row.add("الدرجات");

            int total = 0;

            for (int subjectId : subjectIds) {

                String gradeSql =
                        "SELECT obtained_mark FROM student_grades WHERE student_id = ? AND subject_id = ?";

                PreparedStatement psGrade = con.prepareStatement(gradeSql);

                psGrade.setInt(1, studentId);
                psGrade.setInt(2, subjectId);

                ResultSet rsGrade = psGrade.executeQuery();

                int mark = 0;

                if (rsGrade.next()) {
                    mark = rsGrade.getInt("obtained_mark");
                }

                total += mark;

                row.add(mark);
            }

            row.add(total);

            model.addRow(row);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}