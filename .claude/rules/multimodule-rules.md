# Multi-Module Architecture Rules

멀티모듈 구조 관련 코드 작성/리뷰 시 따르는 규칙.

---

## 1. 모듈 구조

```
project/
├── app/                          (android.application)
├── core/
│   ├── domain/                   (kotlin.jvm)
│   ├── data/                     (android.library)
│   ├── ml/                       (android.library)
│   └── ui/                       (android.library)
└── feature/
    ├── <feature>/
    │   ├── api/                  (android.library)
    │   └── impl/                 (android.library)
    └── <standalone-feature>/     (android.library)
```

### 모듈 역할

| 모듈 | 역할 | 플러그인 |
|------|------|----------|
| `app` | DI aggregation, Application, MainActivity | android.application |
| `core:domain` | 인터페이스, 모델, 순수 비즈니스 로직 | kotlin.jvm |
| `core:data` | Repository 구현, Room, DataStore, DI | android.library |
| `core:ml` | ML 모델, 임베딩, 벡터 검색, DI | android.library |
| `core:ui` | Compose 테마, 공통 UI 컴포넌트, 유틸리티 | android.library |
| `feature:*:api` | Screen 정의 (Screen + State + Event) | android.library |
| `feature:*:impl` | Presenter, UI Composable | android.library |

---

## 2. 의존성 규칙

### 허용되는 의존성 방향

```
app → 모든 모듈

core:data → core:domain
core:ml → core:domain
core:ui → core:domain

feature:*:api → core:domain
feature:*:impl → feature:*:api, 다른 feature:*:api, core:domain, core:ui
```

### 금지되는 의존성

- **core:domain → Android 의존성** — 순수 Kotlin/JVM만 허용
- **core:data ↔ core:ml** — 상호 의존 금지. 둘 다 core:domain만 참조
- **feature:*:api → feature:*:impl** — api가 impl에 의존 금지
- **feature:*:impl → 다른 feature:*:impl** — impl 간 직접 의존 금지
- **core:* → feature:*** — core가 feature에 의존 금지
- **순환 의존성** — 어떤 경우에도 금지

### 의존성 검증

새 의존성 추가 시 반드시 확인:
1. 위 규칙에 위반하지 않는가?
2. `implementation`인가 `api`인가? (기본: `implementation`, 전이 필요 시에만 `api`)
3. 순환이 발생하지 않는가?

---

## 3. api/impl 분리 원칙

### api 모듈

- **Screen 클래스 1개만** 포함 (Screen + State + Event)
- 최소 의존성: `core:domain`, `circuit-foundation`, `kotlin-parcelize`
- Hilt, KSP, Circuit codegen 불필요
- 다른 모듈이 네비게이션을 위해 참조하는 진입점

### impl 모듈

- **Presenter + UI Composable** 포함
- `@CircuitInject` + `circuit.codegen.mode=hilt` 설정
- Hilt, KSP 플러그인 적용
- 테스트 코드 포함 (Presenter 테스트)
- 내부 구현은 `internal` 접근 제한 권장

### 새 Feature 추가 시

1. `feature/<name>/api/` 모듈 생성 → Screen 정의
2. `feature/<name>/impl/` 모듈 생성 → Presenter + UI
3. `settings.gradle.kts`에 include
4. `app/build.gradle.kts`에 의존성 추가
5. 다른 feature에서 네비게이션 필요 시 해당 feature의 api만 의존

---

## 4. 패키지 네이밍

```
com.kangraemin.stash.domain.*          → core:domain
com.kangraemin.stash.data.*            → core:data
com.kangraemin.stash.ml.*              → core:ml
com.kangraemin.stash.ui.*              → core:ui
com.kangraemin.stash.feature.<name>.api    → feature:<name>:api
com.kangraemin.stash.feature.<name>.impl   → feature:<name>:impl
com.kangraemin.stash.share.*           → feature:share
```

### namespace 규칙 (build.gradle.kts)

```kotlin
android {
    namespace = "com.kangraemin.stash.<module-path>"
    // 예: "com.kangraemin.stash.core.data"
    // 예: "com.kangraemin.stash.feature.home.api"
}
```

---

## 5. DI 규칙

### Hilt 모듈 위치

- **구현체가 있는 모듈에 DI 모듈을 둔다.**
- `core:data` → DatabaseModule, RepositoryModule
- `core:ml` → MlModule
- `app` → CircuitModule (Factory 수집)

### Hilt 모듈 패키지

```
com.kangraemin.stash.data.di.DatabaseModule      (core:data)
com.kangraemin.stash.ml.di.MlModule              (core:ml)
com.kangraemin.stash.di.CircuitModule             (app)
```

### 인터페이스 바인딩 원칙

- 인터페이스: `core:domain`에 정의
- 구현체: `core:data` 또는 `core:ml`에 정의
- `@Binds`: 구현체가 있는 모듈의 DI 모듈에서 바인딩
- Hilt가 `@InstallIn(SingletonComponent::class)`를 자동으로 모듈 간 수집

---

## 6. 공유 코드 규칙

### 2개 이상 feature에서 사용하는 UI 컴포넌트

→ `core:ui`로 이동

### Android 의존성이 있는 유틸리티

→ `core:ui` (UI 관련) 또는 해당 core 모듈

### 순수 Kotlin 유틸리티

→ `core:domain`

### 테스트 픽스처 (Fake, Mock)

→ 해당 모듈의 `testFixtures` 소스셋 (`java-test-fixtures` 플러그인)
→ 사용하는 모듈에서 `testImplementation(testFixtures(project(":core:domain")))`

---

## 7. 빌드 설정 규칙

### Version Catalog (`libs.versions.toml`)

- 모든 의존성은 version catalog로 관리
- 버전 직접 명시 금지 (`implementation("group:name:1.0")` 금지)

### 공통 설정

```
compileSdk = 35
minSdk = 26
targetSdk = 35
jvmTarget = "17"
```

### kotlin.jvm 모듈 (core:domain)

```kotlin
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

- Android SDK, Compose, Hilt 의존성 금지
- `kotlinx-coroutines-core`까지만 허용

---

## 8. 체크리스트

새 모듈/파일 추가 시:

- [ ] 의존성 방향이 위 규칙을 따르는가?
- [ ] core:domain에 Android import가 없는가?
- [ ] api 모듈에 구현 코드가 없는가? (Screen 정의만)
- [ ] impl 모듈의 Presenter/UI가 올바른 api를 참조하는가?
- [ ] 공유 UI 컴포넌트가 core:ui에 있는가?
- [ ] DI 모듈이 구현체와 같은 모듈에 있는가?
- [ ] settings.gradle.kts에 include 되었는가?
- [ ] namespace가 규칙을 따르는가?
