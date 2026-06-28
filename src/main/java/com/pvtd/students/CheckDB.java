import com.pvtd.students.db.DatabaseConnection;
import java.sql.*;

public class CheckDB {
    public static void main(String[] args) throws Exception {
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT center_name, region, governorate FROM students WHERE ROWNUM <= 10")) {
            System.out.println("DUMPING 10 STUDENTS:");
            while (rs.next()) {
                String center = rs.getString("center_name");
                String region = rs.getString("region");
                System.out.println("Center: [" + center + "], Region: [" + region + "]");
                if (center != null) {
                    for (char ch : center.toCharArray()) {
                        System.out.print((int)ch + " ");
                    }
                    System.out.println();
                }
            }
        }
    }
}
