# The following practice code is intended for educational purposes only. For contact :  audit@korea.ac.kr, Sungryel Lim Ph.D

# This practice code is not a completed commercial version but has been developed for educational purposes; supplementation is required depending on the deployment objective for use as a commercial service.

# MediWaste Hub 의료폐기물 수거·관리 B2B 플랫폼

병·의원 등 의료폐기물 배출 사업장과 수집·운반 업체를 연결하는 MSA 교육용 프로젝트입니다.
내부 인증 호환을 위해 `STUDENT=배출 사업장`, `INSTRUCTOR=수집·운반 업체` 역할값을 사용합니다.

## 핵심 업무 흐름

수집·운반 업체 수거 서비스 등록 → 배출 사업장 수거 신청(PENDING) → 결제 →
`payment.completed` Kafka 이벤트 → 수거 접수 확정(CONFIRMED) → 이용 이력 기반 추천

Gateway 이미지의 기존 라우팅과 호환하기 위해 외부 API 경로와 프로젝트 디렉터리에는
`courses`, `enrollments` 명칭이 일부 유지됩니다. 실제 DB와 API 데이터 모델은
`collection_services`, `collection_requests` 도메인을 사용합니다.

본 프로젝트는 공식 올바로시스템이나 법정 신고 절차를 대체하지 않으며, 교육용으로
수거 신청·결제·접수 상태와 업무용 기초정보를 관리합니다.

기존 MariaDB 볼륨으로 강의 플랫폼을 실행한 적이 있다면 최초 전환 시 아래 명령으로
기존 실습 데이터를 제거한 후 다시 실행해야 합니다.

```bash
docker compose down -v
docker compose build --no-cache && docker compose up -d
```

# 전체 백엔드 기동 순서 (depends_on 기반)
MariaDB / Kafka (인프라)
  → Eureka (서비스 등록)
    → Auth Server (인증)
      → API Gateway + 4개 서비스
        → Recommend Service

# 공통 이미지 파일 로드 (API Gateway, Auth Server)
docker load -i infra-images.tar

# msa-lecture/auth-server:1.0 등 태그 확인
docker images

## 프로젝트 루트에서 (초기 트러블슈팅/리빌드 고려, 캐시 없이 빌드, 컨테이너는 묶어서 백그라운드로 실행)
docker compose build --no-cache
docker compose up -d

## 또는 한줄로
docker compose build --no-cache && docker compose up -d

# 로그 확인
## 전체 로그 한번에 보기
docker compose logs -f

## 또는 개별 컨테이너 로그 보기
docker compose logs -f [서비스명]

docker compose logs -f mariadb
docker compose logs -f kafka
docker compose logs -f eureka-server
docker compose logs -f auth-server
docker compose logs -f api-gateway
docker compose logs -f user-service
docker compose logs -f course-service
docker compose logs -f enrollment-service
docker compose logs -f payment-service
docker compose logs -f recommend-service

# 전체 종료 (또는 컨테이너 빌드 중, 실패 시에 기존 컨테이너 정리)
docker compose down

# 서버 기동 상태 확인
http://localhost:8761/

# 프론트엔드 실행
## 로컬 실행 방법
cd vue-frontend
npm install
npm run dev

## 브라우저에서 접속
http://localhost:3000 
