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

function Get-RemoteInfo {
  $remoteUrl = (git remote get-url origin).Trim()
  if ($remoteUrl -notmatch "github\.com[:/](?<owner>[^/]+)/(?<repo>[^/.]+)(\.git)?") {
    throw "GitHub 원격 저장소 정보를 확인할 수 없습니다."
  }

  return @{
    Url = $remoteUrl
    Owner = $Matches.owner
    Repo = $Matches.repo
  }
}

function Get-GitHubToken {
  $remoteUrl = (git remote get-url origin).Trim()
  if ($remoteUrl -match "^https://[^:]+:(?<token>[^@]+)@github\.com/") {
    return $Matches.token
  }

  $credentialInput = "protocol=https`nhost=github.com`n`n"
  $credential = $credentialInput | git credential-manager get 2>$null
  if ($LASTEXITCODE -eq 0) {
    $passwordLine = $credential | Where-Object { $_ -like "password=*" } | Select-Object -First 1
    if ($passwordLine) {
      return ($passwordLine -replace "^password=", "")
    }
  }

  throw "GitHub 토큰을 찾을 수 없습니다. Git Credential Manager에 GitHub 인증을 먼저 저장하세요."
}

function Invoke-GitHubApi {
  param(
    [Parameter(Mandatory = $true)]
    [string]$Token,
    [Parameter(Mandatory = $true)]
    [string]$Method,
    [Parameter(Mandatory = $true)]
    [string]$Url,
    [object]$Body = $null
  )

  $headers = @{
    Authorization = "Bearer $Token"
    Accept = "application/vnd.github+json"
    "X-GitHub-Api-Version" = "2022-11-28"
  }

  if ($null -eq $Body) {
    return Invoke-RestMethod -Method $Method -Headers $headers -Uri $Url
  }

  return Invoke-RestMethod -Method $Method -Headers $headers -ContentType "application/json" -Body ($Body | ConvertTo-Json -Depth 10) -Uri $Url
}

function Get-OrCreatePullRequest {
  param(
    [string]$Token,
    [string]$Owner,
    [string]$Repo,
    [string]$HeadBranch,
    [string]$TargetBranch,
    [string]$Title
  )

  $encodedHead = [uri]::EscapeDataString("${Owner}:${HeadBranch}")
  $encodedBase = [uri]::EscapeDataString($TargetBranch)
  $existing = Invoke-GitHubApi -Token $Token -Method Get -Url "https://api.github.com/repos/$Owner/$Repo/pulls?state=open&head=$encodedHead&base=$encodedBase"
  if ($existing.Count -gt 0) {
    return $existing[0]
  }

  return Invoke-GitHubApi -Token $Token -Method Post -Url "https://api.github.com/repos/$Owner/$Repo/pulls" -Body @{
    title = $Title
    body = "Codex 배포 자동화로 생성한 PR입니다."
    head = $HeadBranch
    base = $TargetBranch
  }
}

function Wait-UntilPullRequestMergeable {
  param(
    [string]$Token,
    [string]$Owner,
    [string]$Repo,
    [int]$Number
  )

  for ($i = 0; $i -lt 12; $i++) {
    $pr = Invoke-GitHubApi -Token $Token -Method Get -Url "https://api.github.com/repos/$Owner/$Repo/pulls/$Number"
    if ($null -ne $pr.mergeable) {
      return $pr
    }
    Start-Sleep -Seconds 3
  }

  return Invoke-GitHubApi -Token $Token -Method Get -Url "https://api.github.com/repos/$Owner/$Repo/pulls/$Number"
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
  $remoteInfo = Get-RemoteInfo
  $token = Get-GitHubToken
  $pr = Get-OrCreatePullRequest -Token $token -Owner $remoteInfo.Owner -Repo $remoteInfo.Repo -HeadBranch $Branch -TargetBranch $BaseBranch -Title $Message
  Write-Host "PR #$($pr.number)를 준비했습니다: $($pr.html_url)"

  if ($MergePr) {
    $pr = Wait-UntilPullRequestMergeable -Token $token -Owner $remoteInfo.Owner -Repo $remoteInfo.Repo -Number $pr.number
    if ($pr.mergeable -ne $true) {
      throw "PR #$($pr.number)를 자동 병합할 수 없습니다. 현재 상태: $($pr.mergeable_state)"
    }

    $merge = Invoke-GitHubApi -Token $token -Method Put -Url "https://api.github.com/repos/$($remoteInfo.Owner)/$($remoteInfo.Repo)/pulls/$($pr.number)/merge" -Body @{
      commit_title = $Message
      commit_message = "Codex 배포 자동화를 통해 병합합니다."
      merge_method = "merge"
    }
    Write-Host "PR #$($pr.number)를 $BaseBranch 브랜치에 병합했습니다. 병합 커밋: $($merge.sha)"
  } else {
    Write-Host "$Branch 브랜치에서 $BaseBranch 브랜치로 향하는 PR을 생성했습니다. 병합하면 GCP 배포 워크플로가 시작됩니다."
  }
} else {
  Write-Host "$Branch 브랜치를 $Remote 원격 저장소에 push했습니다. GCP 배포 PR을 열려면 -CreatePr 옵션을 함께 실행하세요."
}
