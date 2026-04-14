import java.sql.*;
import com.pvtd.students.db.DatabaseConnection;

public class CheckSubjects {
    public static void main(String[] args) {
        String profession = "فني تشغيل وصيانة الطلمبات والضواغط";
        System.out.println("Checking subjects for: " + profession);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, name, parent_subject_id, max_mark, sub_name FROM subjects WHERE TRIM(profession) = TRIM(?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, profession);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf("ID: %d | Name: %s | ParentID: %s | Max: %d | SubName: %s\n",
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getObject("parent_subject_id"),
                                rs.getInt("max_mark"),
                                rs.getString("sub_name"));
                    }
                }
            }
            
            // Try with broader search
            System.out.println("\nBroader search (LIKE %الطلمبات%):");
            sql = "SELECT id, name, parent_subject_id, max_mark, profession FROM subjects WHERE profession LIKE '%الطلمبات%'";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("ID: %d | Name: %s | ParentID: %s | Max: %d | Prof: %s\n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getObject("parent_subject_id"),
                            rs.getInt("max_mark"),
                            rs.getString("profession"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
