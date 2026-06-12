# DataMonitor

인메모리 데이터를 콘솔에서 실시간으로 조회·관리하는 관리자용 CLI 도구 PoC.  
DB 없이 JVM 힙 메모리에 샘플 데이터를 적재하고, REPL 방식의 텍스트 인터페이스로 CRUD 및 필터링 기능을 검증한다.

---

## 요구사항

- Java 17+
- Gradle 9 (Wrapper 포함, 별도 설치 불필요)

---

## 실행 방법

```
.\gradlew.bat run --console=plain
```

> Windows cmd 기준. Git Bash / Linux / macOS 에서는 `./gradlew run --console=plain`

실행하면 샘플 데이터 10건이 자동으로 적재되고 `>` 프롬프트가 표시된다.

---

## 명령어

| 명령어 | 설명 | 예시 |
|--------|------|------|
| `list` | 전체 목록 출력 | `list` |
| `list --status <STATUS>` | 상태로 필터링 | `list --status WARNING` |
| `list --category <CAT>` | 카테고리로 필터링 | `list --category system` |
| `get <id>` | ID로 단건 조회 | `get 3` |
| `add <name> <category> <value>` | 항목 추가 (status 기본값: ACTIVE) | `add cpu-2 system 72.5` |
| `update <id> --value <v>` | 측정값 수정 | `update 3 --value 95.1` |
| `update <id> --status <S>` | 상태 수정 | `update 3 --status WARNING` |
| `delete <id>` | 항목 삭제 | `delete 3` |
| `summary` | 상태별 건수·평균값 집계 출력 | `summary` |
| `seed` | 샘플 데이터 10건 재적재 | `seed` |
| `help` | 명령어 목록 출력 | `help` |
| `exit` | 프로그램 종료 | `exit` |

STATUS 값: `ACTIVE` / `INACTIVE` / `WARNING`

---

## 실행 예시

```
DataMonitor started. 명령어를 입력하세요. (help / exit)
> list
+------+------------------+-------------+-----------+------------+---------------------+
| ID   | NAME             | CATEGORY    |     VALUE | STATUS     | CREATED_AT          |
+------+------------------+-------------+-----------+------------+---------------------+
| 1    | CPU 사용률        | system      |     45.20 | ACTIVE     | 2026-06-12 10:00:00 |
| 2    | 메모리 사용률      | system      |     78.50 | WARNING    | 2026-06-12 10:00:00 |
...
+------+------------------+-------------+-----------+------------+---------------------+

> list --status WARNING
> summary
> add my-metric custom 55.5
추가됨: id=11, name=my-metric

> delete 1
삭제됨: id=1

> exit
종료합니다.
```

---

## 테스트 실행

```
.\gradlew.bat test
```

| 대상 | 테스트 수 |
|------|-----------|
| `InMemoryRepositoryTest` | 8개 |
| `MonitorServiceTest` | 12개 |
| `CommandParserTest` | 13개 |
| **합계** | **33개** |

---

## 프로젝트 구조

```
src/
├── main/java/org/example/
│   ├── Main.java                      # REPL 진입점
│   ├── cli/
│   │   ├── CommandParser.java         # 명령어 파싱 및 라우팅
│   │   └── TablePrinter.java          # 콘솔 테이블 출력
│   ├── model/
│   │   ├── MonitorItem.java           # 도메인 모델
│   │   └── Status.java                # 상태 열거형
│   ├── repository/
│   │   └── InMemoryRepository.java    # 인메모리 저장소 (ArrayList + AtomicLong)
│   └── service/
│       ├── MonitorService.java        # 비즈니스 로직
│       ├── SeedData.java              # 샘플 데이터 정의
│       └── SummaryResult.java         # 집계 결과 값 객체
└── test/java/org/example/
    ├── cli/
    │   └── CommandParserTest.java
    ├── repository/
    │   └── InMemoryRepositoryTest.java
    └── service/
        └── MonitorServiceTest.java
```

---

## 기술 스택

| 항목 | 내용 |
|------|------|
| 언어 | Java 17 |
| 빌드 | Gradle 9 (Kotlin DSL) |
| 테스트 | JUnit 5 |
| 저장소 | 인메모리 (`ArrayList` + `AtomicLong`) |
| 외부 라이브러리 | 없음 (Java 표준 라이브러리만 사용) |

---

## 제약사항

- 프로세스 재시작 시 데이터가 초기화된다 (인메모리 특성상 영속성 없음)
- 단일 사용자 CLI 전용 (네트워크·인증 미지원)
