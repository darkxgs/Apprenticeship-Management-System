import os
import re

files_to_check = [
    r"c:\Users\Seif\Videos\Apprenticeship-Management-System-main (1)\Apprenticeship-Management-System-modernization\src\main\java\com\pvtd\students\ui\pages\Report\gradReportSequential.java",
    r"c:\Users\Seif\Videos\Apprenticeship-Management-System-main (1)\Apprenticeship-Management-System-modernization\src\main\java\com\pvtd\students\ui\pages\Report\gradReportSucc.java",
    r"c:\Users\Seif\Videos\Apprenticeship-Management-System-main (1)\Apprenticeship-Management-System-modernization\src\main\java\com\pvtd\students\ui\pages\Report\gradReportFail.java"
]

for file_path in files_to_check:
    if not os.path.exists(file_path):
        continue
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # We want to find the titles array in buildFooter and reverse it.
    # Pattern to match the titles array
    pattern = r'(String\[\]\s+titles\s*=\s*\{)(.*?)(\};)'
    
    match = re.search(pattern, content, re.DOTALL)
    if match:
        prefix = match.group(1)
        items_str = match.group(2)
        suffix = match.group(3)
        
        # Extract items
        items = [item.strip() for item in items_str.split(',') if item.strip()]
        
        # Check if it's already reversed by looking at the first item
        if "كتبه" not in items[0]:
            items.reverse()
            # Reconstruct the array
            new_items_str = "\n            " + ",\n            ".join(items) + "\n        "
            new_content = content[:match.start()] + prefix + new_items_str + suffix + content[match.end():]
            
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Reversed footer titles in {os.path.basename(file_path)}")
        else:
            print(f"Already reversed in {os.path.basename(file_path)}")

