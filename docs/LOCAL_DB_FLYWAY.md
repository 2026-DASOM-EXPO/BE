# 로컬 DB 초기 데이터 실행

## 가장 간단한 실행: H2 로컬 프로필

별도 DB 설치 없이 아래 명령으로 실행하면 Flyway가 인메모리 H2에 스키마와 초기 데이터를 자동 생성합니다.

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

H2 데이터는 서버를 종료하면 사라지고, 다음 실행 시 다시 생성됩니다.

## PostgreSQL 로컬 DB

먼저 `expo` 데이터베이스만 생성한 뒤 환경 변수에 접속 정보를 지정하고 애플리케이션을 실행합니다.

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/expo"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "password"
.\gradlew.bat bootRun
```

애플리케이션 시작 시 다음 Flyway 마이그레이션이 순서대로 한 번씩 적용됩니다.

- `V1__RenameUsernameToLoginId.java`: 기존 `username` 컬럼 호환
- `V2__create_schema.sql`: 전체 테이블, 외래 키, 인덱스 생성
- `V3__seed_local_demo_data.sql`: 로컬 데모 데이터 삽입

## 초기 계정

- 관리자: `manager` / `Admin123!`
- 추가 계정: `manager2`, `worker1`, `worker2`, `worker3`
- 모든 초기 계정 비밀번호: `Admin123!`

초기 비밀번호는 로컬 개발 전용이며 운영 환경에서 사용하면 안 됩니다.

## 데이터 수

| 테이블 | 행 수 |
| --- | ---: |
| `users` | 5 |
| `refresh_tokens` | 5 |
| `workers` | 5 |
| `equipment` | 15 |
| `equipment_logs` | 5 |
| `wearable_commands` | 5 |
| `sensor_logs` | 5 |
| `risk_events` | 5 |
| `alerts` | 5 |
| `drones` | 5 |
| `drone_dispatches` | 5 |
| `drone_videos` | 5 |
| `drone_drop_logs` | 5 |

`equipment`는 작업자 5명 각각에 `HELMET`, `VEST`, `SHOES` 한 개씩 배정하여 총 15개입니다.

## 확인 SQL

```sql
SELECT COUNT(*) FROM workers;
SELECT type, COUNT(*) FROM equipment GROUP BY type ORDER BY type;
SELECT worker_id, COUNT(DISTINCT type)
FROM equipment
GROUP BY worker_id
ORDER BY worker_id;
```

정상 결과는 작업자 5명, 장비 유형별 5개, 각 작업자별 장비 유형 3개입니다.
