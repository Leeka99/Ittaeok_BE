# Codex GitHub 배포

이 백엔드 저장소는 Codex가 GitHub push 및 PR 기반 배포 흐름을 직접 수행할 수 있도록 준비되어 있습니다.

## 검증

```powershell
.\scripts\codex-verify.ps1
```

기본 검증은 GitHub Actions 빌드 경로와 동일하게 `clean bootJar -Pprofile=prod`를 실행합니다.

테스트 환경이 준비되어 있으면 명시적으로 테스트를 함께 실행합니다.

```powershell
.\scripts\codex-verify.ps1 -RunTests
```

## 커밋 및 push

```powershell
.\scripts\codex-deploy-github.ps1 -Message "chore: Codex GitHub 배포 자동화 환경 구성"
```

## 배포 PR 생성

```powershell
.\scripts\codex-deploy-github.ps1 -Message "fix: 배포 오류 수정" -CreatePr
```

백엔드 GitHub Actions 워크플로는 PR이 `dev` 브랜치에 병합된 뒤에만 GCP 배포를 실행합니다.

GitHub CLI 인증이 되어 있고 즉시 병합하려는 경우 Codex는 다음 명령을 실행할 수 있습니다.

```powershell
.\scripts\codex-deploy-github.ps1 -Message "fix: 배포 오류 수정" -CreatePr -MergePr
```
