import pdfplumber
import pandas as pd
import glob
import os
import re

def reverse_arabic(text):
    if not text:
        return ""
    return str(text)[::-1]

def process_pdf_line(line):
    # Characters are placed backwards. Reversing the whole line puts words in the right order and numbers in the right direction
    reversed_line = line[::-1]
    
    # Extract consecutive tokens
    tokens = [t.strip() for t in reversed_line.split("  ") if t.strip()]
    if not tokens:
        return None
        
    return tokens

def extract_pdf_data(pdf_path):
    print(f"Processing PDF: {pdf_path}")
    all_data = []
    
    with pdfplumber.open(pdf_path) as pdf:
        for i, page in enumerate(pdf.pages):
            text = page.extract_text(layout=True)
            if not text:
                continue
                
            lines = text.split('\n')
            for line in lines:
                tokens = process_pdf_line(line)
                if not tokens:
                    continue
                
                # We identify a data row if it has a large sequence of numbers or follows a specific structure
                # A data row usually has many tokens (name, ids, grades)
                if len(tokens) > 10:
                    all_data.append(tokens)
                
    return all_data

def process_pdfs_to_excel():
    excel_columns = ["مسلسل", "الاسم", "رقم التسجيل", "الرقم القومى", "المنطقة", "اسم المركز", "المهنة", "النظام", "رقم الجلوس", "الرقم السرى", "المجموعة المهنية", "رقم التنسيق", "يوم", "شهر", "سنة", "النوع", "حي / قرية", "محافظة", "ديانة", "جنسية", "عنوان", "رقم قومي", "اخري", "pic"]
    final_df = pd.DataFrame(columns=excel_columns)
    
    for pdf_name in ["r_taksosy_nagh.pdf", "r_taksosy_rasb.pdf"]:
        if os.path.exists(pdf_name):
            data = extract_pdf_data(pdf_name)
            print(f"Found {len(data)} potential rows in {pdf_name}")
            for row in data:
                print(f"Data row: {row}")

if __name__ == "__main__":
    process_pdfs_to_excel()
