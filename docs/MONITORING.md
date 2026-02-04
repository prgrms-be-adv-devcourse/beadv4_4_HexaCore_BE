# Resello 모니터링 시스템 가이드

## 개요

이 프로젝트는 Prometheus + Grafana 기반의 모니터링 시스템과 K6 + InfluxDB 기반의 부하 테스트 환경을 제공합니다.

## 기술 스택

| 컴포넌트 | 버전 | 용도 | 포트 |
|----------|------|------|------|
| **Prometheus** | v2.50.0 | 메트릭 수집 및 저장 | 9090 |
| **Grafana** | v10.3.1 | 대시보드 시각화 | 3000 |
| **InfluxDB** | v1.8 | K6 부하테스트 결과 저장 | 8087 |
| **PostgreSQL** | 15-alpine | 테스트용 데이터베이스 | 5432 |
| **postgres-exporter** | v0.15.0 | PostgreSQL 메트릭 수집 | 9187 |

## 빠른 시작

### 1. 모니터링 환경 실행

```bash
docker compose -f docker-compose-monitoring.yml up -d
```

### 2. 애플리케이션 실행 (메트릭 수집 대상)

각 서비스는 Spring Boot Actuator를 통해 `/actuator/prometheus` 엔드포인트를 노출합니다.

```bash
# 예: Settlement 서비스 실행
./gradlew :settlement:bootRun
```

### 3. 대시보드 접속

- **Grafana**: http://localhost:3000
  - ID: `admin`
  - PW: `admin123`

- **Prometheus**: http://localhost:9090

## 서비스별 포트 매핑

| 서비스 | 포트 | Prometheus Job |
|--------|------|----------------|
| User | 8080 | user-service |
| Cash | 8081 | cash-service |
| Chat | 8082 | chat-service |
| Market | 8083 | market-service |
| Notification | 8084 | notification-service |
| Product | 8085 | product-service |
| Settlement | 8086 | settlement-service |

## Grafana 대시보드

### 1. Spring Boot Dashboard

Spring Boot 애플리케이션의 주요 메트릭을 시각화합니다.

**주요 패널:**
- **JVM Memory**: Heap/Non-Heap 메모리 사용량
- **JVM Threads**: 활성 스레드 수, 데몬 스레드
- **GC Statistics**: Garbage Collection 빈도 및 소요시간
- **HikariCP**: 커넥션 풀 상태 (Active/Idle/Pending)
- **HTTP Requests**: 요청 수, 응답 시간, 에러율
- **Logback**: 로그 레벨별 카운트

**확인할 지표:**

| 지표 | 정상 범위 | 주의 기준 |
|------|-----------|-----------|
| JVM Heap Used | < 80% | > 90% |
| HikariCP Active | < max pool | = max pool |
| GC Pause Time | < 100ms | > 500ms |
| HTTP p95 Latency | < 500ms | > 1000ms |

### 2. PostgreSQL Dashboard

데이터베이스 상태를 모니터링합니다.

**주요 패널:**
- **Overview**: 버전, 시작시간, 최대 연결 수, 연결 사용률
- **Sessions & Transactions**: 활성 세션, 커밋/롤백 비율
- **CRUD Statistics**: SELECT/INSERT/UPDATE/DELETE 처리량
- **Performance**: 캐시 히트율, 락, 데드락/충돌

**확인할 지표:**

| 지표 | 정상 범위 | 주의 기준 |
|------|-----------|-----------|
| Connection Usage | < 70% | > 90% |
| Cache Hit Rate | > 95% | < 80% |
| Deadlocks | 0 | > 0 |

### 3. K6 부하테스트 Dashboard

K6 부하 테스트 결과를 실시간으로 시각화합니다.

**주요 패널:**
- **Virtual Users**: 현재 활성 VU 수
- **Requests per Second**: 초당 요청 수
- **Errors Per Second**: 초당 에러 수
- **HTTP Request Metrics**: 지연시간 분포 (min/p90/p95/max)
- **Heatmap**: 응답시간 분포 히트맵

## K6 부하테스트 메트릭 설명

### 기본 메트릭

| Metric | Type | 설명 |
|--------|------|------|
| `vus` | Gauge | 현재 활성 가상 유저 수 |
| `vus_max` | Gauge | 가상 유저 최대 가능 수 |
| `iterations` | Counter | VU가 스크립트를 실행한 총 횟수 |
| `iteration_duration` | Trend | 전체 반복 완료 소요시간 (setup/teardown 포함) |
| `dropped_iterations` | Counter | VU/시간 부족으로 시작되지 않은 반복 수 |
| `data_received` | Counter | 수신된 데이터 양 |
| `data_sent` | Counter | 전송된 데이터 양 |
| `checks` | Rate | 체크 성공률 |

### HTTP 메트릭

| Metric | Type | 설명 |
|--------|------|------|
| `http_reqs` | Counter | K6가 생성한 총 HTTP 요청 수 |
| `http_req_blocked` | Trend | TCP 커넥션 슬롯 대기 시간 |
| `http_req_connecting` | Trend | TCP 커넥션 수립 소요 시간 |
| `http_req_tls_handshaking` | Trend | TLS 핸드쉐이킹 소요 시간 |
| `http_req_sending` | Trend | 데이터 전송 소요 시간 |
| `http_req_waiting` | Trend | 응답 대기 시간 (TTFB) |
| `http_req_receiving` | Trend | 데이터 수신 소요 시간 |
| `http_req_duration` | Trend | 총 요청 소요 시간 (sending + waiting + receiving) |
| `http_req_failed` | Rate | 요청 실패율 |

## K6 테스트 실행 방법

### 기본 실행

```bash
cd k6
k6 run <test-script>.js
```

### Grafana 연동 실행

테스트 결과를 Grafana 대시보드에서 실시간으로 확인하려면:

```bash
k6 run <test-script>.js --out influxdb=http://localhost:8087/k6
```

### 종료

```bash
docker compose -f docker-compose-monitoring.yml down
```

볼륨까지 삭제하려면:
```bash
docker compose -f docker-compose-monitoring.yml down -v
```
