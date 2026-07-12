@echo off
setlocal enabledelayedexpansion

:: Auto-detect the newest installed JDK (picks the last jdk-* folder alphabetically)
set "JAVA_HOME="
for /d %%D in ("C:\Program Files\Java\jdk-*") do set "JAVA_HOME=%%D"
if not defined JAVA_HOME (
    echo ERROR: No JDK found under "C:\Program Files\Java". Please install a JDK.
    pause
    exit /b 1
)
echo Using JDK: %JAVA_HOME%
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [1/4] Cleaning old build files...
if exist target rmdir /s /q target
mkdir target\classes
mkdir target\libs
mkdir target\package
mkdir target\package\libs

echo [2/4] Compiling Java source files...
call compile.bat

echo [3/4] Preparing package contents...
:: Copy classes and resources
xcopy /s /e src\main\resources\* target\classes\ >nul
:: Create the fat-like JAR (main app)
jar --create --file target\package\app.jar --main-class com.pvtd.students.MainApp -C target\classes .

:: Copy dependencies
echo [3/4] Copying dependencies...
powershell -Command "$cp = [System.Environment]::GetEnvironmentVariable('CP', 'Process'); if ($cp) { $cp.Split(';') | ForEach-Object { if ($_ -and (Test-Path $_)) { Copy-Item $_ target\package\libs\ -ErrorAction SilentlyContinue } } }"

:: Copy configuration
copy application.properties target\package\ >nul
copy logo.jpg target\package\ >nul

echo [4/4] Creating Windows Application Image...
:: Using app-image as it is most reliable without WiX
jpackage ^
  --type app-image ^
  --name "ApprenticeshipSystem" ^
  --input target\package ^
  --main-jar app.jar ^
  --main-class com.pvtd.students.MainApp ^
  --dest target\dist

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS! Standalone application created in target\dist\ApprenticeshipSystem
    echo You can run it using ApprenticeshipSystem.exe inside that folder.
    echo.
    echo Note: Ensure you include the 'Ø§Ù„ØªÙ‚Ø§Ø±ÙŠØ±' and 'students_images' folders 
    echo next to the EXE if the app needs to read existing data.
) else (
    echo.
    echo FAILED! Check the error messages above.
)
pause
