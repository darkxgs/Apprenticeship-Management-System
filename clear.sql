DELETE FROM system_dictionaries WHERE category IN ('CENTER','PROFESSION','PROF_GROUP');
COMMIT;
SELECT 'Deleted: ' || COUNT(*) FROM system_dictionaries WHERE category IN ('CENTER','PROFESSION','PROF_GROUP');
EXIT;
