# Compose UI Rules

Jetpack Compose 코드 작성 시 따르는 규칙.

---

## 1. State Hoisting (단방향 데이터 흐름)

```kotlin
// CORRECT: State 내려보내기, Event 올려보내기
@Composable
fun MyComponent(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
)

// INCORRECT: 내부에서 상태 관리
@Composable
fun MyComponent() {
    var value by remember { mutableStateOf("") }
}
```

Circuit MVI에서는 Screen.State가 상태, Screen.Event가 이벤트:

```kotlin
@Composable
fun HomeContent(state: HomeScreen.State, modifier: Modifier = Modifier) {
    // state.contents → 데이터 렌더링
    // state.eventSink(Event.OnClicked) → 이벤트 발생
}
```

---

## 2. Modifier 규칙

- **첫 번째 선택 파라미터**로 `modifier: Modifier = Modifier` 선언
- **루트 레이아웃**에 modifier 적용
- 순서가 중요: `padding().clickable()` ≠ `clickable().padding()`

```kotlin
@Composable
fun ContentCard(
    content: SavedContent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,  // ← 항상 이 위치
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),  // ← 루트에 적용
    ) { ... }
}
```

---

## 3. 성능 최적화

### remember

```kotlin
// BAD: 매 recomposition마다 정렬
val sorted = items.sortedBy { it.name }

// GOOD: 키 변경 시에만 재계산
val sorted = remember(items) { items.sortedBy { it.name } }
```

### derivedStateOf

```kotlin
// 자주 변하는 상태에서 파생 값 계산
val showButton by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}
```

### Lambda 안정성

```kotlin
// BAD: 매번 새 람다 생성
Button(onClick = { viewModel.doSomething(item) })

// GOOD: remember로 안정화
val onClick = remember(item) { { viewModel.doSomething(item) } }
Button(onClick = onClick)
```

### LazyColumn 키

```kotlin
// BAD: 인덱스 기반 (아이템 변경 시 전체 재구성)
LazyColumn { items(items) { ItemRow(it) } }

// GOOD: 안정적 키 사용
LazyColumn {
    items(items, key = { it.id }) { item -> ItemRow(item) }
}
```

### 불안정 타입 처리

| 타입 | 안정? | 해결 |
|------|-------|------|
| Int, String, Boolean | O | - |
| data class (안정 필드) | O | 모든 필드가 안정인지 확인 |
| List, Map, Set | **X** | `@Immutable` + ImmutableList 또는 `remember` |
| var 프로퍼티 | **X** | `@Stable` 어노테이션 |
| 람다 | **X** | `remember { }` |

### State 읽기 시점

```kotlin
// BAD: composition에서 읽기 (불필요한 recomposition)
Box(modifier = Modifier.offset(y = scrollState.value.dp))

// GOOD: layout 단계로 지연
Box(modifier = Modifier.offset { IntOffset(0, scrollState.value) })
```

---

## 4. Coil 이미지 로딩

```kotlin
// 기본: AsyncImage (권장)
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(content.thumbnailUrl)
        .crossfade(true)
        .build(),
    contentDescription = content.title,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
)
```

### 규칙

- **AsyncImage 우선** — SubcomposeAsyncImage는 LazyColumn에서 느림
- **crossfade(true)** 항상 활성화
- **contentDescription** — 의미 있는 설명 또는 장식용이면 `null`
- **ImageLoader 싱글톤** — Application에서 한 번만 설정

---

## 5. 접근성

### 콘텐츠 설명

```kotlin
// 의미 있는 요소: 설명 제공
Icon(Icons.Default.Delete, contentDescription = "삭제")

// 장식용: null
Image(painter, contentDescription = null)
```

### 터치 영역

- 최소 **48x48dp** — 작은 아이콘은 Box로 감싸서 패딩 추가

### 색상 대비

- 일반 텍스트: **4.5:1** (WCAG AA)
- 큰 텍스트/아이콘: **3.0:1**

### 시맨틱

```kotlin
// 복합 아이템 그룹화
Modifier.semantics(mergeDescendants = true)

// 제목 표시 (스크린 리더 점프)
Modifier.semantics { heading() }
```

---

## 6. 테마

- `MaterialTheme.colorScheme` 사용 — 하드코딩 색상 금지
- `MaterialTheme.typography` 사용 — 하드코딩 텍스트 스타일 금지

```kotlin
Text(
    text = content.title,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.onSurface,
)
```

---

## 7. Preview

```kotlin
@Preview(showBackground = true)
@Composable
private fun ContentCardPreview() {
    StashTheme {
        ContentCard(
            content = SavedContent.mock,
            onClick = {},
        )
    }
}
```

- Light/Dark 모드 프리뷰 권장
- `StashTheme`으로 감싸기
- 더미 데이터 사용 (`SavedContent.mock`)
