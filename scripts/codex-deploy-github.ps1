param(
  [Parameter(Mandatory = $true)]
  [string]$Message,

  [string]$Remote = "origin",
  [string]$Branch = "",
  [string]$BaseBranch = "dev",
  [string]$Profile = "prod",
  [switch]$SkipVerify,
  [switch]$RunTests,
  [switch]$CreatePr,
  [switch]$MergePr
)

$ErrorActionPreference = "Stop"

function Exec {
  param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Command)
  & $Command[0] @($Command | Select-Object -Skip 1)
  if ($LASTEXITCODE -ne 0) {
    throw "Command failed: $($Command -join ' ')"
  }
}

function Get-GhPath {
  $cmd = Get-Command gh -ErrorAction SilentlyContinue
  if ($cmd) {
    return $cmd.Source
  }

  $defaultPath = "C:\Program Files\GitHub CLI\gh.exe"
  if (Test-Path $defaultPath) {
    return $defaultPath
  }

  return ""
}

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

if (-not $SkipVerify) {
  $verifyArgs = @("-Profile", $Profile)
  if ($RunTests) {
    $verifyArgs += "-RunTests"
  }
  Exec powershell -ExecutionPolicy Bypass -File ".\scripts\codex-verify.ps1" @verifyArgs
}

if ([string]::IsNullOrWhiteSpace($Branch)) {
  $Branch = (git branch --show-current).Trim()
}

if ([string]::IsNullOrWhiteSpace($Branch)) {
  throw "현재 브랜치를 확인할 수 없습니다. -Branch 값을 직접 전달하세요."
}

$changes = (git status --porcelain)
if ($changes) {
  Exec git add -A
  Exec git commit -m $Message
} else {
  Write-Host "커밋할 로컬 변경사항이 없습니다. 현재 브랜치를 push합니다."
}

Exec git push -u $Remote $Branch

if ($CreatePr -or $MergePr) {
  $gh = Get-GhPath
  if ([string]::IsNullOrWhiteSpace($gh)) {
    throw "PR 자동화를 위해 GitHub CLI가 필요합니다. gh를 설치하거나 PR을 직접 생성하세요."
  }

  Exec $gh pr create --base $BaseBranch --head $Branch --title $Message --body "Codex 배포 자동화로 생성한 PR입니다."

  if ($MergePr) {
    Exec $gh pr merge $Branch --merge --delete-branch
    Write-Host "PR을 $BaseBranch 브랜치에 병합했습니다. 병합된 PR이 닫히면 GCP 배포 워크플로가 시작됩니다."
  } else {
    Write-Host "$Branch 브랜치에서 $BaseBranch 브랜치로 향하는 PR을 생성했습니다. 병합하면 GCP 배포 워크플로가 시작됩니다."
  }
} else {
  Write-Host "$Branch 브랜치를 $Remote 원격 저장소에 push했습니다. GCP 배포 PR을 열려면 -CreatePr 옵션을 함께 실행하세요."
}
