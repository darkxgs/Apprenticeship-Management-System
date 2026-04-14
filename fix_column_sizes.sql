-- ============================================================
-- STEP 2: Fix column sizes to handle long Arabic names
-- ============================================================

-- subjects.name: bump to 300 to be safe
ALTER TABLE subjects MODIFY (name VARCHAR2(300));

-- subjects.profession: bump to 300
ALTER TABLE subjects MODIFY (profession VARCHAR2(300));

-- specializations.name: bump to 300
ALTER TABLE specializations MODIFY (name VARCHAR2(300));

-- professions.name: already 100 by default, bump to 300
ALTER TABLE professions MODIFY (name VARCHAR2(300));

-- professional_groups.name: bump to 300
ALTER TABLE professional_groups MODIFY (name VARCHAR2(300));

COMMIT;

EXIT;
