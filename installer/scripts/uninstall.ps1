#Requires -RunAsAdministrator
param(
    [string]$InstallDir = 'C:\PlantillaOffline',
    [switch]$RemoveData   # sin este parámetro NO se borran los datos
)
$svcExe = Join-Path $InstallDir 'service\PlantillaServer.exe'
if (Test-Path $svcExe) {
    & $svcExe stop
    & $svcExe uninstall
}
Get-NetFirewallRule -DisplayName 'Plantilla Offline (HTTP/HTTPS)' -ErrorAction SilentlyContinue | Remove-NetFirewallRule

$pgUninstall = Join-Path $InstallDir 'pgsql\uninstall-postgresql.exe'
if ($RemoveData) {
    if (Test-Path $pgUninstall) { Start-Process $pgUninstall -ArgumentList '--mode', 'unattended' -Wait }
    Remove-Item $InstallDir -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host 'Instalación y datos eliminados.'
} else {
    Write-Host 'Servicio y reglas eliminados. Los datos (base de datos y archivos) se conservan.'
}