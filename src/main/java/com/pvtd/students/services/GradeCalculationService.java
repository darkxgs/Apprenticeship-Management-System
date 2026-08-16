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

        // A composite parent's grade is ALWAYS the sum of its children.
        // We compute each parent's total from its children and OVERWRITE it (rather than
        // add to whatever the parent already holds). This keeps the method idempotent:
        // calling it on already-resolved grades, or on grades that contain a stale/stored
        // parent row from the DB, yields the same result instead of double-counting
        // (e.g. stored parent 70 + children (70+0) -> 140).
        Map<Integer, Integer> parentSums = new java.util.HashMap<>();
        for (Subject sub : subjects) {
            if (sub.getParentSubjectId() != null) {
                int parentId = sub.getParentSubjectId();
                int childGrade = grades.getOrDefault(sub.getId(), 0);
                parentSums.merge(parentId, childGrade, Integer::sum);
            }
        }
        resolved.putAll(parentSums);
        return resolved;
    }

    /**
     * تطبيق رفع درجات الرأفة (تعليمات مكتب الامتحانات):
     * لأي مادة، إذا كانت الدرجة المتحصلة أقل من درجة النجاح وكان الفارق
     * (pass - obtained) لا يتجاوز (max / 20) بالقسمة الصحيحة، تُرفع الدرجة إلى درجة النجاح.
     * مثال: نهاية 100 ونجاح 50 → تُرفع 45..49 إلى 50، ونهاية 50 ونجاح 25 → تُرفع 23..24 إلى 25.
     *
     * The band is decided per TOP-LEVEL subject. For composite subjects (30/70 parts)
     * it is evaluated on the summed children values, and the deficit is added to the
     * graded child with the largest max mark so the total reaches the pass mark.
     * Negative marks are status marker codes (غائب...) and are never touched.
     * Returns a NEW adjusted map; the input map is not modified. Idempotent:
     * a grade at or above pass is never inside the band, so re-applying changes nothing.
     */
    public static Map<Integer, Integer> applyMercyRaises(List<Subject> subjects, Map<Integer, Integer> grades) {
        if (grades == null) return null;
        Map<Integer, Integer> adjusted = new java.util.HashMap<>(grades);
        if (subjects == null || subjects.isEmpty() || grades.isEmpty()) return adjusted;

        // Group children under their parent (same style as resolveCompositeGrades).
        Map<Integer, List<Subject>> childrenByParent = new java.util.HashMap<>();
        for (Subject sub : subjects) {
            if (sub.getParentSubjectId() != null) {
                childrenByParent.computeIfAbsent(sub.getParentSubjectId(), k -> new java.util.ArrayList<>()).add(sub);
            }
        }

        for (Subject sub : subjects) {
            if (sub.getParentSubjectId() != null) continue; // mercy is decided per top-level subject

            List<Subject> children = childrenByParent.get(sub.getId());
            if (children != null && !children.isEmpty()) {
                // Composite subject: evaluate the band on the summed children values.
                int effObtained = 0, effPass = 0, effMax = 0;
                boolean hasGrade = false, hasMarkerCode = false;
                Subject raiseTarget = null; // graded child with the largest max mark
                for (Subject child : children) {
                    effPass += child.getPassMark();
                    effMax += child.getMaxMark();
                    Integer g = grades.get(child.getId());
                    if (g == null) continue;
                    hasGrade = true;
                    if (g < 0) hasMarkerCode = true;
                    effObtained += g;
                    if (raiseTarget == null || child.getMaxMark() > raiseTarget.getMaxMark()) {
                        raiseTarget = child;
                    }
                }
                if (!hasGrade || hasMarkerCode) continue; // no marks entered, or غائب-style marker code

                int deficit = effPass - effObtained;
                if (deficit > 0 && deficit <= effMax / 20) {
                    adjusted.merge(raiseTarget.getId(), deficit, Integer::sum);
                    // Keep a stored/resolved parent entry consistent with the new children sum.
                    if (adjusted.containsKey(sub.getId())) {
                        adjusted.put(sub.getId(), effObtained + deficit);
                    }
                }
            } else {
                Integer obtained = grades.get(sub.getId());
                if (obtained == null || obtained < 0) continue; // no entry / marker code — never touch

                int deficit = sub.getPassMark() - obtained;
                if (deficit > 0 && deficit <= sub.getMaxMark() / 20) {
                    adjusted.put(sub.getId(), sub.getPassMark());
                }
            }
        }
        return adjusted;
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
