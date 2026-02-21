# Stash Android - Development Guide

프로젝트 전체 규칙의 허브. 모든 에이전트(`~/.claude/agents/`)가 이 문서를 먼저 읽는다.

---

## 1. 프로젝트 개요

| 항목 | 선택 |
|------|------|
| 앱 이름 | **Stash** |
| 플랫폼 | Android (Kotlin) |
| UI | Jetpack Compose |
| 아키텍처 | MVI (Circuit by Slack) |
| DI | Hilt |
| 데이터 | Room (로컬 우선) |
| AI/검색 | ONNX Runtime (온디바이스 임베딩 + 시맨틱 검색) |
| 최소 타겟 | API 26 (Android 8.0) |
| 패키지명 | com.kangraemin.stash |
| 테스트 | JUnit5 + Turbine |
| GitHub | [kangraemin/stash-android](https://github.com/kangraemin/stash-android) |

### 핵심 기능

- **Share Intent**: 1탭 저장 (인스타, 유튜브, 크롬, 네이버지도, 구글맵, 쿠팡 등)
- **자동 카테고리**: URL 도메인 기반 콘텐츠 타입 자동 분류
- **시맨틱 검색**: ONNX Runtime 온디바이스 임베딩 기반 자연어 검색
- **딥링크**: 저장된 콘텐츠에서 원본 앱으로 바로 이동

### UX 원칙

> **저장은 1탭, 정리는 앱이, 검색은 AI가.**

---

## 2. 상세 가이드

| 문서 | 내용 |
|------|------|
| [Architecture](docs/ARCHITECTURE.md) | Circuit 패턴, 프로젝트 구조, DI, 데이터 흐름 |
| [Coding Conventions](docs/CODING_CONVENTIONS.md) | 네이밍, 파일 구성, 코드 스타일 |
| [Testing](docs/TESTING.md) | JUnit5 + Turbine 패턴, 빌드 명령 |
| [Compose + Circuit Guide](docs/COMPOSE_GUIDE.md) | Screen-Presenter 연결, Navigation, 비동기 패턴 |
| [UX Guide](docs/UX_GUIDE.md) | 저장 플로우, 화면 구성, 카드 디자인, 검색 |
| [단계별 개발 원칙](~/.claude/guides/common/phase-development.md) | Phase/Step 구조, 완료 조건 (글로벌) |
| [팀 워크플로우](~/.claude/guides/common/team-workflow.md) | Lead/Dev/QA 역할 (글로벌) |
| [Git Rules](~/.claude/rules/git-rules.md) | 커밋, 푸시, PR 규칙 (글로벌) |

---

## 3. 환경 요구사항

| 항목 | 필요 |
|------|------|
| JDK | 17+ |
| Android Studio | Hedgehog (2023.1.1)+ |
| Kotlin | 1.9+ |
| Gradle | 8.x (Kotlin DSL) |
| AGP | 8.x |

---

## 4. 의존성

| 패키지 | 용도 |
|--------|------|
| [circuit](https://github.com/slackhq/circuit) | MVI 아키텍처 (Slack) |
| [hilt](https://dagger.dev/hilt/) | DI |
| [room](https://developer.android.com/training/data-storage/room) | 로컬 DB |
| [onnxruntime-android](https://onnxruntime.ai/) | 온디바이스 AI |
| [coil](https://coil-kt.github.io/coil/) | 이미지 로딩 |
| [turbine](https://github.com/cashapp/turbine) | Flow 테스트 |
| [molecule](https://github.com/cashapp/molecule) | Presenter 테스트 |
| [mockk](https://mockk.io/) | Mocking |

Gradle (Kotlin DSL) + Version Catalog으로 관리.

---

## 5. Git 컨벤션

`~/.claude/rules/git-rules.md` 참조. 추가로 이 프로젝트에서는:

- `develop` 브랜치를 개발 통합 브랜치로 사용
- `feature/<단계명>` 브랜치로 각 Step 작업
- 단계 완료 시 `develop`에 머지 후 태그: `phase-X.step-Y`
