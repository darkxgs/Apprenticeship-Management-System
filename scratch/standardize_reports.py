import os
import re

directory = "src/main/java/com/pvtd/students/ui/pages/Report"
files = [
    "FailedFramePage.java",
    "DetailersFRamepage.java",
    "DeprivedFramePage.java",
    "delayedFramePage.java",
    "ApologeticFramePage.java",
    "absentFramePage.java",
    "SucssfullPageEdit.java"
]

def standardize_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find region combo name from loadRegions
    # Example: cmdcenter1.removeAllItems();
    region_match = re.search(r'public void loadRegions\(\) \{.*?(\w+)\.removeAllItems\(\);', content, re.DOTALL)
    if not region_match:
        print(f"Could not find region combo in {filepath}")
        return
    region_combo = region_match.group(1)

    # Find center combo name from loadCenters
    center_match = re.search(r'public void loadCenters\(String \w+\) \{.*?(\w+)\.removeAllItems\(\);', content, re.DOTALL)
    if not center_match:
        # Try finding in the second loadCenters if it exists or fallback
        center_match = re.search(r'(\w+)\.removeAllItems\(\);.*?addItem\("اختر المركز\.\.\."\)', content, re.DOTALL)
    
    if not center_match:
        print(f"Could not find center combo in {filepath}")
        return
    center_combo = center_match.group(1)

    print(f"File: {os.path.basename(filepath)} | Region: {region_combo} | Center: {center_combo}")

    # Standardized Bodies
    new_regions_body = f"""    public void loadRegions() {{
        {region_combo}.removeAllItems();
        {region_combo}.addItem("اختر المنطقة...");
        for (String r : com.pvtd.students.services.DictionaryService.getCombinedItems(com.pvtd.students.services.DictionaryService.CAT_REGION)) {{
            {region_combo}.addItem(r);
        }}
    }}"""

    new_centers_body = f"""    public void loadCenters(String region) {{
        {center_combo}.removeAllItems();
        {center_combo}.addItem("اختر المركز...");
        java.util.Map<String, String> centers = com.pvtd.students.services.StudentService.getCentersByRegionWithCodes(region);
        for (String c : centers.keySet()) {{
            {center_combo}.addItem(c);
        }}
    }}"""

    # Replace loadRegions
    content = re.sub(r'public void loadRegions\(\) \{.*?\}', new_regions_body, content, flags=re.DOTALL)
    
    # Replace loadCenters (might be multiple, replace all with the same standardized one)
    content = re.sub(r'public void loadCenters\(String \w+\) \{.*?\}', new_centers_body, content, flags=re.DOTALL)

    # Specific fix for SucssfullPageEdit duplicate loadStudents
    if "SucssfullPageEdit.java" in filepath:
        # Already cleaned up mostly, but let's ensure it's tight
        pass

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for filename in files:
    standardize_file(os.path.join(directory, filename))
