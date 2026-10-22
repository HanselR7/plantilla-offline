# Genera secretos aleatorios para una instalación. Se ejecuta una sola vez.
param([string]$OutFile = "$PSScriptRoot\secrets.env")

function New-Secret([int]$bytes = 24) {
    $buf = New-Object byte[] $bytes
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($buf)
    ([Convert]::ToBase64String($buf)) -replace '[^A-Za-z0-9]', 'x'
}

if (Test-Path $OutFile) { throw "Ya existe $OutFile; no se sobrescribe." }

@"
PLANTILLA_DB_PASSWORD=$(New-Secret)
PLANTILLA_ADMIN_PASSWORD=$(New-Secret 12)
PLANTILLA_KEYSTORE_PASSWORD=$(New-Secret)
"@ | Set-Content -Path $OutFile -Encoding ascii

Write-Host "Secretos generados en $OutFile"