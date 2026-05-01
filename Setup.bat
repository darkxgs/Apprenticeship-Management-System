@echo off
setlocal enabledelayedexpansion
title Apprenticeship Management System Setup

echo ======================================================
echo    Apprenticeship Management System - Installation
echo ======================================================
echo.
echo This will install the application on your computer.
echo.

set "DEFAULT_INSTALL_DIR=%USERPROFILE%\ApprenticeshipSystem"
set /p "INSTALL_DIR=Enter installation folder [%DEFAULT_INSTALL_DIR%]: "
if "!INSTALL_DIR!"=="" set "INSTALL_DIR=%DEFAULT_INSTALL_DIR%"

echo.
echo Installing to: !INSTALL_DIR!
if not exist "!INSTALL_DIR!" mkdir "!INSTALL_DIR!"

echo Copying files...
xcopy /s /e /y "target\dist\ApprenticeshipSystem\*" "!INSTALL_DIR!\" >nul

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
