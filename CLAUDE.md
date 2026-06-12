# DataMonitor — CLAUDE.md

이 파일은 Claude Code가 본 프로젝트에서 작업할 때 따라야 할 개발 규칙과 컨텍스트를 정의한다.

---

## 프로젝트 개요

**DataMonitor**는 인메모리 데이터를 콘솔에서 실시간으로 조회·관리하는 관리자용 CLI 도구의 PoC이다.  
DB 연동 없이 JVM 힙 메모리에 샘플 데이터를 적재하고, REPL(Read-Eval-Print Loop) 방식의 텍스트 인터페이스로 CRUD 및 필터링 기능을 검증한다.

- 상세 요구사항 → `PRD.md`
- 구현 계획 → `PLAN.md`
- 언어: Java 17+ / 빌드: Gradle 9 (Kotlin DSL)
- 테스트 프레임워크: JUnit 5

---

## 개발 워크플로 — TDD (Red → Green)

모든 기능 구현은 아래 사이클을 엄격히 준수한다.

```
① RED   : 테스트 코드 작성 (구현 없이 실패하는 상태)
② 검증  : 사용자에게 테스트 코드 리뷰 요청 → 승인 대기
③ GREEN : 테스트를 통과시키는 최소한의 구현 코드 작성
④ 검증  : 사용자에게 구현 코드 리뷰 요청 → 승인 대기
⑤ COMMIT & PUSH : 사용자 승인 후에만 커밋 & 푸쉬 실행
```

### 단계별 규칙

#### RED 단계
- 테스트 파일만 생성한다. 구현 파일은 절대 먼저 만들지 않는다.
- 테스트가 컴파일 오류 없이 작성되어야 한다 (구현 클래스의 껍데기는 만들어도 됨).
- 테스트 케이스는 `PLAN.md`에 정의된 케이스를 모두 포함해야 한다.
- 작성 완료 후 반드시 사용자에게 "테스트 코드 검증을 요청합니다" 메시지와 함께 리뷰를 요청한다.
- **사용자 승인 없이 GREEN 단계로 넘어가지 않는다.**

#### GREEN 단계
- 테스트를 통과시키는 데 필요한 최소한의 코드만 작성한다.
- 과도한 추상화, 미래를 위한 설계, 사용되지 않는 메서드 추가를 금지한다.
- 구현 완료 후 반드시 사용자에게 "구현 코드 검증을 요청합니다" 메시지와 함께 리뷰를 요청한다.
- **사용자 승인 없이 커밋 & 푸쉬를 실행하지 않는다.**

#### COMMIT & PUSH 단계
- 사용자가 명시적으로 승인한 경우에만 실행한다.
- 커밋 메시지 형식: `[M{마일스톤}] {태스크ID} {한 줄 설명}`
  - 예: `[M1] T1-2 MonitorItem 도메인 모델 추가`
- 브랜치 전략: 별도 지시가 없으면 `master` 브랜치에 직접 커밋한다.

---

## 마일스톤 구조

| 마일스톤 | 내용 | 주요 파일 |
|----------|------|-----------|
| **M1** | 도메인 모델 + 인메모리 저장소 | `model/Status.java`, `model/MonitorItem.java`, `repository/InMemoryRepository.java` |
| **M2** | 서비스 레이어 | `service/SeedData.java`, `service/SummaryResult.java`, `service/MonitorService.java` |
| **M3** | CLI / REPL | `cli/TablePrinter.java`, `cli/CommandParser.java`, `Main.java`, `build.gradle.kts` |
| **M4** | 통합 검증 + 코드 정리 | 수동 AC 체크, `./gradlew test` 전체 통과 |

마일스톤은 순서대로 진행하며, 이전 마일스톤의 모든 테스트가 통과한 후 다음 단계를 시작한다.

---

## 패키지 구조

```
src/
├── main/java/org/example/
│   ├── Main.java                          # REPL 진입점
│   ├── cli/
│   │   ├── CommandParser.java             # 명령어 파싱 및 라우팅
│   │   └── TablePrinter.java             # 콘솔 테이블 출력
│   ├── model/
│   │   ├── MonitorItem.java              # 도메인 모델
│   │   └── Status.java                   # 상태 열거형
│   ├── repository/
│   │   └── InMemoryRepository.java       # 인메모리 저장소
│   └── service/
│       ├── MonitorService.java           # 비즈니스 로직
│       ├── SeedData.java                 # 샘플 데이터 정의
│       └── SummaryResult.java            # 집계 결과 값 객체
└── test/java/org/example/
    ├── repository/
    │   └── InMemoryRepositoryTest.java
    └── service/
        └── MonitorServiceTest.java
```

---

## 코딩 규칙

- **주석**: 기본적으로 작성하지 않는다. WHY가 비자명한 경우에만 한 줄 허용.
- **오류 처리**: CLI 경계(사용자 입력)에서만 예외를 잡는다. 서비스·저장소 내부에서는 예외를 위로 전파한다.
- **불변성**: `MonitorItem`의 `id`, `createdAt`은 생성 후 변경하지 않는다.
- **테스트 격리**: 각 테스트는 `@BeforeEach`에서 새 저장소 인스턴스를 생성하여 독립성을 보장한다.
- **추가 의존성**: 외부 라이브러리는 추가하지 않는다. Java 표준 라이브러리와 JUnit 5만 사용한다.

---

## 알려진 리스크

| 리스크 | 대응 방안 |
|--------|-----------|
| Windows 콘솔 한글 인코딩 깨짐 | `Main.java`에서 `PrintStream`을 UTF-8로 교체 |
| `seed()` 재호출 시 id 누적 증가 | `InMemoryRepository.clear()`에서 `idSequence.set(0)` 함께 호출 |
| `./gradlew run` 종료 시 입력 블로킹 | `exit` 명령으로만 종료, 실행 시 `--console=plain` 옵션 사용 |
