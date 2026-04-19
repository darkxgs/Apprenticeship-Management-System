package com.pvtd.students.services;

import com.pvtd.students.models.Subject;
import java.util.List;
import java.util.Map;

/**
 * Handles all grade and rating calculations for the Data Entry page.
 */
public class GradeCalculationService {

    /**
     * Resolves composite grades by summing marks from child subjects into their parents.
     * Returns a new map containing all original grades plus calculated parent grades.
     */
    public static Map<Integer, Integer> resolveCompositeGrades(List<Subject> subjects, Map<Integer, Integer> grades) {
        Map<Integer, Integer> resolved = new java.util.HashMap<>(grades);
        
        // Sum children into parents
        for (Subject sub : subjects) {
            if (sub.getParentSubjectId() != null) {
                int parentId = sub.getParentSubjectId();
                int childGrade = grades.getOrDefault(sub.getId(), 0);
                
                // Add child grade to parent total (existing or starts at 0)
                int currentParentTotal = resolved.getOrDefault(parentId, 0);
                resolved.put(parentId, currentParentTotal + childGrade);
            }
        }
        return resolved;
    }

    /**
     * Calculates the total for "Theory" (نظري) subjects.
     * Excludes "التطبيقي" and excludes any subject specifically marked "عملي".
     * Only considers top-level subjects (ignoring individual 30/70 sub-marks).
     */
    public static int calculateTheoryTotal(List<Subject> subjects, Map<Integer, Integer> grades) {
        Map<Integer, Integer> resGrades = resolveCompositeGrades(subjects, grades);
        int total = 0;
        for (Subject sub : subjects) {
            if (sub.getParentSubjectId() != null) continue; // Only top-level
            
            String type = sub.getType() != null ? sub.getType().trim() : "نظري";
            if (!type.equals("عملي") && !type.equals("تطبيقي")) {
                int grade = resGrades.getOrDefault(sub.getId(), 0);
                if (grade > 0) total += grade;
            }
        }
        return total;
    }

    /**
     * Calculates the total for "Practical" (عملي) subjects.
     */
    public static int calculatePracticalTotal(List<Subject> subjects, Map<Integer, Integer> grades) {
        Map<Integer, Integer> resGrades = resolveCompositeGrades(subjects, grades);
        int total = 0;
        for (Subject sub : subjects) {
            if (sub.getParentSubjectId() != null) continue; // Only top-level
            
            String type = sub.getType() != null ? sub.getType().trim() : "";
            if (type.equals("عملي")) {
                int grade = resGrades.getOrDefault(sub.getId(), 0);
                if (grade > 0) total += grade;
            }
        }
        return total;
    }

    /**
     * Calculates the total for "Applied" (تطبيقي) subjects.
     */
    public static int calculateAppliedTotal(List<Subject> subjects, Map<Integer, Integer> grades) {
        Map<Integer, Integer> resGrades = resolveCompositeGrades(subjects, grades);
        int total = 0;
        for (Subject sub : subjects) {
            if (sub.getParentSubjectId() != null) continue; // Only top-level
            
            String type = sub.getType() != null ? sub.getType().trim() : "";
            if (type.equals("تطبيقي")) {
                int grade = resGrades.getOrDefault(sub.getId(), 0);
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
        Map<Integer, Integer> resGrades = resolveCompositeGrades(subjects, grades);
        StringBuilder sb = new StringBuilder();
        for (Subject sub : subjects) {
            if (sub.getParentSubjectId() != null) continue; // Only top-level failures
            
            int obtained = resGrades.getOrDefault(sub.getId(), 0);
            if (obtained < sub.getPassMark() && obtained >= 0) { 
                sb.append(sub.getName()).append("\n");
            }
        }
        return sb.toString().trim();
    }
    
    /**
     * Computes the maximum possible grade sum for the current subjects.
     * Only considers top-level subjects to avoid double-counting 30/70 parts.
     */
    public static int calculateMaxPossibleTotal(List<Subject> subjects) {
        int total = 0;
        for (Subject sub : subjects) {
            if (sub.getParentSubjectId() != null) continue; // Only top-level
            
            String name = sub.getName() != null ? sub.getName() : "";
            if (!name.contains("دين")) {
                total += sub.getMaxMark();
            }
        }
        return total;
    }
}
