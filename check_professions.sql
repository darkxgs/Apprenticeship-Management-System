SELECT 'From Dictionaries: ' || COUNT(*) FROM system_dictionaries WHERE category='PROFESSION';
SELECT 'From Students: ' || COUNT(DISTINCT profession) FROM students;
-- Show subjects that are in students but not in dictionary
SELECT DISTINCT profession FROM students 
MINUS 
SELECT value FROM system_dictionaries WHERE category='PROFESSION';
EXIT;
