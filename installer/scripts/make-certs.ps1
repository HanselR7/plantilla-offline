# Genera una CA propia y un certificado de servidor firmado por ella.
param(
    [Parameter(Mandatory)][string]$OutDir,
    [Parameter(Mandatory)][string[]]$Ips,
    [Parameter(Mandatory)][string]$KeystorePassword,
    [string[]]$Hostnames = @('localhost'),
    [string]$Keytool = 'keytool.exe'
)
$ErrorActionPreference = 'Stop'

function Invoke-Keytool([string[]]$a) {
    $prev = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    & $Keytool @a 2>&1 | ForEach-Object { Write-Host $_ }
    $code = $LASTEXITCODE
    $ErrorActionPreference = $prev
    if ($code -ne 0) { throw "keytool falló ($code): $($a -join ' ')" }
}

New-Item -ItemType Directory -Force $OutDir | Out-Null
$caP12 = Join-Path $OutDir 'ca.p12'
$srvP12 = Join-Path $OutDir 'plantilla.p12'
$caCrt = Join-Path $OutDir 'plantilla-ca.crt'
$csr = Join-Path $OutDir 'server.csr'
$srvCrt = Join-Path $OutDir 'server.crt'
foreach ($f in $caP12, $srvP12, $caCrt, $csr, $srvCrt) { Remove-Item $f -ErrorAction SilentlyContinue }

# La CA usa la misma clave que el keystore solo por simplicidad; ca.p12 NO se distribuye.
Invoke-Keytool @('-genkeypair', '-alias', 'plantilla-ca', '-keyalg', 'RSA', '-keysize', '2048',
    '-validity', '3650', '-keystore', $caP12, '-storetype', 'PKCS12', '-storepass', $KeystorePassword,
    '-dname', 'CN=plantilla-offline-server-CA', '-ext', 'bc:c', '-ext', 'ku:c=keyCertSign,cRLSign')

Invoke-Keytool @('-exportcert', '-alias', 'plantilla-ca', '-keystore', $caP12,
    '-storepass', $KeystorePassword, '-file', $caCrt, '-rfc')

Invoke-Keytool @('-genkeypair', '-alias', 'plantilla', '-keyalg', 'RSA', '-keysize', '2048',
    '-keystore', $srvP12, '-storetype', 'PKCS12', '-storepass', $KeystorePassword,
    '-dname', 'CN=plantilla-offline-server')

Invoke-Keytool @('-certreq', '-alias', 'plantilla', '-keystore', $srvP12,
    '-storepass', $KeystorePassword, '-file', $csr)

$entries = @()
foreach ($h in $Hostnames) { $entries += "dns:$h" }
foreach ($i in $Ips) { $entries += "ip:$i" }
$entries += 'ip:127.0.0.1'
$san = $entries -join ','
Invoke-Keytool @('-gencert', '-alias', 'plantilla-ca', '-keystore', $caP12, '-storepass', $KeystorePassword,
    '-infile', $csr, '-outfile', $srvCrt, '-validity', '825', '-rfc',
    '-ext', "SAN=$san", '-ext', 'eku=serverAuth', '-ext', 'ku:c=digitalSignature,keyEncipherment')

# Cadena: primero la CA como confiable, luego la respuesta firmada.
Invoke-Keytool @('-importcert', '-alias', 'plantilla-ca', '-file', $caCrt, '-keystore', $srvP12,
    '-storepass', $KeystorePassword, '-noprompt')
Invoke-Keytool @('-importcert', '-alias', 'plantilla', '-file', $srvCrt, '-keystore', $srvP12,
    '-storepass', $KeystorePassword)

Remove-Item $csr, $srvCrt -ErrorAction SilentlyContinue
Write-Host "Listo. Usar: $srvP12 (servidor) y $caCrt (para los dispositivos). Guardar $caP12 en privado."