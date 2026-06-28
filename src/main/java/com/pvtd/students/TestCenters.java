import com.pvtd.students.services.StudentService;
import com.pvtd.students.db.DatabaseConnection;
import com.pvtd.students.models.Student;
import java.util.List;

public class TestCenters {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing Centers:");
        var regions = com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION);
        System.out.println("Regions: " + regions);
        if(!regions.isEmpty()) {
            String testRegion = "الكل";
            System.out.println("Testing Region: " + testRegion);
            java.util.Map<String, String> centers = StudentService.getCentersByRegionWithCodes(testRegion);
            System.out.println("Centers for region " + testRegion + ": " + centers);
            String testCenter = "مركز أجا";
            List<Student> list = StudentService.searchStudents("", "", "الكل", "الكل", "الكل", "الكل", testCenter);
            System.out.println("Students found for exactly '" + testCenter + "': " + list.size());
        }
    }
}
