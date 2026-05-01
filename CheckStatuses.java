import java.sql.*;
import com.pvtd.students.db.DatabaseConnection;

public class CheckStatuses {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM student_statuses")) {
            System.out.println("ID | Name | Code");
            System.out.println("-----------------");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " | " + rs.getString("status_name") + " | " + rs.getObject("status_code"));
            }
        }
    }
}
