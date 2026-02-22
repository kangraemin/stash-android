# Gradle Rules

빌드 설정 및 성능 최적화 규칙.

---

## 1. Version Catalog 필수

- 모든 의존성은 `gradle/libs.versions.toml`로 관리
- 버전 직접 명시 금지

```kotlin
// CORRECT
implementation(libs.room.runtime)
ksp(libs.room.compiler)

// INCORRECT
implementation("androidx.room:room-runtime:2.6.1")
```

---

## 2. 공통 설정값

```
compileSdk = 35
minSdk = 26
targetSdk = 35
jvmTarget = "17"
```

모든 Android 모듈에서 동일하게 적용.

---

## 3. 플러그인 관리

### Root build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

### 모듈별 필요한 플러그인만 적용

| 모듈 타입 | 플러그인 |
|-----------|----------|
| kotlin.jvm (domain) | `kotlin.jvm` |
| android.library (UI 없음) | `android.library`, `kotlin.android` |
| android.library (Compose) | + `kotlin.compose` |
| android.library (DI) | + `ksp`, `hilt` |
| android.library (Circuit) | + `ksp`, `hilt`, `kotlin.compose` |
| android.application | 전부 |

---

## 4. 빌드 성능 최적화

### gradle.properties 필수 설정

```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.configuration-cache.problems=warn
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC
android.nonTransitiveRClass=true
```

### KSP 사용 (kapt 금지)

```kotlin
// CORRECT: KSP (2배 빠름)
ksp(libs.hilt.compiler)
ksp(libs.room.compiler)

// INCORRECT: kapt (느림, deprecated 예정)
kapt(libs.hilt.compiler)
```

---

## 5. 의존성 규칙

### implementation vs api

```kotlin
// 기본: implementation (내부에서만 사용)
implementation(project(":core:domain"))

// 전이 노출 필요 시에만 api
api(project(":core:domain"))  // 이 모듈의 public API가 domain 타입을 노출할 때만
```

### 동적 버전 금지

```kotlin
// BAD: 매 빌드마다 해석
implementation("com.example:lib:+")
implementation("com.example:lib:1.0.+")

// GOOD: 고정 버전
implementation(libs.some.library)  // version catalog에서 고정
```

---

## 6. Lazy Task 사용

```kotlin
// CORRECT: 필요할 때만 구성
tasks.register("myTask") { ... }

// INCORRECT: 즉시 구성 (configuration 느려짐)
tasks.create("myTask") { ... }
```

---

## 7. Repository 순서

```kotlin
dependencyResolutionManagement {
    repositories {
        google()       // Android 의존성 (가장 먼저)
        mavenCentral() // 대부분 라이브러리
        // 커스텀 레포는 마지막에
    }
}
```

---

## 8. 빌드 문제 진단

```bash
# 빌드 스캔 생성
./gradlew assembleDebug --scan

# Configuration 시간 측정
./gradlew assembleDebug --profile

# 의존성 트리 확인
./gradlew :app:dependencies
```

### 느린 빌드 원인별 해결

| 원인 | 해결 |
|------|------|
| Configuration 느림 | `tasks.register()`, configuration cache |
| 컴파일 느림 | 모듈 분리, KSP 사용 |
| kapt 느림 | KSP로 마이그레이션 |
| 의존성 해석 느림 | 고정 버전, repository 순서 최적화 |
| JVM 메모리 부족 | `-Xmx4g` 설정 |
