# Testing Rules

테스트 코드 작성 시 따르는 규칙.

---

## 1. 테스트 피라미드

| 레벨 | 대상 | 도구 | 속도 |
|------|------|------|------|
| Unit | Presenter, Repository, Mapper, UseCase | JUnit5 + MockK + Turbine | 빠름 |
| Integration | Room DAO, WorkManager | JUnit5 + Robolectric | 보통 |
| UI/Screenshot | Compose 화면 | Compose Test + Roborazzi | 느림 |

---

## 2. Presenter 테스트 (Circuit)

```kotlin
class HomePresenterTest {
    private val fakeRepository = FakeContentRepository()

    @Test
    fun `콘텐츠 로딩 성공`() = runTest {
        val presenter = HomePresenter(
            navigator = FakeNavigator(),
            contentRepository = fakeRepository,
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitItem()
            assertEquals(expectedContents, state.contents)
        }
    }
}
```

### Presenter 테스트 규칙

- `moleculeFlow` + Turbine `test { }` 사용
- `FakeRepository` 사용 (MockK보다 Fake 선호)
- 이벤트 발생: `state.eventSink(Event.OnClicked)`
- 네비게이션 검증: `FakeNavigator`로 `goTo()` 호출 확인

---

## 3. Repository 테스트

```kotlin
class ContentRepositoryImplTest {
    private val fakeDao = FakeContentDao()

    @Test
    fun `getAll은 Entity를 Domain 모델로 변환한다`() = runTest {
        fakeDao.insertAll(listOf(testEntity))

        val result = repository.getAll().first()

        assertEquals(1, result.size)
        assertEquals(testEntity.title, result[0].title)
    }
}
```

---

## 4. Mapper 테스트

```kotlin
@Test
fun `ContentEntity를 SavedContent로 변환`() {
    val entity = ContentEntity(id = "1", title = "Test", ...)
    val domain = entity.toDomain()

    assertEquals("1", domain.id)
    assertEquals("Test", domain.title)
}
```

---

## 5. 테스트 픽스처

### Fake 사용 (Mock보다 선호)

```kotlin
// core:domain testFixtures에 위치
class FakeContentRepository : ContentRepository {
    private val contents = MutableStateFlow<List<SavedContent>>(emptyList())

    override fun getAll(): Flow<List<SavedContent>> = contents

    override suspend fun insert(content: SavedContent) {
        contents.update { it + content }
    }

    // ... 나머지 구현
}
```

### Fake vs Mock 선택 기준

| 상황 | 선택 |
|------|------|
| Repository, Service 인터페이스 | **Fake** (동작 시뮬레이션) |
| Android 프레임워크 객체 | **MockK** (Context, Intent 등) |
| 외부 라이브러리 | **MockK** |
| 단순 반환값 확인 | **MockK** |

---

## 6. 테스트 네이밍

```kotlin
@Test
fun `빈 쿼리 입력 시 빈 결과 반환`() = runTest { ... }

@Test
fun `삭제 확인 시 Repository의 delete 호출`() = runTest { ... }

@Test
fun `네트워크 에러 시 에러 상태 표시`() = runTest { ... }
```

- **한글 백틱** 사용
- `상황 시 기대 결과` 패턴

---

## 7. Coroutine 테스트

```kotlin
@Test
fun `디바운스 검색`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)

    // presenter 생성 시 testDispatcher 주입
    presenter.search("query")

    advanceTimeBy(300)  // 디바운스 대기
    advanceUntilIdle()  // 코루틴 완료 대기

    assertEquals(expected, results)
}
```

### 규칙

- `runTest` 블록 사용
- `StandardTestDispatcher` 주입
- `advanceUntilIdle()` / `advanceTimeBy(ms)` 로 시간 제어
- `Turbine`의 `test { awaitItem() }` 로 Flow 검증

---

## 8. 실행 명령

```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :core:data:test
./gradlew :feature:home:impl:test

# 특정 클래스
./gradlew test --tests "*.HomePresenterTest"
```

---

## 9. 테스트 체크리스트

### Presenter 테스트 필수 항목

- [ ] 초기 상태 검증
- [ ] 데이터 로딩 성공
- [ ] 에러 발생 시 에러 상태
- [ ] 각 Event 처리 검증
- [ ] 네비게이션 호출 검증

### Repository 테스트 필수 항목

- [ ] CRUD 동작 검증
- [ ] Entity ↔ Domain 변환 검증
- [ ] Flow 데이터 변경 감지
- [ ] 에러 케이스

### 새 기능 추가 시

- [ ] Presenter 테스트 작성
- [ ] 관련 Repository 테스트 업데이트
- [ ] 기존 테스트 깨지지 않는지 확인
