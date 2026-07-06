@echo off
chcp 65001 > nul
set NLS_LANG=.AL32UTF8
cd /d "%~dp0"

echo ===============================================
echo   اعادة حساب حالة كل الطلاب (نجاح / رسوب / دور ثاني)
echo ===============================================
echo.

where sqlplus >nul 2>nul
if errorlevel 1 (
  echo [خطأ] لم يتم العثور على sqlplus في النظام.
  echo تأكد من تثبيت Oracle XE / Oracle Client.
  echo.
  pause
  exit /b 1
)

echo جاري الاتصال بقاعدة البيانات وتحديث الحالات...
echo.
sqlplus -S -L system/123@192.168.1.100:1521/XE @"%~dp0RecalcStatuses.sql"

echo.
echo تم. اقفل النافذة.
pause
