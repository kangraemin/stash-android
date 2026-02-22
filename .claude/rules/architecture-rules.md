# Architecture Rules

Android 아키텍처 및 레이어 규칙.

---

## 1. 레이어 구조

```
UI (feature) → Domain ← Data
                 ↑
                 ML
```

### 의존성 방향

- **단방향만 허용** — 위 화살표 방향으로만 의존
- Domain은 어떤 것에도 의존하지 않음 (순수 Kotlin)
- Data, ML이 Domain 인터페이스를 구현
- UI(Feature)는 Domain 인터페이스만 참조

---

## 2. Domain 레이어

- **순수 Kotlin** — `android.*` import 금지
- 인터페이스 + 모델 + 유틸리티만 포함
- 프레임워크 의존성 없음 (Hilt 어노테이션도 없음)

```kotlin
// domain/repository/ContentRepository.kt
interface ContentRepository {
    fun getAll(): Flow<List<SavedContent>>
    suspend fun getById(id: String): SavedContent?
    suspend fun insert(content: SavedContent)
    suspend fun delete(id: String)
    fun searchByKeyword(query: String): Flow<List<SavedContent>>
}
```

### Domain에 허용되는 것

- data class (모델)
- interface (repository, service)
- object (유틸리티 — UrlParser 등)
- `kotlinx.coroutines.flow.Flow`
- `java.time.*`, `java.net.URI`

### Domain에 금지되는 것

- `android.*`, `androidx.*`
- Hilt 어노테이션 (`@Inject`, `@Module` 등)
- Room 어노테이션
- Compose 관련
- Context, Intent, Bundle

---

## 3. Data 레이어

### Repository 패턴 (SSOT)

- Room DAO가 `Flow<T>`로 데이터 노출 (Single Source of Truth)
- Repository는 DAO를 감싸고 domain 모델로 변환
- 모든 `suspend` 함수는 **Main-safe**

```kotlin
class ContentRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
) : ContentRepository {
    override fun getAll(): Flow<List<SavedContent>> =
        contentDao.getAll().map { entities -> entities.map { it.toDomain() } }
}
```

### Mapper 규칙

- Entity ↔ Domain 모델 변환은 Mapper에서
- 확장 함수 `toDomain()`, `toEntity()` 패턴

---

## 4. Circuit MVI 패턴

### Screen 정의 (api 모듈)

```kotlin
@Parcelize
data object HomeScreen : Screen, Parcelable {
    @Immutable
    data class State(
        val contents: List<SavedContent> = emptyList(),
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event {
        data class OnContentClicked(val content: SavedContent) : Event
    }
}
```

### Presenter (impl 모듈)

```kotlin
class HomePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val contentRepository: ContentRepository,
) : Presenter<HomeScreen.State> {

    @CircuitInject(HomeScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }

    @Composable
    override fun present(): HomeScreen.State {
        // Compose State 사용
        var contents by remember { mutableStateOf<List<SavedContent>>(emptyList()) }

        LaunchedEffect(Unit) {
            contentRepository.getAll().collect { contents = it }
        }

        return HomeScreen.State(contents = contents) { event ->
            when (event) {
                is HomeScreen.Event.OnContentClicked ->
                    navigator.goTo(DetailScreen(event.content.id))
            }
        }
    }
}
```

### UI Composable (impl 모듈)

```kotlin
@CircuitInject(HomeScreen::class, SingletonComponent::class)
@Composable
fun Home(state: HomeScreen.State, modifier: Modifier = Modifier) {
    // state에서 데이터 렌더링
    // state.eventSink(event)로 이벤트 전달
}
```

### Circuit 규칙

- Screen = `@Parcelize` + State + Event 정의
- Presenter = `@AssistedInject` + `@CircuitInject` Factory
- UI = `@CircuitInject` Composable
- 네비게이션: `navigator.goTo(Screen)`, `navigator.pop()`
- 복잡한 객체 전달 금지 — ID만 전달: `DetailScreen(contentId)`

---

## 5. DI 규칙 (Hilt)

### 진입점

```kotlin
@HiltAndroidApp class StashApplication
@AndroidEntryPoint class MainActivity
@AndroidEntryPoint class ShareActivity
```

### 모듈 위치

- DI 모듈은 **구현체가 있는 Gradle 모듈**에 배치
- `@InstallIn(SingletonComponent::class)` → Hilt 자동 수집

### 바인딩 패턴

```kotlin
// Interface → Impl 바인딩
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository
}

// 인스턴스 제공
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StashDatabase =
        Room.databaseBuilder(context, StashDatabase::class.java, "stash.db").build()
}
```

### Worker 주입

```kotlin
@HiltWorker
class MetadataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val contentRepository: ContentRepository,
) : CoroutineWorker(context, params)
```

---

## 6. 에러 처리 패턴

### Presenter에서 에러 상태 관리

```kotlin
var error by remember { mutableStateOf<String?>(null) }

LaunchedEffect(Unit) {
    try {
        contentRepository.getAll().collect { contents = it }
    } catch (e: Exception) {
        error = e.message ?: "오류가 발생했습니다"
    }
}

return State(error = error)
```

### UI에서 에러 표시

```kotlin
when {
    state.error != null -> ErrorStateView(
        message = state.error,
        onRetry = { state.eventSink(Event.OnRetry) },
    )
    state.contents.isEmpty() -> EmptyStateView()
    else -> ContentList(state.contents)
}
```
