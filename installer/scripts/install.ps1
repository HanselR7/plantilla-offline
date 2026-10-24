#Requires -RunAsAdministrator
param(
    [string]$InstallDir = 'C:\PlantillaOffline',
    [string]$PayloadDir = (Join-Path $PSScriptRoot '..\payload'),
    [int]$PgPort = 5433
)
$ErrorActionPreference = 'Stop'
$jbossCfgDir = Join-Path $PSScriptRoot '..\jboss'
$svcName = 'PlantillaServer'
$pgService = 'plantilla-postgres'

function Step($t) { Write-Host "`n==> $t" -ForegroundColor Cyan }

function New-Secret([int]$bytes = 18) {
    $buf = New-Object byte[] $bytes
    $rng = New-Object System.Security.Cryptography.RNGCryptoServiceProvider
    $rng.GetBytes($buf)
    $rng.Dispose()
    ([Convert]::ToBase64String($buf)) -replace '[^A-Za-z0-9]', 'x'
}

function Get-Payload([string]$pattern) {
    $f = Get-ChildItem -Path $PayloadDir -Filter $pattern -File | Select-Object -First 1
    if (-not $f) { throw "Falta en payload: $pattern" }
    $f.FullName
}

# Los programas nativos escriben en stderr aunque no fallen; se decide por el código de salida.
function Invoke-Native([string]$exe, [string[]]$cmdArgs) {
    $prev = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    & $exe @cmdArgs 2>&1 | ForEach-Object { Write-Host $_ }
    $code = $LASTEXITCODE
    $ErrorActionPreference = $prev
    if ($code -ne 0) { throw "Falló ($code): $exe" }
}

function Expand-Single([string]$zip, [string]$dest) {
    $tmp = Join-Path $env:TEMP ("plantilla-" + [guid]::NewGuid().ToString('N'))
    Expand-Archive -Path $zip -DestinationPath $tmp -Force
    $inner = Get-ChildItem $tmp | Select-Object -First 1
    Move-Item $inner.FullName $dest
    Remove-Item $tmp -Recurse -Force
}

if (Get-Service -Name $svcName -ErrorAction SilentlyContinue) {
    throw "Ya hay una instalación (servicio $svcName). Ejecuta uninstall.ps1 primero."
}

Step 'Preparando carpetas y secretos'
New-Item -ItemType Directory -Force $InstallDir | Out-Null
$pgSuper = New-Secret
$dbPass = New-Secret
$adminPass = New-Secret 9
$ksPass = New-Secret

Step 'Instalando Java'
Expand-Single (Get-Payload 'OpenJDK21*.zip') (Join-Path $InstallDir 'jre')
$jre = Join-Path $InstallDir 'jre'

Step 'Instalando WildFly'
Expand-Single (Get-Payload 'wildfly-*.zip') (Join-Path $InstallDir 'wildfly')
$wf = Join-Path $InstallDir 'wildfly'

Step "Instalando PostgreSQL (puerto $PgPort, puede tardar varios minutos)"
$pgDir = Join-Path $InstallDir 'pgsql'
$pgData = Join-Path $InstallDir 'pgdata'
$pgArgs = @(
    '--mode', 'unattended', '--unattendedmodeui', 'none',
    '--superpassword', $pgSuper,
    '--servicename', $pgService,
    '--serverport', $PgPort,
    '--prefix', $pgDir,
    '--datadir', $pgData,
    '--enable-components', 'server,commandlineutils',
    '--disable-components', 'pgAdmin,stackbuilder'
)
$p = Start-Process -FilePath (Get-Payload 'postgresql-*-windows-x64.exe') -ArgumentList $pgArgs -Wait -PassThru
if ($p.ExitCode -ne 0) { throw "El instalador de PostgreSQL terminó con código $($p.ExitCode)" }

$psql = Join-Path $pgDir 'bin\psql.exe'
$isReady = Join-Path $pgDir 'bin\pg_isready.exe'
for ($i = 0; $i -lt 30; $i++) {
    & $isReady -h localhost -p $PgPort | Out-Null
    if ($LASTEXITCODE -eq 0) { break }
    Start-Sleep -Seconds 2
}

Step 'Creando usuario y base de datos'
$env:PGPASSWORD = $pgSuper
Invoke-Native $psql @('-h', 'localhost', '-p', $PgPort, '-U', 'postgres', '-c',
    "CREATE ROLE plantilla LOGIN PASSWORD '$dbPass';")
Invoke-Native $psql @('-h', 'localhost', '-p', $PgPort, '-U', 'postgres', '-c',
    "CREATE DATABASE plantilla_local OWNER plantilla TEMPLATE template0 ENCODING 'UTF8' LC_COLLATE 'C' LC_CTYPE 'C';")
Remove-Item Env:\PGPASSWORD

Step 'Configurando driver y datasource en WildFly'
$mod = Join-Path $wf 'modules\org\postgresql\main'
New-Item -ItemType Directory -Force $mod | Out-Null
Copy-Item (Get-Payload 'postgresql-42*.jar') (Join-Path $mod 'postgresql.jar')
Copy-Item (Join-Path $jbossCfgDir 'postgresql-module.xml') (Join-Path $mod 'module.xml')

$env:JAVA_HOME = $jre
$env:NOPAUSE = 'true'
$env:PLANTILLA_DB_URL = "jdbc:postgresql://localhost:$PgPort/plantilla_local"
$env:PLANTILLA_DB_USER = 'plantilla'
$env:PLANTILLA_DB_PASSWORD = $dbPass
$env:PLANTILLA_KEYSTORE_PASSWORD = $ksPass
$cli = Join-Path $wf 'bin\jboss-cli.bat'
Invoke-Native $cli @("--file=$(Join-Path $jbossCfgDir 'datasource.cli')")

Step 'Generando autoridad certificadora y certificado HTTPS'
$ips = Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object { $_.IPAddress -notlike '169.254.*' -and $_.IPAddress -ne '127.0.0.1' } |
    Select-Object -ExpandProperty IPAddress
$certDir = Join-Path $InstallDir 'certs'
New-Item -ItemType Directory -Force $certDir | Out-Null
$hostnames = @('localhost', $env:COMPUTERNAME)
& (Join-Path $PSScriptRoot 'make-certs.ps1') -OutDir $certDir -Ips $ips -Hostnames $hostnames `
    -KeystorePassword $ksPass -Keytool (Join-Path $jre 'bin\keytool.exe')

$cfgDir = Join-Path $wf 'standalone\configuration'
Copy-Item (Join-Path $certDir 'plantilla.p12') $cfgDir -Force
Copy-Item (Join-Path $certDir 'plantilla-ca.crt') $cfgDir -Force
Remove-Item (Join-Path $certDir 'plantilla.p12'), (Join-Path $certDir 'plantilla-ca.crt') -Force
# ca.p12 (clave privada de la CA): solo SYSTEM y administradores.
Invoke-Native 'icacls.exe' @($certDir, '/inheritance:r', '/grant:r', '*S-1-5-18:(OI)(CI)F', '*S-1-5-32-544:(OI)(CI)F')
Invoke-Native $cli @("--file=$(Join-Path $jbossCfgDir 'https.cli')")

Step 'Desplegando la aplicación'
Copy-Item (Get-Payload 'plantilla-api.war') (Join-Path $wf 'standalone\deployments\plantilla-api.war')

Step 'Abriendo puertos del firewall (redes privadas y de dominio)'
New-NetFirewallRule -DisplayName 'Plantilla Offline (HTTP/HTTPS)' -Direction Inbound -Protocol TCP `
    -LocalPort 8080, 8443 -Action Allow -Profile Private, Domain | Out-Null

Step 'Registrando el servicio de Windows'
$svcDir = Join-Path $InstallDir 'service'
New-Item -ItemType Directory -Force $svcDir | Out-Null
$svcExe = Join-Path $svcDir "$svcName.exe"
Copy-Item (Get-Payload 'WinSW-x64.exe') $svcExe

$xml = @"
<service>
  <id>$svcName</id>
  <name>Plantilla Offline - Servidor</name>
  <description>Servidor local de Plantilla Offline (WildFly).</description>
  <executable>$wf\bin\standalone.bat</executable>
  <arguments>-b 0.0.0.0 -Dplantilla.mode=LOCAL</arguments>
  <stopexecutable>$wf\bin\jboss-cli.bat</stopexecutable>
  <stoparguments>--connect --command=:shutdown</stoparguments>
  <workingdirectory>$wf</workingdirectory>
  <env name="JAVA_HOME" value="$jre"/>
  <env name="JBOSS_HOME" value="$wf"/>
  <env name="NOPAUSE" value="true"/>
  <env name="PLANTILLA_DB_URL" value="jdbc:postgresql://localhost:$PgPort/plantilla_local"/>
  <env name="PLANTILLA_DB_USER" value="plantilla"/>
  <env name="PLANTILLA_DB_PASSWORD" value="$dbPass"/>
  <env name="PLANTILLA_ADMIN_PASSWORD" value="$adminPass"/>
  <env name="PLANTILLA_KEYSTORE_PASSWORD" value="$ksPass"/>
  <depend>$pgService</depend>
  <startmode>Automatic</startmode>
  <onfailure action="restart" delay="10 sec"/>
  <log mode="roll-by-size">
    <sizeThreshold>10240</sizeThreshold>
    <keepFiles>5</keepFiles>
  </log>
</service>
"@
$svcXml = Join-Path $svcDir "$svcName.xml"
[IO.File]::WriteAllText($svcXml, $xml, (New-Object System.Text.UTF8Encoding($false)))
# El XML contiene las claves: solo SYSTEM y administradores pueden leerlo.
Invoke-Native 'icacls.exe' @($svcXml, '/inheritance:r', '/grant:r', '*S-1-5-18:F', '*S-1-5-32-544:F')

Invoke-Native $svcExe @('install')
Invoke-Native $svcExe @('start')

Step 'Esperando a que el servidor responda'
$up = $false
for ($i = 0; $i -lt 60; $i++) {
    try {
        $r = Invoke-RestMethod -Uri 'http://localhost:8080/plantilla-api/api/health' -TimeoutSec 3
        if ($r.status -eq 'UP') { $up = $true; break }
    } catch { }
    Start-Sleep -Seconds 3
}
if (-not $up) { Write-Warning "El servidor no respondió a tiempo. Revisa $wf\standalone\log\server.log" }

$info = Join-Path $InstallDir 'install-info.txt'
@"
Plantilla Offline - datos de la instalación
Usuario administrador : admin
Contraseña            : $adminPass
Contraseña postgres   : $pgSuper   (puerto $PgPort)
Dirección             : https://$env:COMPUTERNAME`:8443/plantilla-api/
iPhone: abrir en Safari http://IP-DE-ESTA-PC:8080/plantilla-api/api/pki/ca
        instalar el perfil y activarlo en Ajustes > General > Información > Certificados de confianza
"@ | Set-Content -Path $info -Encoding ascii
Invoke-Native 'icacls.exe' @($info, '/inheritance:r', '/grant:r', '*S-1-5-18:F', '*S-1-5-32-544:F')

Write-Host "`nInstalación terminada. Datos en: $info" -ForegroundColor Green