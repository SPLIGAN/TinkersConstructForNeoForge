#Requires -Version 5.1
<#
.SYNOPSIS
  Build SlimeKnights/Mantle (NeoForge 1.21.1 WIP tree) and publish to mavenLocal, then optionally copy the jar to this repo as libs/mantle.jar.

  Prerequisite: Mantle clone at $MantleRoot with NeoGradle port (see sibling folder GitHub/Mantle).

  Coordinate must match gradle.properties: mantle_coordinate_version (default 1.21.1-LOCAL-SNAPSHOT).
#>
$ErrorActionPreference = 'Stop'
$RepoRoot = Split-Path $PSScriptRoot -Parent
$MantleRoot = if ($env:MANTLE_ROOT) { $env:MANTLE_ROOT } else { Join-Path (Split-Path $RepoRoot -Parent) 'Mantle' }
if (-not (Test-Path (Join-Path $MantleRoot 'gradlew.bat'))) {
  Write-Error "Mantle repo not found at $MantleRoot. Set env MANTLE_ROOT or clone https://github.com/SlimeKnights/Mantle"
}

Push-Location $MantleRoot
try {
  & .\gradlew.bat publishToMavenLocal --no-daemon
  if ($LASTEXITCODE -ne 0) { throw "Mantle publishToMavenLocal failed with exit $LASTEXITCODE" }
}
finally {
  Pop-Location
}

$libsDir = Join-Path $RepoRoot 'libs'
if ($env:COPY_MANTLE_JAR -eq '1') {
  New-Item -ItemType Directory -Force -Path $libsDir | Out-Null
  $built = Get-ChildItem (Join-Path $MantleRoot 'build\libs') -Filter 'Mantle*.jar' -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1
  if (-not $built) { Write-Error "No Mantle jar under $MantleRoot\build\libs" }
  Copy-Item -Force $built.FullName (Join-Path $libsDir 'mantle.jar')
  Write-Host "Copied $($built.Name) -> $libsDir\mantle.jar"
}

Write-Host "Published Mantle to mavenLocal. TConstruct resolves: slimeknights.mantle:Mantle:<mantle_coordinate_version>"
