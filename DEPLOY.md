# 배포 관련 문서

Resello 프로젝트의 배포 및 인프라 구성

---

## 아키텍처 개요

### 단일 서버 구성도 (AWS EC2 t3.large)

```
                              ┌─────────────────┐
                              │    클라이언트    │
                              │  (브라우저/앱)   │
                              └────────┬────────┘
                                       │
                                       ▼ HTTPS (443)
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                            AWS EC2 t3.large (Ubuntu)                                 │
│                            IP: 13.209.85.165                                         │
│                            도메인: api.resello.co.kr (백엔드)                           │
│                                    www.resello.co.kr (프론트엔드)                       │
│ ┌──────────────────────────────────────────────────────────────────────────────────┐ │
│ │                              K3s 클러스터 (단일 노드)                                 │ │
│ │                                                                                  │ │
│ │  ┌─────────────────────────────────────────────────────────────────────────────┐ │ │
│ │  │                    kube-system namespace                                    │ │ │
│ │  │  ┌─────────────────┐  ┌─────────────────┐                                   │ │ │
│ │  │  │    Traefik      │  │    CoreDNS      │                                   │ │ │
│ │  │  │ (Ingress/SSL)   │  │   (DNS 해석)     │                                   │ │ │
│ │  │  │   :80, :443     │  │                 │                                   │ │ │
│ │  │  └────────┬────────┘  └─────────────────┘                                   │ │ │
│ │  └───────────┼─────────────────────────────────────────────────────────────────┘ │ │
│ │              │                                                                   │ │
│ │              ▼ HTTP (8080)                                                       │ │
│ │  ┌─────────────────────────────────────────────────────────────────────────────┐ │ │
│ │  │                    default namespace (애플리케이션)                            │ │ │
│ │  │                                                                             │ │ │
│ │  │  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐      │ │ │
│ │  │  │   user    │ │  product  │ │  market   │ │   chat    │ │   cash    │      │ │ │
│ │  │  │  service  │ │  service  │ │  service  │ │  service  │ │  service  │    │ │ │
│ │  │  │  :8080    │ │  :8080    │ │  :8080    │ │  :8080    │ │  :8080    │    │ │ │
│ │  │  └───────────┘ └───────────┘ └───────────┘ └───────────┘ └───────────┘    │ │ │
│ │  │  ┌───────────┐ ┌───────────┐                                              │ │ │
│ │  │  │notification│ │settlement │                                             │ │ │
│ │  │  │  service  │ │  service  │                                              │ │ │
│ │  │  │  :8080    │ │  :8080    │                                              │ │ │
│ │  │  └───────────┘ └─────┬─────┘                                              │ │ │
│ │  └──────────────────────┼──────────────────────────────────────────────────────┘ │ │
│ │                         │                                                        │ │
│ │  ┌──────────────────────┼──────────────────────────────────────────────────────┐ │ │
│ │  │                    호스트 Docker (K3s 외부)                                   │ │ │
│ │  │                         ▼                                                  │ │ │
│ │  │  ┌───────────┐ ┌───────────┐                                               │ │ │
│ │  │  │PostgreSQL │ │Elasticsearch│   ← Docker 컨테이너로 실행                      │ │ │
│ │  │  │  :5432    │ │  :9200     │                                              │ │ │
│ │  │  └───────────┘ └───────────┘                                               │ │ │
│ │  └─────────────────────────────────────────────────────────────────────────────┘ │ │
│ │                                                                                  │ │
│ │  ┌─────────────────────────────────────────────────────────────────────────────┐ │ │
│ │  │                    argocd namespace (CD 도구)                               │ │ │
│ │  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │ │ │
│ │  │  │  argocd-server  │  │   repo-server   │  │ app-controller  │             │ │ │
│ │  │  │   (웹 UI)       │  │ (Git 연동)      │  │  (배포 관리)    │             │ │ │
│ │  │  └─────────────────┘  └─────────────────┘  └─────────────────┘             │ │ │
│ │  └─────────────────────────────────────────────────────────────────────────────┘ │ │
│ │                                                                                  │ │
│ │  ┌─────────────────────────────────────────────────────────────────────────────┐ │ │
│ │  │                    cert-manager namespace (SSL 인증서)                      │ │ │
│ │  │  ┌─────────────────┐  ┌─────────────────┐                                  │ │ │
│ │  │  │  cert-manager   │  │    webhook      │  ← Let's Encrypt 자동 발급       │ │ │
│ │  │  └─────────────────┘  └─────────────────┘                                  │ │ │
│ │  └─────────────────────────────────────────────────────────────────────────────┘ │ │
│ └──────────────────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼ (AWS 서비스)
                    ┌──────────────────────────────────────┐
                    │         AWS 서비스                    │
                    │  ┌─────────┐ ┌─────────┐             │
                    │  │   SQS   │ │   S3    │             │
                    │  │ (알림큐) │ │ (파일)  │             │
                    │  └─────────┘ └─────────┘             │
                    └──────────────────────────────────────┘
```

### 현재 데이터 저장소 구성

**K3s 외부 Docker 컨테이너로 실행 중:**

```bash
# 현재 실행 중인 컨테이너
docker ps

CONTAINER ID   IMAGE                 PORTS                          NAMES
c24491afe32e   postgres:15           0.0.0.0:5432->5432/tcp         postgres-db
b7be28b042a8   elasticsearch:8.9.0   0.0.0.0:9200->9200/tcp, 9300   elasticsearch-db
```

| 서비스 | 이미지 | 포트 | 용도 |
|--------|--------|------|------|
| PostgreSQL | postgres:15 | 5432 | 메인 데이터베이스 |
| Elasticsearch | elasticsearch:8.9.0 | 9200, 9300 | 검색 엔진 |

> **참고**: DB는 K3s Pod이 아닌 호스트 Docker에서 직접 실행 중. 애플리케이션에서 `host.docker.internal` 또는 호스트 IP로 접근해야됨

### 도메인 구성

| 도메인 | 용도 | 대상 |
|--------|------|------|
| `api.resello.co.kr` | 백엔드 API | K3s (Spring Boot 서비스들) |
| `www.resello.co.kr` | 프론트엔드 | Vercel |
| `resello.co.kr` | 루트 도메인 | www로 리다이렉트 |


### 네트워크 흐름

```
1. 백엔드 API 요청
   https://api.resello.co.kr/api/v1/users/...
              │
              ▼
2. Traefik (Ingress Controller)
   - HTTP → HTTPS 리다이렉션 (http://로 접속 시 https://로 강제 이동)
   - SSL 인증서 검증 (cert-manager가 발급한 Let's Encrypt 인증서)
   - SSL Termination (외부 HTTPS → 내부 Pod은 HTTP로 통신)
   - 경로 기반 라우팅 (/api/v1/users → user-service)
              │
              ▼
3. Kubernetes Service
   - user-service:8080 (ClusterIP)
   - 로드밸런싱 (Pod이 여러 개일 경우)
              │
              ▼
4. Pod (Spring Boot 애플리케이션)
   - 실제 비즈니스 로직 처리
   - DB/Redis/Kafka 연결
```

### 서비스 모듈 (7개)

| 모듈 | 설명 | 엔드포인트 |
|------|------|-----------|
| `user` | 사용자 관리 | `/api/v1/users`, `/api/v1` (토큰 재발급) |
| `product` | 상품 관리 | `/api/v1/products`, `/api/v1/products/categories`, `/api/v1/products/brands` |
| `market` | 입찰/거래 관리 | `/api/v1/market` |
| `chat` | 채팅 서비스 | `/api/v1/chat`, `/api/v1/chat-ws` |
| `cash` | 결제/캐시 관리 | `/api/v1/cash/payments` |
| `notification` | 알림 서비스 | `/api/v1/notifications`, `/api/v1/price-alerts` |
| `settlement` | 정산 서비스 | `/api/v1/admin/settlements` |

### 공유 라이브러리 (2개)

| 모듈 | 설명 |
|------|------|
| `common` | 공통 코드 (응답 포맷, 에러 코드 등) |
| `security` | 보안 관련 공통 코드 (JWT, 인증 등) |

### 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| **언어** | Java | 25 |
| **프레임워크** | Spring Boot | 4.0.1 |
| **빌드** | Gradle | - |
| **DB** | PostgreSQL | 15 |
| **검색** | Elasticsearch | 8.9.0 |
| **캐시** | Redis | - |
| **메시징** | AWS SQS | - |
| **인증** | JWT, OAuth2, Spring Security | - |

---
### K3s 클러스터 구성

| 컴포넌트 | 역할 | 비고 |
|----------|------|------|
| **K3s** | 경량 Kubernetes | 컨테이너 오케스트레이션 |
| **Traefik** | Ingress Controller | K3s 기본 포함, 리버스 프록시 |
| **ArgoCD** | GitOps CD | Helm Chart 자동 배포 |
| **cert-manager** | 인증서 관리 | Let's Encrypt 자동 발급/갱신 |

### 클러스터 상태 확인

```bash
# 노드 확인
sudo kubectl get nodes

# 전체 Pod 확인
sudo kubectl get pods -A

# ArgoCD Application 확인
sudo kubectl get applications -n argocd
```

---

## CI/CD 파이프라인

### 전체 흐름

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   개발자      │     │   GitHub    │     │   GitHub    │     │    K3s     │
│  코드 Push   │ ──▶ │   Actions   │ ──▶ │    GHCR     │ ──▶  │  클러스터   │
│             │     │  (CI/CD)    │     │ (이미지저장)   │     │  (ArgoCD)   │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
```

### CI 파이프라인 (`.github/workflows/ci.yml`)

```
1. dev 브랜치에 Push/PR
       ↓
2. 변경된 모듈 감지 (dorny/paths-filter)
       ↓
3. 해당 모듈만 Docker 이미지 빌드
       ↓
4. GHCR (GitHub Container Registry)에 Push
       ↓
5. Helm Chart 레포지토리 values.yaml 업데이트
       ↓
6. ArgoCD가 변경 감지 → 자동 배포
```

### 배포 확인 명령어

```bash
# ArgoCD Application 상태 확인
sudo kubectl get applications -n argocd

# 배포된 Pod 확인
sudo kubectl get pods

# 서비스 확인
sudo kubectl get svc
```

---
## 배포 방법

### 자동 배포

1. `dev` 브랜치에 코드 Push
2. GitHub Actions가 자동으로 빌드 및 이미지 Push
3. Helm Chart 업데이트
4. ArgoCD가 자동으로 K3s에 배포

---

## SSL/Ingress 설정

### 구성 파일

| 파일 | 설명 |
|------|------|
| `cluster-issuer.yml` | Let's Encrypt ClusterIssuer |
| `middleware-redirect-https.yml` | HTTP→HTTPS 리다이렉션 Middleware |
| `ingress.yml` | 도메인 라우팅 + SSL 설정 |

### 적용 순서

#### 1. DNS 설정 (가비아)
```
A 레코드: api → ec2_ip
A 레코드: www → Vercel 프론트 서버
```

#### 2. cert-manager 설치
```bash
sudo kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.4/cert-manager.yaml

# 설치 확인
sudo kubectl get pods -n cert-manager
```

#### 3. ClusterIssuer 적용
```bash
# 이메일 수정 후 적용
sudo kubectl apply -f cluster-issuer.yml

# 확인
sudo kubectl get clusterissuer
```

#### 4. Middleware 적용
```bash
sudo kubectl apply -f middleware-redirect-https.yml

# 확인
sudo kubectl get middleware
```

#### 5. Ingress 적용
```bash
sudo kubectl apply -f ingress.yml

# 확인
sudo kubectl get ingress
sudo kubectl get certificate
```

#### 6. 접속 테스트
```bash
# HTTPS 직접 접속
curl -I https://api.resello.co.kr

# HTTP → HTTPS 리다이렉션 확인
curl -I http://api.resello.co.kr
# 308 Permanent Redirect → https://api.resello.co.kr
```

### 라우팅 규칙

**api.resello.co.kr (백엔드)**

| 경로 | 서비스 | 포트 |
|------|--------|------|
| `/api/v1/users` | user-service | 8080 |
| `/api/v1/products` | product-service | 8080 |
| `/api/v1/market` | market-service | 8080 |
| `/api/v1/chat` | chat-service | 8080 |
| `/api/v1/cash` | cash-service | 8080 |
| `/api/v1/notifications` | notification-service | 8080 |
| `/api/v1/price-alerts` | notification-service | 8080 |
| `/api/v1/admin/settlements` | settlement-service | 8080 |

**www.resello.co.kr (프론트엔드)**

| 경로 | 서비스 | 포트 |
|------|--------|------|
| `/` | frontend-service | 3000 |

### 프론트엔드 (www.resello.co.kr)

**Vercel 등 외부 서비스 사용 시:**
1. DNS에서 `www` A 레코드를 Vercel IP로 변경
2. 이 서버의 Ingress 설정은 무시됨 (DNS가 다른 곳을 가리키므로)
3. Vercel이 자동으로 SSL 처리