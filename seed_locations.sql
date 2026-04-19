-- Regions and Centers Seeding Script
-- Generated from '«”„«¡ «·„—«ﬂ“ Ê«·„Õÿ« .xlsx'

-- Region: «·«„Ì—Ì…
MERGE INTO regions r USING (SELECT '«·«„Ì—Ì…' n, '1' c FROM DUAL) src 
ON (r.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c 
WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.c);

MERGE INTO centers c USING (SELECT '«·√„Ì—Ì… «· Ã—Ì»Ï' n, '1' c, (SELECT id FROM regions WHERE name = '«·«„Ì—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);

-- Region: ‘„«· «·ﬁ«Â—…
MERGE INTO regions r USING (SELECT '‘„«· «·ﬁ«Â—…' n, '2' c FROM DUAL) src 
ON (r.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c 
WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.c);

MERGE INTO centers c USING (SELECT '„⁄«œ‰ ‘»—« «·ŒÌ„Â' n, '1' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '‘»—« ··’‰«⁄«  «·‰”ÌÃÌ… Ê«·Â‰œ”Ì…' n, '2' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„‘ —ﬂ «·„Ÿ·« ' n, '3' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '’Ì«‰… «·”Ì«—«  »‘»—«' n, '4' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„⁄«œ‰ Ê »—Ìœ ‘»—«' n, '5' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„‘ —ﬂ «·⁄»«”Ì…' n, '6' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·„ÿ—Ì…' n, '7' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / „Ã·” «·œ›«⁄' n, '8' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·„’—Ì… ··Õ«”»«  »‘»—« «·ŒÌ„…' n, '9' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «»Ê “⁄»· ··√”„œ…' n, '10' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·›«—”' n, '11' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «œ›«‰”' n, '12' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / Â«—œÊÌ—' n, '13' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·»«‘«' n, '14' c, (SELECT id FROM regions WHERE name = '‘„«· «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);

-- Region: Ã‰Ê» «·ﬁ«Â—…
MERGE INTO regions r USING (SELECT 'Ã‰Ê» «·ﬁ«Â—…' n, '3' c FROM DUAL) src 
ON (r.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c 
WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.c);

MERGE INTO centers c USING (SELECT '«·√·«  «·œﬁÌﬁ… »«·ﬁ«Â—…' n, '1' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '“Â—«¡ Õ·Ê«‰' n, '2' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„⁄«œ‰ Ê ”Ì«—«  Ê«œÏ ÕÊ›' n, '3' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT 'ÿ—… «·√”„‰ ' n, '4' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„—ﬂ“ «·√”„—« ' n, '5' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„’—Ì… ··Õ«”»«  ( ›—⁄ «·„⁄«œÏ )' n, '6' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·‘—Êﬁ' n, '7' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·ﬁ«Â—…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);

-- Region: «·ÃÌ“…
MERGE INTO regions r USING (SELECT '«·ÃÌ“…' n, '4' c FROM DUAL) src 
ON (r.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c 
WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.c);

MERGE INTO centers c USING (SELECT '”Ì«—«  «„»«»…' n, '1' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT 'ÿ»«⁄… «„»«»…' n, '2' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„‰Ì· ‘ÌÕ…' n, '3' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Ã„⁄ «·ÕÊ«„œÌ…' n, '4' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ…/ «·‰»√ «·Êÿ‰Ï ··‰‘—' n, '5' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / »«ÌÊ‰Ì—“ «·œÊ·Ì… ··Õ«”»«  ( 1 )' n, '6' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ…/ œ«— «· ⁄«Ê‰ ··ÿ»«⁄…' n, '7' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·‘—ﬂ… «·⁄«·„Ì… ·’‰«⁄… «·„·«»” (  Ìœ ·«„Ê‰œ )' n, '8' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT 'œ«— «·—Õ„‰' n, '9' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / ‰Â÷… „’—' n, '10' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·„” ﬁ»· ( 1 )' n, '11' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·„” ﬁ»· ( 2 )' n, '12' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / ‰  ”ﬂÌ·1' n, '13' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / ‰  ”ﬂÌ·2' n, '14' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·‘—Êﬁ ( Õœ«∆ﬁ «·«Â—«„ )' n, '15' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·ﬂ„«·  —‰œ ·Ì‰ﬂ' n, '16' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ…/ «·›‰Ì… «·ÕœÌÀ… ··ÿ»«⁄…' n, '17' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·Ì«”„Ì‰« ··„·«»” «·Ã«Â“…' n, '18' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / √„ √› ”Ï' n, '19' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '√„«‰ ”Ì› Ï' n, '20' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·‰’— ··„”»Êﬂ« ' n, '21' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Ã„Ê⁄… «·√”ﬂ‰œ—Ì… «·’‰«⁄Ì… ( ›—⁄ Õœ«∆ﬁ «·√Â—«„ )' n, '22' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Ã„Ê⁄… «·√”ﬂ‰œ—Ì… «·’‰«⁄Ì… ( ›—⁄ ﬂ—œ«”… )' n, '23' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '»«ÌÊ‰Ì—“ «·œÊ·Ì… ··Õ«”»«  ( 2 )' n, '24' c, (SELECT id FROM regions WHERE name = '«·ÃÌ“…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);

-- Region: ‘„«· «·’⁄Ìœ
MERGE INTO regions r USING (SELECT '‘„«· «·’⁄Ìœ' n, '5' c FROM DUAL) src 
ON (r.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c 
WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.c);

MERGE INTO centers c USING (SELECT '«·„‰Ì«' n, '1' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '»‰Ï ”ÊÌ›' n, '2' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·’›' n, '3' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT 'ﬂÊ„ √Ê‘Ì„ - «·›ÌÊ„' n, '4' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„’‰⁄ ”„«—  ·· ÿÊÌ— ( «·„‰Ì« √»Ê ›Ì·Ê )' n, '5' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„’‰⁄ ”„«—  ·· ÿÊÌ— ( »‰Ï „“«—)' n, '6' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„’‰⁄ ”„«—  ·· ÿÊÌ— («·„‰Ì« «·ÃœÌœ… )' n, '7' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„’‰⁄ ”„«—  ·· ÿÊÌ— ( »‰œ— „·ÊÏ )' n, '8' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·‘—ﬂ… «·œÊ·Ì… · ’‰Ì⁄ «·„·«»” «·Ã«Â“…' n, '9' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·’«‰⁄ «·√Ê·' n, '10' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / ÿÌ»…  ﬂ‰Ê·ÊÃÏ' n, '11' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·—«⁄Ï «·’«·Õ' n, '12' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ…/ ›ÌÊ ‘—' n, '13' c, (SELECT id FROM regions WHERE name = '‘„«· «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);

-- Region: «·ﬁ‰«… Ê«·⁄«‘—
MERGE INTO regions r USING (SELECT '«·ﬁ‰«… Ê«·⁄«‘—' n, '6' c FROM DUAL) src 
ON (r.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c 
WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.c);

MERGE INTO centers c USING (SELECT '«·⁄«‘—' n, '1' c, (SELECT id FROM regions WHERE name = '«·ﬁ‰«… Ê«·⁄«‘—') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '»Ê— ”⁄Ìœ' n, '2' c, (SELECT id FROM regions WHERE name = '«·ﬁ‰«… Ê«·⁄«‘—') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·”ÊÌ”' n, '3' c, (SELECT id FROM regions WHERE name = '«·ﬁ‰«… Ê«·⁄«‘—') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·“ﬁ«“Ìﬁ' n, '4' c, (SELECT id FROM regions WHERE name = '«·ﬁ‰«… Ê«·⁄«‘—') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ…/ „’— «·’‰«⁄Ì…' n, '5' c, (SELECT id FROM regions WHERE name = '«·ﬁ‰«… Ê«·⁄«‘—') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ…/  «·”ÊÌœÏ' n, '6' c, (SELECT id FROM regions WHERE name = '«·ﬁ‰«… Ê«·⁄«‘—') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·«ﬂ«œÌ„Ì… «· ﬂ‰Ê·ÊÃÌ… «·„ ﬁœ„…' n, '7' c, (SELECT id FROM regions WHERE name = '«·ﬁ‰«… Ê«·⁄«‘—') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ…/ ﬂÊ‰”· ‰Ã «· ﬂ‰Ê·ÊÃÌ…' n, '8' c, (SELECT id FROM regions WHERE name = '«·ﬁ‰«… Ê«·⁄«‘—') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·⁄·Ê„ «·»Õ—Ì…' n, '9' c, (SELECT id FROM regions WHERE name = '«·ﬁ‰«… Ê«·⁄«‘—') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / ”Ìﬂ„ ( «Ì“Ì” )' n, '10' c, (SELECT id FROM regions WHERE name = '«·ﬁ‰«… Ê«·⁄«‘—') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);

-- Region: «·ÊÃÂ «·»Õ—Ï
MERGE INTO regions r USING (SELECT '«·ÊÃÂ «·»Õ—Ï' n, '7' c FROM DUAL) src 
ON (r.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c 
WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.c);

MERGE INTO centers c USING (SELECT 'ÿ‰ÿ«' n, '1' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„‘ —ﬂ «·„‰’Ê—…' n, '2' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Ã„⁄ ﬂ›— «·“Ì« ' n, '3' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '”Ì«—«  ﬂ›— «·“Ì« ' n, '4' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT 'ﬂ›— «·‘ÌŒ' n, '5' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„⁄«œ‰ œ„Ì«ÿ' n, '6' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„Õ·…' n, '7' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «Ì—  ﬂ‰Ê·Ê·ÃÏ1 (ÿ‰ÿ«. ”⁄œ “€·Ê· )' n, '8' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«· ﬂ‰Ê·ÊÃÌ… «·’‰«⁄Ì… «·ÕœÌÀ… ( «·”Ã«⁄Ï )' n, '9' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / ÂÊﬂ ”»Ê— 1' n, '10' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / “Â—… «·„Õ·…' n, '11' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„’‰⁄ «·√Ê—»Ï ( ›—⁄ œ„Ì«ÿ «·ÃœÌœ… )' n, '12' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„’‰⁄ «·√Ê—»Ï ( «·»’«Ì·… )' n, '13' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / ”„«œ ÿ·Œ«' n, '14' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / »Ì·« ﬂ›— «·‘ÌŒ' n, '15' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„’‰⁄ ›ÌÊÃÌ— «Ê» ﬂ”  ﬂ‰Ê·ÊÃÏ' n, '16' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„’—Ì… ··Õ«”»«  ( œ„Ì«ÿ «·—∆Ì”Ï)' n, '17' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„’—Ì… ··Õ«”»«  ( «·—Ê÷…)' n, '18' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„’—Ì… ··Õ«”»«  ( ‰ÊÌ·“)' n, '19' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·—Õ„… ( œ„Ì«ÿ «·ÃœÌœ… )' n, '20' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·„Ê‰«·Ì“«' n, '21' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / ›«Ì› œÏ ( 5D' n, '22' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·‰Œ»…' n, '23' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·—Ì«œ…' n, '24' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / ÃÌ· «·„” ﬁ»·' n, '25' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ…/ Â«‰Ê' n, '26' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / «·„‰«—' n, '27' c, (SELECT id FROM regions WHERE name = '«·ÊÃÂ «·»Õ—Ï') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);

-- Region: Ã‰Ê» «·’⁄Ìœ
MERGE INTO regions r USING (SELECT 'Ã‰Ê» «·’⁄Ìœ' n, '8' c FROM DUAL) src 
ON (r.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c 
WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.c);

MERGE INTO centers c USING (SELECT '„⁄«œ‰ ﬁ‰«' n, '1' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT ' ⁄œÌ‰ ﬁ‰«' n, '2' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT 'ﬂÊ„ «„»Ê' n, '3' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«”Ê«‰' n, '4' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«”ÌÊÿ' n, '5' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·œÊ·Ì… ··⁄·Ê„ «·»Õ—Ì… Ê«· ’‰Ì⁄ Ê«·≈‰ «Ã' n, '7' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„—ﬂ“ ”ÊÂ«Ã' n, '8' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„’—Ì… ··Õ«”»«  »«·€—œﬁ…' n, '9' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '”·„«‰ »«·€—œﬁ…' n, '10' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·√Ê—»Ì ›—⁄ «·€—œﬁ…' n, '11' c, (SELECT id FROM regions WHERE name = 'Ã‰Ê» «·’⁄Ìœ') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);

-- Region: €—» «·«”ﬂ‰œ—Ì…
MERGE INTO regions r USING (SELECT '€—» «·«”ﬂ‰œ—Ì…' n, '9' c FROM DUAL) src 
ON (r.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c 
WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.c);

MERGE INTO centers c USING (SELECT '„⁄«œ‰ „Õ—„' n, '1' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT 'ﬂÂ—»«¡ „Õ—„' n, '2' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '”Ì«—«  „Õ—„' n, '3' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT 'ÿ»«⁄… „Õ—„' n, '4' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„’— «·⁄«„—Ì… ··€“·' n, '5' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„⁄Âœ «·›‰Ï ··⁄·Ê„ Ê«· ﬂ‰Ê·ÊÃÌ« („Õÿ… „Â—«‰ ‹ »—Ã «·⁄—» )' n, '6' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·›‰Ï „—«œ ( «„ « ‘ Ã—Ê» ··√·ﬂ —Ê‰Ì«  )' n, '7' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT 'ÿÌ»… ( 1 )' n, '8' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·“Â—«¡ «·’‰«⁄Ì…' n, '9' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '»ÌÊ ﬂ ··√ÃÂ“… «·ÿ»Ì…' n, '10' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT ' Ê— ‰ÌÊ tit ( «·»Ìÿ«‘ )' n, '11' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„’‰⁄  Ê— ‰ÌÊ tit (€Ìÿ «·⁄‰» )' n, '12' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·‘Â«» ··„·«»” «·Ã«Â“… ( «·⁄«„—Ì… )' n, '13' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·‘Â«» ··„·«»” «·Ã«Â“… ( «·⁄Ã„Ï)' n, '14' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '’ﬁ— ‘«ÂÌ‰ «·⁄Ã„Ï' n, '15' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„ﬂ… «·„ﬂ—„…' n, '16' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„—Ì„ ”«»« »«‘«' n, '17' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„—Ì„ „Õ—„ »ﬂ' n, '18' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·„—Ì„ ’·«Õ «·œÌ‰' n, '19' c, (SELECT id FROM regions WHERE name = '€—» «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);

-- Region: ‘—ﬁ «·«”ﬂ‰œ—Ì…
MERGE INTO regions r USING (SELECT '‘—ﬁ «·«”ﬂ‰œ—Ì…' n, '10' c FROM DUAL) src 
ON (r.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c 
WHEN NOT MATCHED THEN INSERT (name, code) VALUES (src.n, src.c);

MERGE INTO centers c USING (SELECT '«·¬·«  «·œﬁÌﬁ… »«·√”ﬂ‰œ—Ì…' n, '1' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„⁄«œ‰ ›Ìﬂ Ê—Ì«' n, '2' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT 'ﬂÂ—»«¡ ›Ìﬂ Ê—Ì«' n, '3' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·‰Â÷… ··€“· Ê«·‰”ÌÃ Ê«· —ÌﬂÊ' n, '4' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '»Ì‰ﬂ ﬁÿ‰ ··„·«»” «·Ã«Â“…' n, '5' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·‘—Êﬁ' n, '6' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Ã„Ê⁄… «·√”ﬂ‰œ—Ì… «·’‰«⁄Ì…' n, '7' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '«·⁄—»Ì… ··€“· Ê«·‰”ÌÃ' n, '8' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / „Â—«‰ «·”ÌÊ›' n, '9' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / „Â—«‰ «·«’·«Õ' n, '10' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / ›ÌÊ ‘—' n, '11' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '„Õÿ… / ·Ê“«‰' n, '12' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '”Ìœ  ﬂ”' n, '13' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);
MERGE INTO centers c USING (SELECT '’ﬁ— ‘«ÂÌ‰ «·⁄Ê«Ìœ' n, '14' c, (SELECT id FROM regions WHERE name = '‘—ﬁ «·«”ﬂ‰œ—Ì…') rid FROM DUAL) src 
ON (c.name = src.n) 
WHEN MATCHED THEN UPDATE SET code = src.c, region_id = src.rid 
WHEN NOT MATCHED THEN INSERT (name, code, region_id) VALUES (src.n, src.c, src.rid);

COMMIT;