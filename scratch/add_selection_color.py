import os
import glob

directory = r"c:\Users\Seif\Videos\Apprenticeship-Management-System-modernization\Apprenticeship-Management-System-modernization\src\main\java\com\pvtd\students\ui\pages\Report"

count = 0
for filepath in glob.glob(os.path.join(directory, "*.java")):
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    if "private void setupTableUi()" in content and "jTable1.setSelectionBackground" not in content:
        lines = content.split('\n')
        # find the jTable1 variable (or whatever table is used inside setupTableUi)
        start_idx = -1
        for i, line in enumerate(lines):
            if "private void setupTableUi()" in line:
                start_idx = i
                break
        
        if start_idx != -1:
            inserted = False
            for i in range(start_idx, min(len(lines), start_idx + 20)):
                # match the table variable
                if ".setRowHeight" in lines[i]:
                    table_name = lines[i].split(".setRowHeight")[0].strip()
                    indent = lines[i][:len(lines[i]) - len(lines[i].lstrip())]
                    
                    lines.insert(i+1, indent + table_name + '.setSelectionBackground(new java.awt.Color(153, 204, 255)); // Light blue selection')
                    lines.insert(i+2, indent + table_name + '.setSelectionForeground(java.awt.Color.BLACK);')
                    inserted = True
                    break
            
            if inserted:
                new_content = '\n'.join(lines)
                with open(filepath, "w", encoding="utf-8") as f:
                    f.write(new_content)
                count += 1
                print(f"Updated {os.path.basename(filepath)}")

print(f"Total updated: {count}")
