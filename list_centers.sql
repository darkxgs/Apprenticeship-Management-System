SET PAGESIZE 100;  
SELECT c.name as center, r.name as region FROM centers c JOIN regions r ON c.region_id = r.id;  
EXIT;  
