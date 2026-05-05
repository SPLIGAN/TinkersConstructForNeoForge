#Requires -Version 5.1
<#
.SYNOPSIS
  Copy the built TConstruct jar into an Arclight server mods folder for manual smoke testing.

  Usage:
    $env:ARCLIGHT_SERVER = 'D:\servers\arclight-1211'
    .\scripts\04-arclight-smoke.ps1

  After start, inspect latest.log under ARCLIGHT_SERVER for registry/bridge errors.
#>
$ErrorActionPreference = 'Stop'
$RepoRoot = Split-Path $PSScriptRoot -Parent
if (-not $env:ARCLIGHT_SERVER) {
  Write-Error 'Set ARCLIGHT_SERVER to your Arclight server root (folder containing arclight.jar or start scripts).'
}
$mods = Join-Path $env:ARCLIGHT_SERVER 'mods'
New-Item -ItemType Directory -Force -Path $mods | Out-Null

Push-Location $RepoRoot
try {
  & .\gradlew.bat jar --no-daemon
  if ($LASTEXITCODE -ne 0) { throw "gradlew jar failed: $LASTEXITCODE" }
}
finally {
  Pop-Location
}

$jar = Get-ChildItem (Join-Path $RepoRoot 'build\libs') -Filter 'TinkersConstruct*.jar' | Where-Object { $_.Name -notmatch 'sources' } | Select-Object -First 1
if (-not $jar) { Write-Error "No TinkersConstruct jar in $RepoRoot\build\libs" }
Copy-Item -Force $jar.FullName (Join-Path $mods $jar.Name)
Write-Host "Installed $($jar.Name) -> $mods"
Write-Host "Start the Arclight server, then review: $(Join-Path $env:ARCLIGHT_SERVER 'logs\latest.log')"
