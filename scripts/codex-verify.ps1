param(
  [string]$Profile = "prod",
  [switch]$RunTests
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

if ($RunTests) {
  .\gradlew.bat clean test "-Pprofile=$Profile"
} else {
  .\gradlew.bat clean bootJar "-Pprofile=$Profile"
}

if ($LASTEXITCODE -ne 0) {
  throw "Gradle 검증에 실패했습니다."
}
