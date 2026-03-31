SELECT 'Centers: ' || COUNT(*) AS result FROM system_dictionaries WHERE category='CENTER';
SELECT 'Professions: ' || COUNT(*) AS result FROM system_dictionaries WHERE category='PROFESSION';
SELECT 'ProfGroups: ' || COUNT(*) AS result FROM system_dictionaries WHERE category='PROF_GROUP';
SELECT 'Subjects: ' || COUNT(*) AS result FROM subjects;
EXIT;
