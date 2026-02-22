# Stash

> 저장은 1탭, 정리는 앱이, 검색은 AI가.

인스타, 유튜브, 네이버지도, 쿠팡 등을 보다가 나중에 다시 보고 싶은 콘텐츠를 **공유하기 한 번**으로 저장하는 Android 앱.

---

## 어떤 앱인가요?

- 유튜브에서 재밌는 영상 발견 → 공유하기 → Stash에 저장
- 네이버지도에서 맛집 발견 → 공유하기 → Stash에 저장
- 인스타에서 갈만한 카페 발견 → 공유하기 → Stash에 저장
- 쿠팡에서 사고 싶은 물건 발견 → 공유하기 → Stash에 저장

나중에 찾을 때는 **"강남 근처 이탈리안"** 같은 자연어로 검색하면 AI가 찾아줍니다.

## 핵심 기능

- **1탭 저장** — Share Intent로 어떤 앱에서든 공유하기 한 번이면 끝
- **자동 분류** — 영상, 장소, 쇼핑, 아티클 등 콘텐츠 타입을 자동으로 구분
- **소스별 카드** — 유튜브는 썸네일+채널명, 맛집은 주소+지도, 쇼핑은 가격 표시
- **AI 검색** — 온디바이스 시맨틱 검색으로 정리 안 해도 원하는 걸 찾을 수 있음
- **원본 앱 이동** — 저장한 콘텐츠를 탭하면 유튜브, 네이버지도 등 원래 앱으로 바로 이동
- **테마 설정** — 라이트 / 다크 / 시스템 연동

---

## 기술 스택

| 항목 | 선택 |
|------|------|
| 플랫폼 | Android (API 26+) |
| 언어 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 아키텍처 | MVI (Circuit by Slack) |
| DI | Hilt |
| 데이터 | Room (로컬 우선) |
| 설정 저장 | DataStore Preferences |
| AI | ONNX Runtime (온디바이스 임베딩 + 시맨틱 검색) |
| 백그라운드 | WorkManager (메타데이터 추출, 임베딩 생성) |
| 이미지 | Coil |
| 딥링크 | Chrome Custom Tab + 앱별 Intent |
| 테스트 | JUnit5 + Turbine + Molecule + MockK |

---

## 프로젝트 구조

```
app/src/main/java/com/kangraemin/stash/
├── app/                          # Application, MainActivity
├── features/                     # 화면별 모듈 (Circuit MVI)
│   ├── home/                     # 홈 화면 (카드 그리드 + 필터 칩)
│   ├── search/                   # 검색 (키워드 + 시맨틱 하이브리드)
│   ├── detail/                   # 상세 화면 (딥링크, 삭제)
│   ├── settings/                 # 설정 (테마, 데이터 관리)
│   └── common/                   # 공통 UI (Empty, Error, Loading)
├── domain/                       # 비즈니스 로직 (순수 Kotlin)
│   ├── model/                    # SavedContent, ContentType, ThemeMode
│   ├── repository/               # Repository / Service 인터페이스
│   └── contentparsing/           # URL 파싱, 딥링크 핸들러
├── data/                         # 데이터 구현
│   ├── room/                     # Room Entity, DAO, Database
│   ├── repository/               # Repository 구현체
│   └── mapper/                   # Entity ↔ Domain 변환
├── ml/                           # 온디바이스 AI
│   ├── embedding/                # ONNX 모델 로딩, 텍스트 임베딩
│   └── vectorsearch/             # 코사인 유사도 벡터 검색
├── share/                        # Share Intent 처리
│   ├── ShareActivity.kt          # 공유 수신
│   ├── MetadataWorker.kt         # OG 태그 메타데이터 추출
│   └── EmbeddingWorker.kt        # 임베딩 벡터 생성
└── di/                           # Hilt 모듈
```

---

## 아키텍처

Circuit (Slack) 기반 MVI 패턴:

```
Screen (화면 식별자, @Parcelize)
  ├── State (UI 상태, CircuitUiState)
  └── Event (사용자 이벤트, sealed interface)

Presenter (@CircuitInject, @AssistedInject)
  → Repository / Service 주입
  → State 생산 + Event 처리

UI Composable (@CircuitInject)
  → State 렌더링 + Event 발생
```

의존성 방향:

```
features → domain ← data
              ↑
              ml
```

- `domain`은 어떤 것에도 의존하지 않음 (순수 Kotlin 인터페이스)
- `data`, `ml`이 domain의 인터페이스를 구현

---

## 지원 콘텐츠 타입

| 타입 | 소스 | 딥링크 |
|------|------|--------|
| YouTube | youtube.com, youtu.be | 유튜브 앱 / 브라우저 |
| Instagram | instagram.com | 인스타 앱 / 브라우저 |
| 네이버지도 | map.naver.com, naver.me | nmap:// scheme |
| 구글맵 | maps.google.com, maps.app.goo.gl | 구글맵 앱 / 브라우저 |
| 쿠팡 | coupang.com, coupa.ng | 쿠팡 앱 / 브라우저 |
| 웹 | 기타 URL | Chrome Custom Tab |

---

## 검색

두 가지 검색을 병렬 실행하고 결과를 병합합니다:

1. **키워드 검색** — Room LIKE 쿼리 (제목, 설명, URL 대상)
2. **시맨틱 검색** — ONNX Runtime 온디바이스 임베딩 → 코사인 유사도

키워드 매치 결과를 우선 배치하고, 시맨틱 유사도 결과를 보조로 사용합니다.

---

## 빌드

```bash
# 디버그 빌드
./gradlew assembleDebug

# 테스트 실행
./gradlew test
```

### 요구사항

| 항목 | 버전 |
|------|------|
| JDK | 17+ |
| Kotlin | 2.0+ |
| Gradle | 8.x (Kotlin DSL) |
| AGP | 8.7+ |
| minSdk | 26 (Android 8.0) |
| targetSdk | 35 (Android 15) |

---

## 개발 문서

| 문서 | 내용 |
|------|------|
| [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) | 프로젝트 전체 규칙 허브 |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | 아키텍처, DI, 데이터 흐름 |
| [CODING_CONVENTIONS.md](docs/CODING_CONVENTIONS.md) | 네이밍, 코드 스타일 |
| [TESTING.md](docs/TESTING.md) | 테스트 패턴, 빌드 명령 |
| [COMPOSE_GUIDE.md](docs/COMPOSE_GUIDE.md) | Circuit 패턴, Navigation |
| [UX_GUIDE.md](docs/UX_GUIDE.md) | 저장 플로우, 화면 설계 |
| [PHASES.md](docs/PHASES.md) | 단계별 개발 계획 + 진행 상태 |
