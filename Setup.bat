@echo off
setlocal enabledelayedexpansion
title Apprenticeship Management System Setup

echo ======================================================
echo    Apprenticeship Management System - Installation
echo ======================================================
echo.
echo This will install the application on your computer.
echo.

set "DEFAULT_INSTALL_DIR=%~dp0application"
set /p "INSTALL_DIR=Enter installation folder [%DEFAULT_INSTALL_DIR%]: "
if "!INSTALL_DIR!"=="" set "INSTALL_DIR=%DEFAULT_INSTALL_DIR%"

echo.
echo Installing to: !INSTALL_DIR!
if not exist "!INSTALL_DIR!" mkdir "!INSTALL_DIR!"

if not exist "target\dist\ApprenticeshipSystem\" (
    echo.
    echo ERROR: Built application not found in target\dist\ApprenticeshipSystem
    echo Please run 'package_app.bat' first to build the application.
    echo.
    pause
    exit /b 1
)

echo Copying application files...
xcopy /s /e /y "target\dist\ApprenticeshipSystem\*" "!INSTALL_DIR!\" >nul

echo Copying data folders...
if exist "التقارير" (
    echo Copying التقارير...
    xcopy /s /e /y "التقارير\*" "!INSTALL_DIR!\التقارير\" >nul
)
if exist "students_images" (
    echo Copying students_images...
    xcopy /s /e /y "students_images\*" "!INSTALL_DIR!\students_images\" >nul
)

echo Creating shortcuts...
set "SC_PATH=%INSTALL_DIR%\ApprenticeshipSystem.exe"
set "DESKTOP_PATH=%USERPROFILE%\Desktop\Apprenticeship System.lnk"

:: Create VBScript to create shortcut
echo Set oWS = WScript.CreateObject("WScript.Shell") > "%temp%\shortcut.vbs"
echo sLinkFile = "%DESKTOP_PATH%" >> "%temp%\shortcut.vbs"
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> "%temp%\shortcut.vbs"
echo oLink.TargetPath = "%SC_PATH%" >> "%temp%\shortcut.vbs"
echo oLink.WorkingDirectory = "%INSTALL_DIR%" >> "%temp%\shortcut.vbs"
echo oLink.Description = "Apprenticeship Management System" >> "%temp%\shortcut.vbs"
echo oLink.Save >> "%temp%\shortcut.vbs"

cscript /nologo "%temp%\shortcut.vbs"
del "%temp%\shortcut.vbs"

echo.
echo ======================================================
echo            Installation Complete Successfully!
echo ======================================================
echo You can find the application on your Desktop.
echo.
pause
