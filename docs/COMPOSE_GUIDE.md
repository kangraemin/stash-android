# Jetpack Compose + Circuit Guide

---

## 1. Screen + Presenter + UI

```kotlin
// Screen 정의 (Navigation key)
@Parcelize
data object HomeScreen : Screen {
    data class State(
        val contents: List<SavedContent> = emptyList(),
        val selectedFilter: ContentType? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event {
        data object OnAppear : Event
        data class OnFilterSelected(val type: ContentType?) : Event
        data class OnContentClicked(val content: SavedContent) : Event
    }
}

// Presenter
@CircuitInject(HomeScreen::class, ActivityComponent::class)
class HomePresenter @Inject constructor(
    private val contentRepository: ContentRepository,
    private val navigator: Navigator,
) : Presenter<HomeScreen.State> {
    @Composable
    override fun present(): HomeScreen.State {
        var contents by remember { mutableStateOf<List<SavedContent>>(emptyList()) }
        var selectedFilter by remember { mutableStateOf<ContentType?>(null) }

        LaunchedEffect(Unit) {
            contentRepository.getAll().collect { contents = it }
        }

        return HomeScreen.State(
            contents = selectedFilter?.let { filter ->
                contents.filter { it.contentType == filter }
            } ?: contents,
            selectedFilter = selectedFilter,
        ) { event ->
            when (event) {
                is HomeScreen.Event.OnFilterSelected -> selectedFilter = event.type
                is HomeScreen.Event.OnContentClicked -> navigator.goTo(DetailScreen(event.content.id))
                else -> {}
            }
        }
    }
}

// UI
@CircuitInject(HomeScreen::class, ActivityComponent::class)
@Composable
fun Home(state: HomeScreen.State, modifier: Modifier = Modifier) {
    Column(modifier) {
        FilterChips(
            selected = state.selectedFilter,
            onSelected = { state.eventSink(HomeScreen.Event.OnFilterSelected(it)) }
        )
        ContentGrid(
            contents = state.contents,
            onClicked = { state.eventSink(HomeScreen.Event.OnContentClicked(it)) }
        )
    }
}
```

---

## 2. Navigation

### 화면 이동
```kotlin
// Navigator로 push
navigator.goTo(DetailScreen(contentId = "123"))

// 뒤로가기
navigator.pop()
```

### Circuit Navigator 설정
```kotlin
// MainActivity에서 NavigableCircuitContent 사용
setContent {
    val backStack = rememberSaveableBackStack(HomeScreen)
    val navigator = rememberCircuitNavigator(backStack)
    NavigableCircuitContent(navigator, backStack)
}
```

---

## 3. 리스트

```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
) {
    items(state.contents, key = { it.id }) { content ->
        ContentCard(
            content = content,
            onClick = { state.eventSink(HomeScreen.Event.OnContentClicked(content)) }
        )
    }
}
```

---

## 4. 비동기

```kotlin
// Presenter 내 데이터 로딩
@Composable
override fun present(): HomeScreen.State {
    var contents by remember { mutableStateOf<List<SavedContent>>(emptyList()) }

    // 화면 진입 시 로딩
    LaunchedEffect(Unit) {
        contentRepository.getAll().collect { contents = it }
    }

    // 디바운스 검색
    var searchQuery by remember { mutableStateOf("") }
    LaunchedEffect(searchQuery) {
        delay(300)
        val results = searchRepository.search(searchQuery)
        // update state
    }

    return HomeScreen.State(...)
}
```

---

## 5. Dialog / BottomSheet

```kotlin
// Presenter에서 상태 관리
var showDeleteDialog by remember { mutableStateOf(false) }

// UI에서 표시
if (state.showDeleteDialog) {
    AlertDialog(
        onDismissRequest = { state.eventSink(Event.OnDeleteDismissed) },
        confirmButton = {
            TextButton(onClick = { state.eventSink(Event.OnDeleteConfirmed) }) {
                Text("삭제")
            }
        },
        title = { Text("콘텐츠 삭제") },
        text = { Text("이 콘텐츠를 삭제하시겠습니까?") },
    )
}
```

---

## 6. Preview

```kotlin
@Preview
@Composable
private fun HomePreview() {
    StashTheme {
        Home(
            state = HomeScreen.State(
                contents = listOf(SavedContent.mock),
                selectedFilter = null,
            )
        )
    }
}
```

모든 Screen UI에 `@Preview` 작성 필수.
