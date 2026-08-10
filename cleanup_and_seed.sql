TRUNCATE TABLE subjects;
COMMIT;
-- Re-run the clean seed
@seed_subjects.sql
EXIT;
