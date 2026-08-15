-- ============================================================
-- STEP 1: Clear all subjects-related data in correct order
-- ============================================================

-- Clear student grades (depend on subjects)
DELETE FROM student_grades;

-- Clear subjects
DELETE FROM subjects;

-- Clear specializations that were auto-created from professions
DELETE FROM specializations WHERE description = 'تم الإنشاء من بيانات المهن';

-- Clear professions
DELETE FROM professions;

-- Clear professional groups
DELETE FROM professional_groups;

COMMIT;

EXIT;
