# Agent Team을 활용한 개발 워크플로우

Stash Android 프로젝트는 Claude Code의 에이전트 팀 기능을 활용하여 Phase 0~6까지 전체 앱을 개발했습니다.

---

## 팀 구성

3명의 전문 에이전트가 역할을 나눠 협업합니다.

| 역할 | 담당 | 규칙 파일 |
|------|------|-----------|
| **Lead** | 아키텍처 설계, Phase/Step 기획, 태스크 관리, 코드 리뷰 | `~/.claude/agents/lead.md` |
| **Dev** | 코드 구현, 커밋, 푸시 | `~/.claude/agents/dev.md` |
| **QA** | 테스트 작성, 빌드 검증, 품질 판정 | `~/.claude/agents/qa.md` |

사용자(나)는 **의사결정자**로서 Phase 승인, 방향 조정, 최종 확인만 합니다.

---

## 개발 루프

```
사용자: /dev 명령 실행
  ↓
Lead: Phase 설계 → Step 분할 → 사용자 승인 요청
  ↓
사용자: 승인
  ↓
Lead: TaskCreate로 태스크 생성 + 의존성 설정
  ↓
Dev: 태스크 구현 → 워크로그 작성 → 커밋 + 푸시 → 완료 보고
  ↓
QA: 테스트 작성 → 빌드 검증 → 통과/반려 보고
  ↓
Lead: Step 완료 확인 → PHASES.md 업데이트
  ↓
(다음 Step, 또는 Phase 완료 → 사용자에게 보고)
```

---

## Phase/Step 구조

### Phase = 독립적으로 동작하는 기능 덩어리

```
Phase 0: 프로젝트 셋업 (Gradle, Hilt, Circuit, 테마)
Phase 1: 데이터 모델 + 저장소 (Room, Repository)
Phase 2: 홈 화면 (카드 그리드, 필터 칩)
Phase 3: Share Intent (URL 파싱, 메타데이터 추출)
Phase 4: 상세 화면 + 딥링크
Phase 5: 검색 (키워드 + ONNX 시맨틱)
Phase 6: 설정 + 마무리 (테마, 에러 처리, 접근성, 성능)
```

### Step = 하나의 커밋으로 완결되는 최소 작업 단위

좋은 예:
- "Room LIKE 기반 키워드 검색 DAO 메서드 추가"
- "SearchPresenter 구현 (디바운스 키워드 검색)"

나쁜 예:
- "검색 기능 전체 구현" ← 이건 10개 이상의 Step으로 쪼개야 함

### 자가 검증 규칙

Step 설명에 "~하고 ~한다", "A + B", "A 및 B" 패턴이 있으면 무조건 분리합니다. Lead가 이 규칙을 자동으로 적용합니다.

---

## 태스크 관리

### TaskCreate + 의존성

```
#1 Step 6.1: DataStore 의존성 추가          (blockedBy: 없음)
#2 Step 6.2: ThemeMode 모델 정의            (blockedBy: #1)
#3 Step 6.3: PreferencesRepositoryImpl       (blockedBy: #2)
#5 Step 6.5: deleteAll 추가                 (blockedBy: 없음)  ← 독립 트랙
#11 Step 6.11: Presenter 에러 상태           (blockedBy: 없음)  ← 독립 트랙
```

독립적인 태스크는 병렬 트랙으로 운영합니다. Dev가 순서대로 처리하되, blockedBy가 없는 태스크를 우선 진행합니다.

### 태스크 상태 흐름

```
pending → in_progress → completed
```

Dev/QA가 작업 시작 시 `in_progress`, 완료 시 `completed`로 업데이트합니다.

---

## 커밋 규칙

모든 에이전트가 `~/.claude/rules/git-rules.md`를 따릅니다.

### 커밋 메시지 형식

```
분류: 한글 설명

상세 설명 (선택)

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
```

### Step 하나 = 즉시 커밋 + 푸시

Dev가 Step을 완료하면:
1. 워크로그 작성 (`.worklogs/YYYY-MM-DD.md`에 append)
2. 변경 파일 + 워크로그를 git add
3. HEREDOC으로 커밋
4. 즉시 푸시

커밋하지 않은 채 다음 Step으로 넘어가는 것은 금지입니다.

---

## 워크로그

### 구조

```
.worklogs/
├── 2026-02-21.md    ← 날짜별 단일 파일
├── 2026-02-22.md
└── .snapshot         ← 토큰/시간 스냅샷 (git 추적 안 함)
```

### 엔트리 형식

```markdown
---

## HH:MM

### 요청사항
- 사용자가 요청한 내용

### 작업 내용
- 구체적으로 무엇을 했는지

### 변경 통계
(git diff --cached --stat 결과)

### 토큰 사용량
- 모델: claude-opus-4-6
- 이번 작업: 25,180,408 토큰 / $17.32
- 소요 시간: 184분
- 일일 누적: 674,791,698 토큰 / $417.92
```

Dev/QA 에이전트는 커밋마다 워크로그를 작성합니다. 사용자가 `/worklog` 또는 `/commit`을 실행하면 토큰 delta 계산이 포함됩니다.

---

## 에이전트 간 소통

### SendMessage

에이전트들은 `SendMessage` 도구로 직접 소통합니다.

```
Lead → Dev: "Step 5.6 변경사항이 잘 만들어졌다. 커밋해라."
Lead → QA: "Step 5.7 테스트 작업 시작해라."
QA → Dev: "테스트 실패. 수정 필요."
Dev → Lead: "Step 완료. 다음 태스크 확인."
```

### 충돌 방지

- 태스크 소유자(owner)를 명확히 지정하여 같은 파일을 동시에 수정하지 않도록 합니다.
- 테스트는 QA 담당, 프로덕션 코드는 Dev 담당으로 분리합니다.

### 셧다운

Phase 완료 시 `shutdown_request`로 에이전트를 정리합니다.

```
Lead: Phase 6 완료. 수고했다.
→ shutdown_request to dev
→ shutdown_request to qa
→ shutdown_request to lead
→ TeamDelete
```

---

## 실제 개발 흐름 예시: Phase 5 (검색)

### 1. Phase 설계

Lead가 기존 코드를 분석하고 15개 Step을 설계했습니다.

```
Block A (키워드 검색): Step 5.1 ~ 5.7
Block B (시맨틱 검색): Step 5.8 ~ 5.15
```

### 2. 사용자 승인

```
Lead → 사용자: "15개 Step으로 설계했습니다. 승인해주세요."
사용자: "ㅇㅇ 계속"
```

### 3. 태스크 생성

Lead가 TaskCreate로 15개 태스크를 만들고, 의존성을 설정했습니다.

### 4. Dev 구현

```
Step 5.1: Room LIKE 검색 DAO → 커밋 (3aa70ab)
Step 5.2: Repository 검색 메서드 → 커밋 (7b7db59)
Step 5.3: SearchScreen 정의 → 커밋 (ab06e82)
Step 5.4: SearchPresenter 디바운스 → 커밋 (5a3f184)
Step 5.5: 검색 UI → 커밋 (16a62b5)
...
Step 5.13: 하이브리드 검색 통합 → 커밋 (ee5c74a)
```

### 5. QA 검증

```
Step 5.7: SearchPresenterTest 8개 테스트 → 전체 통과
Step 5.14: 시맨틱 검색 테스트 → 전체 통과
Step 5.15: 전체 빌드 + 테스트 → BUILD SUCCESSFUL
```

### 6. Phase 완료

```
Lead: PHASES.md 상태를 "완료 ✅"로 업데이트
사용자에게 최종 보고
```

---

## 프로젝트 전체 통계

| 항목 | 값 |
|------|-----|
| 총 Phase | 7개 (Phase 0~6) |
| 총 Step | 약 80개 |
| 총 커밋 | 60+ |
| 총 파일 | 50+ |
| 총 코드 | 5,000+ 줄 |
| 개발 기간 | 2일 (2026-02-21 ~ 22) |

---

## 핵심 원칙

1. **Phase 계획은 반드시 사용자 승인 후 진행한다** — AI가 마음대로 방향을 정하지 않음
2. **한 번에 하나의 Phase만 진행한다** — 집중과 품질 보장
3. **Step 하나 = 커밋 하나** — 작은 단위로 추적 가능한 진행
4. **테스트 실패 시 다음 단계로 넘어가지 않는다** — QA가 게이트키퍼
5. **워크로그로 모든 작업을 기록한다** — 투명한 진행 추적

---

## 설정 파일 구조

```
~/.claude/
├── agents/
│   ├── lead.md          # Lead 에이전트 역할 정의
│   ├── dev.md           # Dev 에이전트 역할 정의
│   └── qa.md            # QA 에이전트 역할 정의
├── rules/
│   ├── git-rules.md     # 커밋/푸시/PR 규칙
│   ├── review-rules.md  # 코드 리뷰 규칙
│   └── worklog-rules.md # 워크로그 규칙
├── guides/common/
│   ├── phase-development.md  # Phase/Step 개발 원칙
│   └── team-workflow.md      # 팀 워크플로우
└── settings.json        # 글로벌 설정 (hooks, skills 등)
```

이 구조를 갖추면 `/dev` 한 번으로 팀이 생성되고 개발이 시작됩니다.
