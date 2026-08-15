-- Check for discrepancies in profession names
SELECT 'Unique from Students: ' || COUNT(DISTINCT profession) FROM students;
SELECT 'Unique from Subjects: ' || COUNT(DISTINCT profession) FROM subjects;

-- Show example mismatch
SELECT DISTINCT s.profession AS student_prof, sub.profession AS subject_prof
FROM students s
LEFT JOIN subjects sub ON TRIM(s.profession) = TRIM(sub.profession)
WHERE sub.profession IS NULL AND ROWNUM <= 20;

-- Look at raw lengths to find hidden spaces
SELECT profession, LENGTH(profession) FROM (SELECT DISTINCT profession FROM students WHERE ROWNUM <= 5);
SELECT profession, LENGTH(profession) FROM (SELECT DISTINCT profession FROM subjects WHERE ROWNUM <= 5);

EXIT;
