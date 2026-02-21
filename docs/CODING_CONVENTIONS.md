# Coding Conventions

---

## 1. 네이밍

| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스/인터페이스 | UpperCamelCase | `SavedContent`, `ContentType` |
| 함수, 변수 | lowerCamelCase | `fetchContent()`, `searchQuery` |
| 상수 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 패키지 | 소문자 | `com.kangraemin.stash.domain.model` |
| Circuit Screen | `{화면명}Screen` | `HomeScreen` |
| Circuit Presenter | `{화면명}Presenter` | `HomePresenter` |
| Composable (UI) | `{화면명}` | `Home`, `Detail` |
| UiState | `{화면명}UiState` | `HomeUiState` |
| Event (사용자) | `{동사}{목적어}` | `OnSaveClicked`, `OnFilterSelected` |
| Event (내부) | `{명사}{결과}` | `ContentLoaded`, `SaveFailed` |
| Repository | `{도메인}Repository` | `ContentRepository` |
| Room Entity | `{도메인}Entity` | `ContentEntity` |
| 도메인 모델 | 접미사 없음 | `SavedContent` |
| DAO | `{도메인}Dao` | `ContentDao` |

---

## 2. 파일 구성

- 파일당 하나의 주요 클래스.
- Circuit: `{Name}Screen.kt` (Screen + Presenter + UI 함수) 또는 분리 가능.
- UiState는 별도 파일: `{Name}UiState.kt`.

---

## 3. 접근 제어

- 기본 `internal` (명시 안 함). 클래스 내부만 `private`.
- `open` 최소화, 상속보다 합성.

---

## 4. import 정렬

IDE 기본 설정 (알파벳 순, 와일드카드 import 금지).

```kotlin
import androidx.compose.foundation.layout.Column
import com.kangraemin.stash.domain.model.SavedContent
import dagger.hilt.android.lifecycle.HiltViewModel
```

---

## 5. Circuit 규칙

- Side effect는 `LaunchedEffect` / `rememberCoroutineScope`로 처리.
- 상태는 `mutableStateOf` / `remember`로 관리.
- Navigation: Circuit의 `Navigator`로 push/pop.
- Event는 람다 콜백으로 UI → Presenter 전달.

---

## 6. Compose 규칙

- Composable 함수 100줄 초과 시 하위 Composable 분리.
- 모든 Screen에 `@Preview` 작성.
- State hoisting: UI는 stateless, state는 Presenter가 소유.

---

## 7. 주석

- **Why**만 적는다. What은 코드로.
- KDoc은 public API에만.
- TODO: `// TODO: 설명`
