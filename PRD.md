# DataMonitor — Product Requirements Document (PRD)

## 1. 개요

**DataMonitor**는 인메모리 데이터를 콘솔에서 실시간으로 조회·관리할 수 있는 관리자용 CLI 도구의 PoC(Proof of Concept)이다.  
실제 DB 연동 없이 JVM 힙 메모리에 샘플 데이터를 적재하고, 텍스트 기반 인터페이스로 CRUD 및 필터링 기능을 검증하는 것을 목표로 한다.

---

## 2. 목표 및 비목표

### 목표
- 콘솔(표준 입출력)만으로 데이터를 실시간 조회·등록·수정·삭제할 수 있는 최소 기능 구현
- 인메모리 저장소(List / Map)를 사용하여 DB 없이 동작 가능한 구조 검증
- 명령어 기반 인터페이스(REPL 방식)로 관리자가 직관적으로 사용할 수 있는 UX 설계
- JUnit 5 단위 테스트로 핵심 비즈니스 로직 검증

### 비목표
- 실제 RDBMS / NoSQL 연동
- 네트워크(REST API, gRPC 등) 노출
- 인증·권한 관리
- 데이터 영속성 (프로세스 재시작 시 데이터 초기화)

---

## 3. 사용자

| 역할 | 설명 |
|------|------|
| 관리자(Admin) | CLI를 직접 실행하여 데이터를 조회·관리하는 단일 사용자 |

---

## 4. 기능 요구사항

### 4.1 데이터 모델 — `MonitorItem`

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | `long` | 자동 증가 고유 식별자 |
| `name` | `String` | 항목 이름 |
| `category` | `String` | 분류 카테고리 |
| `value` | `double` | 측정값 |
| `status` | `enum Status` | `ACTIVE` / `INACTIVE` / `WARNING` |
| `createdAt` | `LocalDateTime` | 생성 시각 |

### 4.2 명령어 인터페이스 (REPL)

프로그램 실행 후 `>` 프롬프트가 표시되며, 아래 명령어를 지원한다.

| 명령어 | 설명 | 예시 |
|--------|------|------|
| `list` | 전체 데이터 목록 출력 | `list` |
| `list --status <STATUS>` | 상태로 필터링 | `list --status WARNING` |
| `list --category <CAT>` | 카테고리로 필터링 | `list --category network` |
| `get <id>` | ID로 단건 조회 | `get 3` |
| `add <name> <category> <value>` | 새 항목 추가 (status 기본값: ACTIVE) | `add cpu system 82.5` |
| `update <id> --value <v>` | 측정값 수정 | `update 3 --value 95.1` |
| `update <id> --status <S>` | 상태 수정 | `update 3 --status WARNING` |
| `delete <id>` | 항목 삭제 | `delete 3` |
| `summary` | 상태별 집계(건수, 평균값) 출력 | `summary` |
| `seed` | 샘플 데이터 재적재 | `seed` |
| `help` | 명령어 목록 출력 | `help` |
| `exit` | 프로그램 종료 | `exit` |

### 4.3 샘플 데이터

`seed` 명령(또는 최초 실행) 시 아래 10건의 데이터를 인메모리에 자동 적재한다.

| id | name | category | value | status |
|----|------|----------|-------|--------|
| 1 | cpu-usage | system | 45.2 | ACTIVE |
| 2 | memory-usage | system | 78.9 | WARNING |
| 3 | disk-io | system | 12.3 | ACTIVE |
| 4 | network-in | network | 102.5 | ACTIVE |
| 5 | network-out | network | 88.0 | ACTIVE |
| 6 | db-conn-pool | database | 95.0 | WARNING |
| 7 | cache-hit-rate | cache | 62.4 | INACTIVE |
| 8 | api-latency | api | 340.0 | WARNING |
| 9 | error-rate | api | 0.5 | ACTIVE |
| 10 | queue-depth | messaging | 1500.0 | WARNING |

### 4.4 출력 포맷

**`list` / `get` 출력 예시**
```
ID  NAME             CATEGORY    VALUE     STATUS     CREATED_AT
--  ---------------  ----------  --------  ---------  -------------------
 1  cpu-usage        system         45.20  ACTIVE     2026-06-12 09:00:00
 2  memory-usage     system         78.90  WARNING    2026-06-12 09:00:01
```

**`summary` 출력 예시**
```
STATUS      COUNT   AVG_VALUE
----------  -----   ---------
ACTIVE          5       49.70
WARNING         4      505.98
INACTIVE        1       62.40
```

---

## 5. 비기능 요구사항

| 항목 | 요건 |
|------|------|
| 언어 / 런타임 | Java 17+, Gradle 9 |
| 빌드 | `./gradlew run` 한 명령으로 실행 가능 |
| 응답 시간 | 모든 명령 100 ms 이내 응답 (인메모리 특성상 자연히 충족) |
| 테스트 커버리지 | 서비스 레이어 핵심 메서드 80% 이상 (JUnit 5) |
| 코드 구조 | 계층 분리: `model` / `repository` / `service` / `cli` 패키지 |

---

## 6. 패키지 구조 (예시)

```
src/main/java/org/example/
├── Main.java                  # 진입점 — REPL 루프
├── cli/
│   └── CommandParser.java     # 명령어 파싱 및 라우팅
├── model/
│   ├── MonitorItem.java       # 도메인 모델
│   └── Status.java            # 상태 열거형
├── repository/
│   └── InMemoryRepository.java # 인메모리 저장소 (List + AtomicLong ID)
└── service/
    └── MonitorService.java    # 비즈니스 로직 (필터, 집계, CRUD)
```

---

## 7. 수용 기준 (Acceptance Criteria)

1. `./gradlew run` 실행 시 샘플 데이터 10건이 자동 적재되고 `>` 프롬프트가 표시된다.
2. `list` 명령 실행 시 전체 10건이 테이블 형식으로 출력된다.
3. `list --status WARNING` 실행 시 WARNING 상태 항목만 출력된다.
4. `add` 명령으로 항목을 추가하면 `list` 결과에 즉시 반영된다.
5. `update` 명령으로 값·상태를 수정하면 `get <id>` 결과에 즉시 반영된다.
6. `delete <id>` 실행 후 해당 ID로 `get`하면 "Not found" 메시지가 출력된다.
7. `summary` 명령은 상태별 건수와 평균값을 정확히 집계하여 출력한다.
8. 존재하지 않는 ID나 잘못된 명령어 입력 시 오류 메시지를 출력하고 프롬프트로 복귀한다.
9. 서비스 레이어 단위 테스트가 모두 통과한다 (`./gradlew test`).

---

## 8. 마일스톤

| 단계 | 내용 | 산출물 |
|------|------|--------|
| M1 | 도메인 모델 및 인메모리 저장소 구현 | `model`, `repository` 패키지 + 테스트 |
| M2 | 서비스 레이어 구현 (CRUD, 필터, 집계) | `service` 패키지 + 테스트 |
| M3 | CLI / REPL 루프 구현 | `cli` 패키지, `Main.java` 업데이트 |
| M4 | 통합 검증 및 샘플 데이터 시드 적용 | 전체 수용 기준 통과 확인 |
