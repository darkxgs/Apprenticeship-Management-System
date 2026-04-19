package com.pvtd.students;

import com.pvtd.students.db.DatabaseConnection;
import java.sql.*;
import java.io.PrintWriter;

public class DBDebugger {
    public static void main(String[] args) {
        try (Connection con = DatabaseConnection.getConnection();
             PrintWriter out = new PrintWriter("db_dump.txt")) {
            
            out.println("--- Student Statuses for بنی سویف ---");
            String sql = "SELECT DISTINCT status FROM students WHERE center_name LIKE '%بني سويف%'";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.println("Status: [" + rs.getString("status") + "]");
                }
            }
            
            out.println("\n--- Sample Missing Students ? ---");
            sql = "SELECT name, status FROM students WHERE center_name LIKE '%بني سويف%' LIMIT 10";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.println("Name: " + rs.getString("name") + " | Status: [" + rs.getString("status") + "]");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
