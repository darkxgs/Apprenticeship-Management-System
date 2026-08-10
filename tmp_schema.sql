SELECT column_name, data_type FROM all_tab_columns WHERE table_name IN ('SUBJECTS', 'PROFESSIONS', 'PROFESSIONAL_GROUPS') ORDER BY table_name, column_id;  
EXIT;  
