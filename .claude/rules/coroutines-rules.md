# Coroutines Rules

Kotlin Coroutines / Flow 코드 작성 시 따르는 규칙.

---

## 1. Dispatcher 주입

```kotlin
// CORRECT: 테스트에서 교체 가능
class ContentRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun search(query: String) = withContext(ioDispatcher) { ... }
}

// INCORRECT: 하드코딩 — 테스트 불가
class ContentRepositoryImpl {
    suspend fun search(query: String) = withContext(Dispatchers.IO) { ... }
}
```

---

## 2. Main-Safety

- Data/Domain 레이어의 모든 `suspend` 함수는 **Main-safe** 이어야 함
- 호출자가 `Dispatchers.Main`에서 호출해도 안전해야 함
- 무거운 작업은 함수 내부에서 `withContext(ioDispatcher)`로 전환

```kotlin
// Repository 함수는 내부에서 dispatcher 전환
suspend fun generateEmbedding(text: String): List<Float> = withContext(ioDispatcher) {
    // ONNX inference 등 무거운 작업
}
```

---

## 3. Flow 노출 패턴

- **지속적 데이터 변화**: `Flow<T>` 로 노출
- **일회성 결과**: `suspend` 함수로 노출

```kotlin
interface ContentRepository {
    fun getAll(): Flow<List<SavedContent>>           // 지속 관찰
    suspend fun getById(id: String): SavedContent?   // 일회성
    suspend fun delete(id: String)                    // 일회성
}
```

---

## 4. MutableState 캡슐화

```kotlin
// CORRECT: 읽기 전용만 노출
private val _state = MutableStateFlow(UiState())
val state: StateFlow<UiState> = _state.asStateFlow()

// INCORRECT: 외부에서 변경 가능
val state = MutableStateFlow(UiState())
```

Circuit Presenter에서는 Compose State 사용:

```kotlin
// Circuit 패턴: remember + mutableStateOf
var contents by remember { mutableStateOf<List<SavedContent>>(emptyList()) }
```

---

## 5. GlobalScope 금지

- **절대 사용 금지** — 구조적 동시성 파괴
- 대안: `viewModelScope`, `lifecycleScope`, 주입된 `applicationScope`

| 스코프 | 용도 | 생명주기 |
|--------|------|----------|
| Circuit Presenter | @Composable present() 내 | Screen 생명주기 |
| `lifecycleScope` | Activity/Fragment | Destroy 시 취소 |
| `applicationScope` (주입) | 앱 전역 백그라운드 | Application 생명주기 |
| `GlobalScope` | **사용 금지** | 취소 불가 |

---

## 6. 예외 처리

### CancellationException 재던지기

```kotlin
// CORRECT
try {
    doSuspendWork()
} catch (e: CancellationException) {
    throw e  // 반드시 재던지기
} catch (e: Exception) {
    handleError(e)
}

// INCORRECT: 취소를 삼킴
try {
    doSuspendWork()
} catch (e: Exception) {
    handleError(e)  // CancellationException도 여기 걸림
}
```

### Result 래핑

```kotlin
suspend fun search(query: String): Result<List<SavedContent>> = runCatching {
    contentRepository.searchByKeyword(query).first()
}
```

---

## 7. 협력적 취소

```kotlin
// CORRECT: 루프에서 취소 확인
suspend fun processItems(items: List<Item>) {
    items.forEach { item ->
        ensureActive()  // 취소 시 즉시 중단
        processItem(item)
    }
}

// INCORRECT: 취소 확인 없음
suspend fun processItems(items: List<Item>) {
    items.forEach { processItem(it) }  // 취소 불가
}
```

---

## 8. 병렬 실행

```kotlin
// CORRECT: coroutineScope로 구조적 병렬
suspend fun hybridSearch(query: String) = coroutineScope {
    val keyword = async { contentRepository.searchByKeyword(query).first() }
    val semantic = async { vectorSearchService.searchBySimilarity(query) }
    mergeResults(keyword.await(), semantic.await())
}
```

---

## 9. 콜백 → Flow 변환

```kotlin
fun locationUpdates(): Flow<Location> = callbackFlow {
    val listener = LocationListener { trySend(it) }
    registerListener(listener)
    awaitClose { unregisterListener(listener) }
}
```

---

## 10. 테스트

```kotlin
@Test
fun `검색 결과 반환`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val repository = FakeContentRepository()
    val presenter = SearchPresenter(repository, testDispatcher)

    // ...
    advanceUntilIdle()

    assertEquals(expected, results)
}
```

- `runTest` + `StandardTestDispatcher` 사용
- `advanceUntilIdle()`로 코루틴 완료 대기
- Turbine으로 Flow 테스트: `flow.test { assertEquals(expected, awaitItem()) }`
