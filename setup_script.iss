; Inno Setup Script for Apprenticeship Management System
; --------------------------------------------------

[Setup]
AppName=Apprenticeship Management System
AppVersion=1.0
DefaultDirName={pf}\ApprenticeshipSystem
DefaultGroupName=ApprenticeshipSystem
OutputDir=target\installer
OutputBaseFilename=ApprenticeshipSystemSetup
Compression=lzma
SolidCompression=yes
; Icon for the installer
; SetupIconFile=logo.ico 

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Main JAR and config
Source: "target\package\app.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "target\package\application.properties"; DestDir: "{app}"; Flags: ignoreversion
Source: "target\package\logo.jpg"; DestDir: "{app}"; Flags: ignoreversion
; Dependencies
Source: "target\package\libs\*"; DestDir: "{app}\libs"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\Apprenticeship Management System"; FileName: "javaw.exe"; Parameters: "-jar ""{app}\app.jar"""; WorkingDir: "{app}"
Name: "{commondesktop}\Apprenticeship Management System"; FileName: "javaw.exe"; Parameters: "-jar ""{app}\app.jar"""; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
Filename: "javaw.exe"; Parameters: "-jar ""{app}\app.jar"""; Description: "{cm:LaunchProgram,Apprenticeship Management System}"; Flags: nowait postinstall skipifsilent
