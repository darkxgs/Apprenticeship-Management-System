import pandas as pd
import numpy as np
import traceback

try:
    df = pd.read_excel('Data.xlsx')

    # Convert column to string/object to avoid TypeError
    df['الرقم القومى'] = df['الرقم القومى'].astype(str)
    
    # Assign Test Data for Row 1
    df.loc[0, 'الرقم القومى'] = '30405242112357'
    df.loc[0, 'الاسم'] = 'أحمد علي محمد'
    
    # Assign Test Data for Row 2
    if len(df) > 1:
        df.loc[1, 'الرقم القومى'] = '30611180298742'
        df.loc[1, 'الاسم'] = 'سارة أحمد محمود'

    # Auto-fillable fields blanking
    cols_to_blank = ['يوم', 'شهر', 'سنة', 'النوع', 'محافظة', 'الرقم السرى']
    
    for c in cols_to_blank:
        if c in df.columns:
            df.loc[0, c] = np.nan
            if len(df) > 1: df.loc[1, c] = np.nan

    df.head(2).to_excel('Test_Data.xlsx', index=False)
except Exception as e:
    with open('error.txt', 'w', encoding='utf-8') as f:
        f.write(traceback.format_exc())
