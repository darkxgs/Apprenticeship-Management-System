SELECT category, count(*), min(value), max(value) FROM system_dictionaries GROUP BY category;
SELECT count(*), min(name), max(name) FROM professions;
SELECT count(*), min(name), max(name) FROM regions;
SELECT count(*), min(name), max(name) FROM centers;
EXIT;
