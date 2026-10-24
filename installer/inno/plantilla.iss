; Instalador de Plantilla Offline (Windows 10/11 de 64 bits)
#define AppName "Plantilla Offline"
#define AppVersion "1.0.0"

[Setup]
; Genera tu propio GUID en el IDE de Inno: Tools > Generate GUID. No lo cambies entre versiones.
AppId={{REEMPLAZA-ESTE-GUID}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher=Tu Empresa
; Ruta fija y sin espacios: los scripts de WildFly se llevan mal con espacios.
DefaultDirName=C:\PlantillaOffline
DisableDirPage=yes
DisableProgramGroupPage=yes
PrivilegesRequired=admin
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
OutputDir=..\output
OutputBaseFilename=PlantillaOffline-Setup-{#AppVersion}
; Los ZIP y el EXE de PostgreSQL ya vienen comprimidos: comprimir más solo hace lento el build.
Compression=lzma2/fast
SolidCompression=no
WizardStyle=modern
UninstallDisplayName={#AppName}

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Files]
; Material de instalación: va a una carpeta temporal y se borra al terminar.
Source: "..\payload\*"; DestDir: "{tmp}\plantilla\payload"; Flags: deleteafterinstall ignoreversion recursesubdirs
Source: "..\jboss\*"; DestDir: "{tmp}\plantilla\jboss"; Flags: deleteafterinstall ignoreversion
Source: "..\scripts\install.ps1"; DestDir: "{tmp}\plantilla\scripts"; Flags: deleteafterinstall ignoreversion
; El desinstalador sí se queda en la instalación.
Source: "..\scripts\uninstall.ps1"; DestDir: "{app}\scripts"; Flags: ignoreversion

[INI]
Filename: "{autodesktop}\{#AppName}.url"; Section: "InternetShortcut"; Key: "URL"; String: "https://localhost:8443/plantilla-api/"

[UninstallDelete]
Type: files; Name: "{autodesktop}\{#AppName}.url"

[Run]
; Las credenciales están en un archivo con permisos restringidos; se abre con permisos de administrador.
Filename: "notepad.exe"; Parameters: """{app}\install-info.txt"""; Description: "Ver usuario y contraseña del administrador"; Flags: postinstall skipifsilent runascurrentuser unchecked

[Code]
procedure CurStepChanged(CurStep: TSetupStep);
var
  ResultCode: Integer;
  Script, Cmd: String;
begin
  if CurStep = ssPostInstall then
  begin
    WizardForm.StatusLabel.Caption :=
      'Instalando servidor y base de datos. Puede tardar varios minutos...';
    Script := ExpandConstant('{tmp}\plantilla\scripts\install.ps1');
    Cmd := '/C ""powershell.exe" -NoProfile -ExecutionPolicy Bypass -File "' + Script +
           '" -InstallDir "' + ExpandConstant('{app}') + '" > "' +
           ExpandConstant('{app}\install.log') + '" 2>&1"';
    if (not Exec(ExpandConstant('{cmd}'), Cmd, '', SW_HIDE, ewWaitUntilTerminated, ResultCode))
       or (ResultCode <> 0) then
    begin
      MsgBox('La instalación no se completó (código ' + IntToStr(ResultCode) + ').' + #13#10 +
             'Revisa el registro en: ' + ExpandConstant('{app}\install.log'),
             mbError, MB_OK);
    end;
  end;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
var
  ResultCode: Integer;
  Params: String;
begin
  if CurUninstallStep = usUninstall then
  begin
    Params := '-NoProfile -ExecutionPolicy Bypass -File "' +
              ExpandConstant('{app}\scripts\uninstall.ps1') + '" -InstallDir "' +
              ExpandConstant('{app}') + '"';
    // Por defecto los datos se conservan; solo se borran si el usuario lo confirma.
    if MsgBox('¿Eliminar también la base de datos y todos los datos?' + #13#10 +
              'Si responde No, se conservan y podrá reinstalar sin perderlos.',
              mbConfirmation, MB_YESNO or MB_DEFBUTTON2) = IDYES then
      Params := Params + ' -RemoveData';
    Exec('powershell.exe', Params, '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
  end;
end;{BCF0DF78-7A45-4EB2-8335-C6BD4CB879ED}