# Architecture

Stash Android 앱의 아키텍처 가이드.

---

## 1. 기술 스택

| 항목 | 선택 |
|------|------|
| UI | Jetpack Compose |
| 아키텍처 | MVI (Circuit by Slack) |
| DI | Hilt |
| 데이터 | Room (로컬 우선) |
| AI/검색 | ONNX Runtime (온디바이스 임베딩 + 시맨틱 검색) |
| 최소 타겟 | API 26 (Android 8.0) |
| 테스트 | JUnit5 + Turbine |

---

## 2. 프로젝트 구조

```
app/src/main/java/com/kangraemin/stash/
├── app/                           # 앱 진입점
│   ├── StashApplication.kt
│   └── MainActivity.kt
│
├── features/                      # Circuit Screen 모듈 (화면 단위)
│   ├── home/
│   │   ├── HomeScreen.kt          # Circuit Presenter + UI
│   │   └── HomeUiState.kt
│   ├── search/
│   ├── detail/
│   ├── categorylist/
│   └── settings/
│
├── domain/                        # 비즈니스 로직 (순수 Kotlin, 외부 의존 없음)
│   ├── model/                     # 도메인 모델
│   ├── contentparsing/            # URL/콘텐츠 파싱
│   └── repository/                # Repository 인터페이스 정의
│
├── data/                          # 데이터 계층
│   ├── room/                      # Room Entity + DAO + Database
│   ├── repository/                # Repository 구현
│   └── mapper/                    # Room Entity ↔ 도메인 모델 변환
│
├── ml/                            # ONNX Runtime
│   ├── embedding/
│   └── vectorsearch/
│
├── share/                         # Share Intent 수신 처리
│
└── di/                            # Hilt Module 정의
```

---

## 3. Circuit Screen 패턴

```kotlin
@CircuitInject(HomeScreen::class, ActivityComponent::class)
class HomePresenter @Inject constructor(
    private val contentRepository: ContentRepository,
) : Presenter<HomeUiState> {
    @Composable
    override fun present(): HomeUiState {
        // state 관리 + event 처리
    }
}

@CircuitInject(HomeScreen::class, ActivityComponent::class)
@Composable
fun Home(state: HomeUiState, modifier: Modifier = Modifier) {
    // UI 렌더링
}
```

Circuit의 핵심:
- **Presenter**: 상태 생산 + 이벤트 처리 (비즈니스 로직)
- **UI (Composable)**: 순수 렌더링 (state → UI, event → Presenter)
- **Screen**: 화면 식별자 + Navigation key

---

## 4. 의존성 주입 (DI)

Hilt 사용:

```kotlin
// Repository 인터페이스 (domain/repository/)
interface ContentRepository {
    suspend fun save(content: SavedContent)
    fun getAll(): Flow<List<SavedContent>>
    suspend fun delete(id: String)
}

// 구현 (data/repository/)
class ContentRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
    private val mapper: ContentMapper,
) : ContentRepository { ... }

// Hilt Module (di/)
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository
}
```

**규칙**: Presenter에서 Room/Retrofit 등 직접 접근 금지. 반드시 Repository를 통해 주입.

---

## 5. 데이터 흐름

```
[Share Intent] → ShareActivity → [Room DB]
                                      ↓
[Main App] → Circuit Presenter → Repository → [Room DB] → 도메인 모델
                                                   ↓
                                      [ONNX Embedding] → 벡터 검색
```

- Share Intent: URL만 저장 (빠른 처리)
- 메인 앱: 메타데이터 추출, 임베딩 생성 등 무거운 작업 WorkManager로 백그라운드 처리

---

## 6. 콘텐츠 타입 + 딥링크

| ContentType | 소스 | 딥링크 |
|-------------|------|--------|
| `YOUTUBE` | youtube.com, youtu.be | Intent (앱) / 브라우저 |
| `INSTAGRAM` | instagram.com | Intent (앱) / 브라우저 |
| `NAVER_MAP` | map.naver.com, naver.me | `nmap://` scheme |
| `GOOGLE_MAP` | maps.google.com, maps.app.goo.gl | Intent (앱) / 브라우저 |
| `COUPANG` | coupang.com, coupa.ng | Intent (앱) / 브라우저 |
| `WEB` | 기타 | Chrome Custom Tab |

---

## 7. 모듈 의존성 방향

```
app → features → domain ← data
                    ↑        ↑
                    ml      Room

share → domain ← data
```

- **domain**은 어떤 것에도 의존하지 않는다 (순수 Kotlin).
- **features**는 domain에만 의존한다.
- **data**는 domain의 인터페이스를 구현한다.
