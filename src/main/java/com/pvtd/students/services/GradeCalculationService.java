package com.pvtd.students.services;

import com.pvtd.students.models.Subject;
import java.util.List;
import java.util.Map;

/**
 * Handles all grade and rating calculations for the Data Entry page.
 */
public class GradeCalculationService {

    /**
     * Calculates the total for "Theory" (نظري) subjects.
     * Excludes "التطبيقي" and excludes any subject specifically marked "عملي".
     */
    public static int calculateTheoryTotal(List<Subject> subjects, Map<Integer, Integer> grades) {
        int total = 0;
        for (Subject sub : subjects) {
            String name = sub.getName() != null ? sub.getName() : "";
            String type = sub.getType() != null ? sub.getType() : "";
            
            // Exclude Applied and Practical
            if (!name.contains("تطبيقي") && !type.equals("عملي") && !name.contains("عملي") && !name.contains("دين") && !name.contains("عربي")) {
                int grade = grades.getOrDefault(sub.getId(), 0);
                if (grade > 0) total += grade;
            }
        }
        return total;
    }

    /**
     * Calculates the total for "Practical" (عملي) subjects.
     */
    public static int calculatePracticalTotal(List<Subject> subjects, Map<Integer, Integer> grades) {
        int total = 0;
        for (Subject sub : subjects) {
            String name = sub.getName() != null ? sub.getName() : "";
            String type = sub.getType() != null ? sub.getType() : "";
            
            if (!name.contains("تطبيقي") && (type.equals("عملي") || name.contains("عملي"))) {
                int grade = grades.getOrDefault(sub.getId(), 0);
                if (grade > 0) total += grade;
            }
        }
        return total;
    }

    /**
     * Calculates the total for "Applied" (تطبيقي) subjects.
     */
    public static int calculateAppliedTotal(List<Subject> subjects, Map<Integer, Integer> grades) {
        int total = 0;
        for (Subject sub : subjects) {
            String name = sub.getName() != null ? sub.getName() : "";
            if (name.contains("تطبيقي")) {
                int grade = grades.getOrDefault(sub.getId(), 0);
                if (grade > 0) total += grade;
            }
        }
        return total;
    }

    /**
     * Grand total simply sums everything.
     */
    public static int calculateGrandTotal(int theory, int practical, int applied) {
        return theory + practical + applied;
    }

    /**
     * Determines Rating (ممتاز, جيد جدا, جيد, مقبول, ضعيف)
     * Assuming standard percentage brackets on the total.
     */
    public static String calculateRating(int grandTotal, int maxPossibleTotal) {
        if (maxPossibleTotal == 0) return "-";
        
        double percentage = ((double) grandTotal / maxPossibleTotal) * 100;

        if (percentage >= 85) return "ممتاز";
        if (percentage >= 75) return "جيد جداً";
        if (percentage >= 65) return "جيد";
        if (percentage >= 50) return "مقبول";
        return "ضعيف";
    }

    /**
     * Generates a newline-separated string of failed subject names.
     */
    public static String getFailedSubjectsText(List<Subject> subjects, Map<Integer, Integer> grades) {
        StringBuilder sb = new StringBuilder();
        for (Subject sub : subjects) {
            int obtained = grades.getOrDefault(sub.getId(), 0);
            if (obtained < sub.getPassMark() && obtained >= 0) { // ignoring global negative markers here
                sb.append(sub.getName()).append("\n");
            }
        }
        return sb.toString().trim();
    }
    
    /**
     * Computes the maximum possible grade sum for the current subjects, 
     * to help calculate the percentage for Rating.
     */
    public static int calculateMaxPossibleTotal(List<Subject> subjects) {
        int total = 0;
        for (Subject sub : subjects) {
            String name = sub.getName() != null ? sub.getName() : "";
            // exclude religious/arabic if they don't count towards max general total (depends on system, usually religion is not counted)
            if (!name.contains("دين")) {
                total += sub.getMaxMark();
            }
        }
        return total;
    }
}
