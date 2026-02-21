# Phase 계획

Stash Android 앱의 단계별 개발 계획.

---

## Phase 0: 프로젝트 셋업
상태: 완료 ✅

### Step 0.1: Gradle 프로젝트 생성
- 구현: Android 프로젝트 생성 (Empty Compose Activity), `com.kangraemin.stash` 패키지, Kotlin DSL
- 완료 기준: `./gradlew assembleDebug` 빌드 성공

### Step 0.2: Version Catalog 설정
- 구현: `gradle/libs.versions.toml`에 전체 의존성 버전 정의 (Circuit, Hilt, Room, Coil, ONNX Runtime, Turbine, Molecule, MockK, JUnit5, kotlinx-coroutines-test)
- 완료 기준: Version Catalog 파일이 존재하고, 빌드 스크립트에서 참조 가능

### Step 0.3: 앱 수준 의존성 추가
- 구현: `build.gradle.kts`에 Version Catalog 기반 의존성 선언 (implementation, testImplementation, kapt/ksp)
- 완료 기준: `./gradlew dependencies` 에서 모든 의존성 resolve 성공

### Step 0.4: Hilt 설정
- 구현: Hilt Gradle 플러그인 적용, `StashApplication.kt`에 `@HiltAndroidApp` 어노테이션, `AndroidManifest.xml` 등록
- 완료 기준: 빌드 성공, Hilt 어노테이션 프로세싱 동작 확인

### Step 0.5: 디렉토리 구조 생성
- 구현: ARCHITECTURE.md에 정의된 패키지 구조 생성 (`app/`, `features/`, `domain/`, `data/`, `ml/`, `share/`, `di/`)
- 완료 기준: 패키지 구조가 존재하고 빌드 성공

### Step 0.6: Circuit 기본 설정
- 구현: Circuit 라이브러리 설정, `MainActivity.kt`에 `NavigableCircuitContent` + `rememberSaveableBackStack` 초기화
- 완료 기준: 빈 Circuit 화면이 표시되고 빌드 성공

### Step 0.7: Material 3 테마 설정
- 구현: `StashTheme` 정의 (Dynamic Color 지원, 라이트/다크 모드), `Color.kt`, `Theme.kt`, `Type.kt`
- 완료 기준: 테마 적용된 빈 화면이 빌드/실행 성공

### Step 0.8: JUnit5 + 테스트 환경 설정
- 구현: JUnit5 Gradle 플러그인, Turbine + Molecule + MockK 테스트 의존성 확인, 샘플 테스트 작성
- 완료 기준: `./gradlew test` 실행 시 샘플 테스트 통과

### Step 0.9: .gitignore 설정
- 구현: Android 표준 `.gitignore` 작성 (빌드 산출물, IDE 설정, 로컬 프로퍼티 등 제외)
- 완료 기준: 불필요한 파일이 git 추적에서 제외됨

### Step 0.10: 전체 빌드 + 테스트 검증
- 구현: Phase 0 전체 결과물에 대한 최종 빌드 및 테스트 실행
- 완료 기준: `./gradlew assembleDebug` + `./gradlew test` 모두 성공, warning 0

---

## Phase 1: 도메인 모델 + 데이터 계층
상태: 완료 ✅

> 핵심 도메인 모델을 정의하고, Room DB로 영속화하며, Repository 패턴으로 데이터 접근을 추상화한다.

### Step 1.1: ContentType enum 정의
- 구현: `domain/model/ContentType.kt` — YOUTUBE, INSTAGRAM, NAVER_MAP, GOOGLE_MAP, COUPANG, WEB 등 콘텐츠 타입 열거
- 완료 기준: 빌드 성공, ARCHITECTURE.md의 콘텐츠 타입 표와 일치

### Step 1.2: SavedContent 도메인 모델 정의
- 구현: `domain/model/SavedContent.kt` — id, title, url, contentType, thumbnailUrl, description, createdAt 등 핵심 필드 정의 (data class)
- 완료 기준: 빌드 성공, 테스트용 mock companion 포함

### Step 1.3: ContentRepository 인터페이스 정의
- 구현: `domain/repository/ContentRepository.kt` — save, getAll (Flow), getById, delete 메서드 선언
- 완료 기준: 빌드 성공, 순수 Kotlin (외부 의존 없음)

### Step 1.4: Room Entity 정의
- 구현: `data/room/ContentEntity.kt` — Room @Entity 어노테이션, 테이블명, 컬럼 정의 (SavedContent 필드 매핑)
- 완료 기준: 빌드 성공, Room 어노테이션 정상 적용

### Step 1.5: Room DAO 정의
- 구현: `data/room/ContentDao.kt` — @Insert, @Query (getAll, getById, search), @Delete 메서드 정의
- 완료 기준: 빌드 성공, Flow 반환 타입 사용

### Step 1.6: Room Database 정의
- 구현: `data/room/StashDatabase.kt` — @Database 어노테이션, entities 등록, DAO 접근 메서드, 버전 1
- 완료 기준: 빌드 성공, Room 컴파일러 오류 없음

### Step 1.7: Entity-도메인 모델 Mapper 작성
- 구현: `data/mapper/ContentMapper.kt` — ContentEntity ↔ SavedContent 변환 함수 (확장 함수 또는 클래스)
- 완료 기준: 빌드 성공, 양방향 변환 가능

### Step 1.8: ContentRepositoryImpl 구현
- 구현: `data/repository/ContentRepositoryImpl.kt` — ContentRepository 인터페이스 구현, ContentDao + ContentMapper 주입, Flow 매핑
- 완료 기준: 빌드 성공, @Inject constructor 사용

### Step 1.9: Repository Hilt Module 바인딩
- 구현: `di/RepositoryModule.kt` — @Module @InstallIn(SingletonComponent) + @Binds로 ContentRepositoryImpl → ContentRepository 바인딩, `di/DatabaseModule.kt` — Room Database + DAO @Provides
- 완료 기준: 빌드 성공, Hilt 컴파일 오류 없음

### Step 1.10: Repository 단위 테스트
- 구현: `ContentMapperTest` (매핑 정확성), `FakeContentRepository` (테스트용 Fake 구현), `ContentRepositoryImplTest` (Room In-Memory DB 기반 통합 테스트)
- 완료 기준: `./gradlew test` 모든 테스트 통과

---

## Phase 2: 홈 화면
상태: 진행중 🔄

> 메인 화면 구현. 저장된 콘텐츠를 2열 그리드로 표시하고, 콘텐츠 타입 필터 칩으로 분류한다.

### Step 2.1: HomeScreen Screen 정의
- 구현: `features/home/HomeScreen.kt` — @Parcelize data object HomeScreen : Screen, 내부에 State (contents, selectedFilter, eventSink) + sealed interface Event (OnFilterSelected, OnContentClicked) 정의
- 완료 기준: 빌드 성공, COMPOSE_GUIDE.md의 Screen 패턴과 일치

### Step 2.2: HomePresenter 구현
- 구현: `features/home/HomePresenter.kt` — @CircuitInject Presenter, ContentRepository 주입, getAll() Flow 수집, 필터링 로직, eventSink 처리
- 완료 기준: 빌드 성공, @CircuitInject + @Inject constructor 사용

### Step 2.3: 콘텐츠 카드 Composable 작성
- 구현: `features/home/ContentCard.kt` — ContentCard Composable, SavedContent를 받아 썸네일 + 제목 + 소스 표시, Coil로 이미지 로딩, Material 3 Card 사용
- 완료 기준: 빌드 성공, @Preview 포함

### Step 2.4: 필터 칩 Composable 작성
- 구현: `features/home/FilterChips.kt` — FilterChips Composable, ContentType 기반 필터 칩 행 (전체/영상/장소/쇼핑/아티클/인스타), Material 3 FilterChip 사용, 선택 상태 표시
- 완료 기준: 빌드 성공, @Preview 포함

### Step 2.5: 홈 화면 전체 UI 조립
- 구현: `features/home/Home.kt` — @CircuitInject Home Composable, FilterChips + LazyVerticalGrid(2열) + ContentCard 조합, 빈 상태 표시, PlaceholderScreen을 HomeScreen으로 교체 (MainActivity backStack 초기 화면 변경)
- 완료 기준: 빌드 성공, 빈 상태/콘텐츠 목록 모두 표시 가능

### Step 2.6: HomePresenter 테스트
- 구현: `HomePresenterTest.kt` — Molecule + Turbine 기반, 콘텐츠 로드 테스트, 필터 선택 테스트, 빈 목록 테스트, FakeContentRepository 사용
- 완료 기준: `./gradlew test` 모든 테스트 통과

### Step 2.7: Home UI Preview 작성
- 구현: Home, ContentCard, FilterChips 각각의 @Preview 함수 정리, StashTheme 적용, 다양한 상태 (빈 목록, 필터 선택, 콘텐츠 있음)
- 완료 기준: 빌드 성공, 모든 Preview 렌더링 가능

---

## Phase 3: Share Intent 수신
상태: 대기 ⏳

> 외부 앱에서 공유된 URL을 Stash에 저장하는 기능. Android Share Intent 수신 처리.

### 예상 Step (Phase 진입 시 상세화)
- URL 파싱 유틸리티 (도메인 → ContentType 매핑)
- ShareActivity 구현 (Intent 수신 + URL 추출)
- 저장 로직 연결 (ShareActivity → Repository)
- Intent Filter 매니페스트 등록
- WorkManager로 메타데이터 추출 백그라운드 작업 설정
- 메타데이터 추출 Worker 구현 (OG 태그 파싱)
- Share Intent 흐름 테스트

---

## Phase 4: 상세 화면 + 딥링크
상태: 대기 ⏳

> 저장된 콘텐츠의 상세 정보 표시 + 원본 앱/웹으로 이동하는 딥링크 처리.

### 예상 Step (Phase 진입 시 상세화)
- `DetailScreen` Screen + State + Event 정의
- `DetailPresenter` 구현 (콘텐츠 상세 로드)
- 상세 화면 UI 작성 (소스별 레이아웃)
- 딥링크 유틸리티 (ContentType별 Intent/CustomTab 분기)
- Chrome Custom Tab 연동
- 홈 → 상세 Navigation 연결
- 콘텐츠 삭제 기능
- DetailPresenter 테스트

---

## Phase 5: 검색
상태: 대기 ⏳

> 키워드 검색 + ONNX Runtime 기반 온디바이스 시맨틱 검색 구현.

### 예상 Step (Phase 진입 시 상세화)
- Room LIKE 기반 키워드 검색 DAO 메서드 추가
- `SearchScreen` Screen + State + Event 정의
- `SearchPresenter` 구현 (디바운스 키워드 검색)
- 검색 UI (검색바 + 결과 리스트)
- ONNX Runtime 모델 로딩 설정
- 텍스트 임베딩 서비스 구현
- 벡터 유사도 검색 서비스 구현
- 키워드 + 시맨틱 검색 결과 병합 랭킹
- 검색 테스트

---

## Phase 6: 설정 + 마무리
상태: 대기 ⏳

> 설정 화면, 에러 처리, 접근성, 최종 품질 다듬기.

### 예상 Step (Phase 진입 시 상세화)
- `SettingsScreen` 구현 (테마 설정, 데이터 관리)
- 에러 처리 통합 (네트워크 없음, DB 오류 등)
- 빈 상태 / 로딩 상태 UI 통일
- 접근성 (contentDescription, 터치 영역)
- 전체 UI 테스트 + 통합 테스트
- 성능 최적화 (Compose 리컴포지션, 이미지 캐시)
