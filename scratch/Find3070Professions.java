import java.sql.*;
import com.pvtd.students.db.DatabaseConnection;

public class Find3070Professions {
    public static void main(String[] args) {
        System.out.println("Finding all professions that have 30/70 subjects...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT DISTINCT profession FROM subjects WHERE parent_subject_id IS NOT NULL";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String p = rs.getString(1);
                    System.out.println("- [" + p + "]");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
