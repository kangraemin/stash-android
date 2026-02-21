# Testing

---

## 1. 프레임워크

| 용도 | 프레임워크 |
|------|-----------|
| 단위 테스트 | JUnit5 (`@Test`, `assertThat`) |
| Flow 테스트 | Turbine |
| Compose UI 테스트 | Compose Testing (`createComposeRule`) |
| Mocking | MockK |
| Coroutine 테스트 | kotlinx-coroutines-test (`runTest`) |

---

## 2. Presenter 테스트 패턴

```kotlin
class HomePresenterTest {
    @Test
    fun `콘텐츠 로드 시 목록에 표시된다`() = runTest {
        val fakeRepository = FakeContentRepository(
            contents = listOf(SavedContent.mock)
        )
        val presenter = HomePresenter(contentRepository = fakeRepository)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitItem()
            assertThat(state.contents).hasSize(1)
        }
    }
}
```

- Presenter 로직을 Molecule + Turbine으로 테스트.
- Repository는 Fake 구현 사용.

---

## 3. 테스트 네이밍

- 클래스명: `{대상}Test` (예: `HomePresenterTest`)
- 함수명: **백틱 한글** (예: `` `콘텐츠 저장 시 목록에 추가된다`() ``)
- `@Nested`로 관련 테스트 그룹핑.

---

## 4. Fake / Mock

```kotlin
// Fake Repository (테스트용)
class FakeContentRepository(
    private val contents: List<SavedContent> = emptyList()
) : ContentRepository {
    override fun getAll(): Flow<List<SavedContent>> = flowOf(contents)
    override suspend fun save(content: SavedContent) { }
    override suspend fun delete(id: String) { }
}

// Mock 도메인 모델
val SavedContent.Companion.mock get() = SavedContent(
    id = "test-id",
    title = "테스트 콘텐츠",
    url = "https://example.com",
    contentType = ContentType.WEB,
    createdAt = Instant.now()
)
```

---

## 5. 빌드/테스트 명령

```bash
# 빌드
./gradlew assembleDebug

# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :app:test

# 특정 테스트 클래스
./gradlew test --tests "com.kangraemin.stash.features.home.HomePresenterTest"

# Lint
./gradlew lint
```

---

## 6. 단계 완료 조건

매 Step 완료 시:
1. 해당 기능 검증 테스트 작성
2. 모든 테스트 통과 (`./gradlew test`)
3. 빌드 성공 (`./gradlew assembleDebug`), warning 최소화
