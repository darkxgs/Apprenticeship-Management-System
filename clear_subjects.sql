DELETE FROM subjects;
COMMIT;
SELECT 'Subjects after clear: ' || COUNT(*) FROM subjects;
EXIT;
