import pandas as pd
import numpy as np

print("====================================")
print("   Starting Data Extraction...      ")
print("====================================")

# ---------------------------------------------------------
# 1. Processing Centers (اسماء المراكز والمحطات)
# ---------------------------------------------------------
try:
    print("\nReading Centers...")
    # Skip the first 6 rows which contain titles like "وزارة الصناعة" and "مصلحة الكفاية"
    df_centers = pd.read_excel('اسماء المراكز والمحطات.xlsx', skiprows=5)
    
    # The columns should ideally be [م, المركز / المحطة]
    # We rename them to be safe
    df_centers.columns = ['ID', 'Center_Name']
    
    # Remove empty rows or rows where Center_Name is NaN
    df_centers = df_centers.dropna(subset=['Center_Name'])
    
    # Remove rows where Center_Name is just "المركز / المحطة" (the header itself)
    df_centers = df_centers[df_centers['Center_Name'] != 'المركز / المحطة']
    df_centers = df_centers[df_centers['Center_Name'].str.strip() != '']
    
    # Save to a clean CSV
    df_centers.to_csv('cleaned_centers.csv', index=False, encoding='utf-8-sig')
    print(f"✅ Success! Extracted {len(df_centers)} centers.")
    print("   Saved to: cleaned_centers.csv")
    
except Exception as e:
    print("❌ Error processing centers:", e)

# ---------------------------------------------------------
# 2. Processing Subjects (المواد الدراسية 25-26دبلوم.xlsx)
# ---------------------------------------------------------
try:
    print("\nReading Subjects...")
    # We use engine='xlrd' because the file is an old XLS format 
    # Skipping the first 3 rows to avoid titles/logos
    df_subjects = pd.read_excel('المواد الدراسية 25-26دبلوم.xlsx', engine='xlrd', skiprows=3)
    
    # We save it as a flat Excel file for the user to review before importing
    df_subjects.to_excel('cleaned_subjects_raw.xlsx', index=False)
    
    print(f"✅ Success! Flattened subjects data.")
    print("   Saved to: cleaned_subjects_raw.xlsx")
    
    # Let's also print out the columns so the user can see them
    print("   Found Columns:")
    for col in df_subjects.columns:
        print(f"      - {col}")
        
except Exception as e:
    print("❌ Error processing subjects:", e)

print("\n====================================")
print(" Extraction Script Finished! ")
print("====================================")
