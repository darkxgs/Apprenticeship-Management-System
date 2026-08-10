ALTER TABLE professions ADD (exam_system VARCHAR2(100));  
ALTER TABLE professions MODIFY (name VARCHAR2(300));  
ALTER TABLE subjects MODIFY (name VARCHAR2(300));  
COMMIT;  
EXIT;  
