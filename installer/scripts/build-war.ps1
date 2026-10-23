# Compila frontend + backend y deja plantilla-api.war en installer\payload.
$ErrorActionPreference = 'Stop'
$root = Resolve-Path (Join-Path $PSScriptRoot '..\..')
$webapp = Join-Path $root 'backend\bootstrap\src\main\webapp'

Push-Location (Join-Path $root 'frontend')
yarn install --frozen-lockfile
yarn build
Pop-Location

# Se conserva WEB-INF; lo demás se reemplaza por el frontend compilado.
Get-ChildItem $webapp -Exclude 'WEB-INF' | Remove-Item -Recurse -Force
Copy-Item (Join-Path $root 'frontend\dist\*') $webapp -Recurse

Push-Location (Join-Path $root 'backend')
mvn -B clean package -DskipTests
Pop-Location

New-Item -ItemType Directory -Force (Join-Path $root 'installer\payload') | Out-Null
Copy-Item (Join-Path $root 'backend\bootstrap\target\plantilla-api.war') `
          (Join-Path $root 'installer\payload\plantilla-api.war') -Force
Write-Host 'WAR listo en installer\payload\plantilla-api.war'