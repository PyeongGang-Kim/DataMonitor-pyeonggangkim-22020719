# DataMonitor — Implementation Plan

> PRD 기반 구현 계획서. 각 태스크는 독립적으로 완료·검증 가능한 단위로 분리한다.

---

## 전체 진행 순서

```
M1 (모델 + 저장소) → M2 (서비스) → M3 (CLI / REPL) → M4 (통합 검증)
```

각 마일스톤은 이전 단계가 완료된 후 시작한다.  
테스트는 구현과 동시에 작성한다(TDD 방향).

---

## M1 — 도메인 모델 및 인메모리 저장소

### 목표
데이터 구조와 저장소를 정의하여 이후 모든 계층의 기반을 마련한다.

### 태스크

#### T1-1. `Status` 열거형 생성
- **파일**: `src/main/java/org/example/model/Status.java`
- **내용**
  ```java
  public enum Status { ACTIVE, INACTIVE, WARNING }
  ```
- **완료 기준**: 컴파일 통과

#### T1-2. `MonitorItem` 도메인 모델 생성
- **파일**: `src/main/java/org/example/model/MonitorItem.java`
- **필드**: `id(long)`, `name(String)`, `category(String)`, `value(double)`, `status(Status)`, `createdAt(LocalDateTime)`
- **구현 사항**
  - 모든 필드를 받는 생성자 (id 제외, id는 저장소에서 주입)
  - getter / setter
  - `toString()` — 디버깅용 간단 출력
- **완료 기준**: 필드 접근 및 변경 정상 동작

#### T1-3. `InMemoryRepository` 구현
- **파일**: `src/main/java/org/example/repository/InMemoryRepository.java`
- **내부 구조**: `List<MonitorItem>` + `AtomicLong idSequence`
- **메서드**

  | 메서드 시그니처 | 설명 |
  |----------------|------|
  | `MonitorItem save(MonitorItem item)` | id 채번 후 저장, 저장된 객체 반환 |
  | `Optional<MonitorItem> findById(long id)` | ID로 단건 조회 |
  | `List<MonitorItem> findAll()` | 전체 목록 반환 (삽입 순) |
  | `List<MonitorItem> findByStatus(Status status)` | 상태 필터 |
  | `List<MonitorItem> findByCategory(String category)` | 카테고리 필터 (대소문자 무시) |
  | `boolean deleteById(long id)` | 삭제 성공 여부 반환 |
  | `void clear()` | 전체 초기화 (seed 재적재용) |

- **완료 기준**: 아래 단위 테스트 통과

#### T1-4. `InMemoryRepositoryTest` 작성
- **파일**: `src/test/java/org/example/repository/InMemoryRepositoryTest.java`
- **테스트 케이스**

  | 케이스 | 검증 내용 |
  |--------|-----------|
  | `save_assignsIncrementalId` | 저장 시 id가 1부터 순차 증가 |
  | `findById_returnsItem` | 존재하는 id → 정상 반환 |
  | `findById_returnsEmpty` | 없는 id → `Optional.empty()` |
  | `findByStatus_filtersCorrectly` | WARNING만 조회 시 해당 항목만 반환 |
  | `findByCategory_caseInsensitive` | "Network" / "network" 동일 결과 |
  | `deleteById_removesItem` | 삭제 후 findAll 목록에서 제거됨 |
  | `deleteById_returnsFalse_whenNotFound` | 없는 id 삭제 시 false 반환 |
  | `clear_removesAll` | clear 후 findAll 빈 리스트 |

---

## M2 — 서비스 레이어

### 목표
비즈니스 로직(CRUD, 필터, 집계, 시드)을 CLI와 저장소 사이에서 담당하는 계층을 구현한다.

### 태스크

#### T2-1. `SeedData` 유틸리티 클래스 생성
- **파일**: `src/main/java/org/example/service/SeedData.java`
- **내용**: PRD 4.3의 샘플 10건을 `List<MonitorItem>`으로 반환하는 정적 메서드
  ```java
  public static List<MonitorItem> defaultItems() { ... }
  ```
- **완료 기준**: 반환 리스트 크기 == 10, 각 필드값 PRD와 일치

#### T2-2. `SummaryResult` 값 객체 생성
- **파일**: `src/main/java/org/example/service/SummaryResult.java`
- **필드**: `status(Status)`, `count(int)`, `avgValue(double)`
- **완료 기준**: 컴파일 통과

#### T2-3. `MonitorService` 구현
- **파일**: `src/main/java/org/example/service/MonitorService.java`
- **의존성**: 생성자 주입으로 `InMemoryRepository` 수령
- **메서드**

  | 메서드 시그니처 | 설명 |
  |----------------|------|
  | `void seed()` | 저장소 clear → 샘플 10건 재적재 |
  | `List<MonitorItem> listAll()` | 전체 목록 |
  | `List<MonitorItem> listByStatus(String status)` | 상태 문자열 → `Status` 변환 후 필터, 잘못된 값 → `IllegalArgumentException` |
  | `List<MonitorItem> listByCategory(String category)` | 카테고리 필터 |
  | `MonitorItem getById(long id)` | 없으면 `NoSuchElementException("Not found: id=N")` |
  | `MonitorItem add(String name, String category, double value)` | 신규 항목 추가, 기본 status = ACTIVE |
  | `MonitorItem updateValue(long id, double value)` | 값 수정 |
  | `MonitorItem updateStatus(long id, String status)` | 상태 수정 |
  | `void delete(long id)` | 없으면 `NoSuchElementException` |
  | `List<SummaryResult> summary()` | 상태별 집계 (건수, 평균값) |

- **완료 기준**: 아래 단위 테스트 통과

#### T2-4. `MonitorServiceTest` 작성
- **파일**: `src/test/java/org/example/service/MonitorServiceTest.java`
- **테스트 케이스**

  | 케이스 | 검증 내용 |
  |--------|-----------|
  | `seed_loads10Items` | seed 후 listAll 크기 == 10 |
  | `seed_clearsPreviousData` | 이미 데이터 있을 때 seed 재호출 → 중복 없이 10건 |
  | `listByStatus_returnsOnlyMatchingStatus` | WARNING 항목만 반환 |
  | `listByStatus_throwsOnInvalidStatus` | "UNKNOWN" 입력 → `IllegalArgumentException` |
  | `listByCategory_returnsMatchingItems` | "api" 카테고리 2건 반환 |
  | `getById_throwsWhenNotFound` | 없는 id → `NoSuchElementException` |
  | `add_incrementsListSize` | add 후 listAll 크기 +1 |
  | `add_defaultStatusIsActive` | 추가된 항목 status == ACTIVE |
  | `updateValue_changesValue` | value 변경 후 getById로 확인 |
  | `updateStatus_changesStatus` | status 변경 후 getById로 확인 |
  | `delete_removesItem` | delete 후 getById → `NoSuchElementException` |
  | `summary_correctCountAndAvg` | seed 후 summary의 ACTIVE 건수 및 avgValue 검증 |

---

## M3 — CLI / REPL 루프

### 목표
표준 입출력 기반의 대화형 명령어 인터페이스를 구현하여 서비스 레이어와 연결한다.

### 태스크

#### T3-1. `TablePrinter` 유틸리티 구현
- **파일**: `src/main/java/org/example/cli/TablePrinter.java`
- **메서드**
  - `static void printItems(List<MonitorItem> items)` — PRD 4.4 형식의 테이블 출력
  - `static void printSummary(List<SummaryResult> results)` — 집계 테이블 출력
  - `static void printItem(MonitorItem item)` — 단건 출력 (get 명령용)
- **출력 규격**
  - 컬럼: `ID(4) | NAME(16) | CATEGORY(11) | VALUE(9) | STATUS(10) | CREATED_AT(19)`
  - 구분선: `-` 반복
  - 빈 목록: `"결과가 없습니다."` 출력
- **완료 기준**: 수동 실행으로 PRD 예시와 동일한 포맷 확인

#### T3-2. `CommandParser` 구현
- **파일**: `src/main/java/org/example/cli/CommandParser.java`
- **역할**: 입력 문자열을 파싱하여 `MonitorService`를 호출하고 결과를 `TablePrinter`로 출력
- **처리 흐름**

  ```
  입력 문자열
      └─ split by whitespace
          └─ 첫 토큰 = 명령어 분기
              ├─ list       → 옵션 파싱 (--status / --category) → service 호출 → TablePrinter
              ├─ get        → id 파싱 → service.getById → TablePrinter.printItem
              ├─ add        → name/category/value 파싱 → service.add → 성공 메시지
              ├─ update     → id + 옵션 파싱 → service.updateValue / updateStatus
              ├─ delete     → id 파싱 → service.delete → 성공 메시지
              ├─ summary    → service.summary → TablePrinter.printSummary
              ├─ seed       → service.seed → 성공 메시지
              ├─ help       → 명령어 목록 출력 (하드코딩)
              └─ 그 외      → "알 수 없는 명령어: <입력값>"
  ```

- **오류 처리**
  - `NumberFormatException` → "숫자 형식 오류: <상세>"
  - `IllegalArgumentException` → 메시지 그대로 출력
  - `NoSuchElementException` → 메시지 그대로 출력
  - 예외 발생 후 프롬프트로 정상 복귀 (프로그램 종료 금지)
- **완료 기준**: 각 명령어 수동 입력 후 올바른 출력 확인

#### T3-3. `Main.java` REPL 루프 교체
- **파일**: `src/main/java/org/example/Main.java`
- **구현 사항**
  ```
  1. InMemoryRepository 생성
  2. MonitorService 생성 (저장소 주입)
  3. service.seed() 호출
  4. CommandParser 생성 (서비스 주입)
  5. Scanner로 표준 입력 읽기 루프:
       "> " 프롬프트 출력
       입력 읽기
       "exit" 이면 종료 메시지 출력 후 break
       공백 입력 무시
       CommandParser.execute(line) 호출
  ```
- **완료 기준**: `./gradlew run` 으로 REPL 진입, 전체 명령어 수동 테스트 통과

#### T3-4. `build.gradle.kts` application 플러그인 추가
- **변경 내용**
  ```kotlin
  plugins {
      id("java")
      id("application")
  }
  application {
      mainClass.set("org.example.Main")
  }
  ```
- **완료 기준**: `./gradlew run --console=plain` 으로 REPL 실행

---

## M4 — 통합 검증

### 목표
PRD 수용 기준 9항목을 모두 통과함을 확인하고 코드를 정리한다.

### 태스크

#### T4-1. 수용 기준 체크리스트 수동 검증

| # | 검증 명령 시퀀스 | 기대 결과 |
|---|-----------------|-----------|
| AC1 | `./gradlew run` 실행 | 샘플 10건 적재 + `>` 프롬프트 표시 |
| AC2 | `list` | 10건 테이블 출력 |
| AC3 | `list --status WARNING` | WARNING 항목 4건만 출력 |
| AC4 | `add test-metric custom 55.5` → `list` | 11건, 방금 추가된 항목 포함 |
| AC5 | `update 1 --value 99.9` → `get 1` | value = 99.90 확인 |
| AC6 | `delete 1` → `get 1` | "Not found" 메시지 출력 |
| AC7 | `summary` | ACTIVE 5건·avg 정확, WARNING 4건·avg 정확 |
| AC8 | `get 999` / `update abc` / `unknowncmd` | 오류 메시지 출력 후 프롬프트 복귀 |
| AC9 | `./gradlew test` | 모든 테스트 PASSED |

#### T4-2. `./gradlew test` 전체 실행 및 결과 확인
- 실패 케이스 없을 것
- 서비스 레이어 메서드 커버리지 80% 이상 목표

#### T4-3. 코드 정리
- IntelliJ IDEA 자동 생성 주석 (`//TIP ...`) 제거
- 미사용 import 제거
- 파일별 Javadoc 불필요, 공개 메서드에 한 줄 설명 주석만 허용

---

## 파일 생성 순서 요약

```
M1
  src/main/java/org/example/model/Status.java
  src/main/java/org/example/model/MonitorItem.java
  src/main/java/org/example/repository/InMemoryRepository.java
  src/test/java/org/example/repository/InMemoryRepositoryTest.java

M2
  src/main/java/org/example/service/SeedData.java
  src/main/java/org/example/service/SummaryResult.java
  src/main/java/org/example/service/MonitorService.java
  src/test/java/org/example/service/MonitorServiceTest.java

M3
  src/main/java/org/example/cli/TablePrinter.java
  src/main/java/org/example/cli/CommandParser.java
  src/main/java/org/example/Main.java          (수정)
  build.gradle.kts                              (수정)

M4
  수동 AC 체크 → ./gradlew test → 코드 정리
```

---

## 의존성 다이어그램

```
Main
 └─ CommandParser
      └─ MonitorService
           └─ InMemoryRepository
                └─ MonitorItem / Status

      └─ TablePrinter
           └─ MonitorItem / SummaryResult

MonitorService
 └─ SeedData         (시드 데이터 공급)
 └─ SummaryResult    (집계 결과 반환)
```

---

## 리스크 및 고려 사항

| 리스크 | 대응 |
|--------|------|
| `Scanner`의 `hasNextLine()` 블로킹으로 `./gradlew run` 종료 어려움 | `--console=plain` 옵션 사용, `exit` 명령으로만 종료 |
| Windows 콘솔 인코딩 문제 (한글 깨짐) | `System.out`을 `new PrintStream(System.out, true, "UTF-8")`로 교체 고려 |
| `AtomicLong` id가 `clear()` 후 리셋되지 않으면 seed id가 계속 증가 | `clear()` 에서 `idSequence.set(0)` 함께 호출 |
