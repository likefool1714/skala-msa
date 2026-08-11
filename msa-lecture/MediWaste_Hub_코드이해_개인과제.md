# MediWaste Hub 의료폐기물 수거·관리 MSA 코드 이해 보고서

- 과목: Agile 방법론 및 MSA 개발
- 과제: 코드 이해 개인과제
- 성명: 최민석
- 프로젝트명: MediWaste Hub
- 작성일: 2026. 08. 11.

---

## Chapter 1. 고객 중심 서비스 이해

### 1. 서비스 개요

MediWaste Hub는 병원·의원과 의료폐기물 수집·운반 업체를 연결하는 B2B 수거·관리 플랫폼이다. 병원·의원에서 발생하는 의료폐기물은 유형에 따라 적절한 방법으로 수거·운반되어야 하며, 배출 사업장과 수집·운반 업체 사이의 일정·요청사항·처리 상태 공유가 필요하다.

기존 스켈레톤은 강사가 강의를 등록하고, 학생이 수강신청과 결제를 진행하는 온라인 교육 플랫폼이었다. 본 프로젝트에서는 기존 MSA 구조를 재사용하면서 각 개념을 다음과 같이 의료폐기물 수거 도메인으로 치환하였다.

| 기존 교육 플랫폼 | MediWaste Hub |
|---|---|
| 학생 | 병원·의원 담당자 |
| 강사 | 의료폐기물 수집·운반 업체 |
| 강의 | 수거 서비스 |
| 강의 카테고리 | 의료폐기물 유형 |
| 수강신청 | 수거 신청 |
| 수강 결제 | 수거 비용 결제 |
| 수강 확정 | 수거 접수 확정 |
| 추천 강의 | 수거 이력 기반 추천 서비스 |

병원·의원 담당자는 수거 서비스를 조회하고 희망 수거일, 시작·종료 시간, 폐기물 정보, 요청사항을 입력해 수거를 신청할 수 있다. 수거업체는 신규 신청을 확인하고 수락 또는 거절한 후, 수거가 끝나면 완료 상태로 변경할 수 있다.

### 2. 고객에게 제공하는 가치

#### 2.1 병원·의원 담당자

- 의료폐기물 유형에 맞는 수거 서비스를 한 곳에서 조회할 수 있다.
- 희망 수거일과 시간, 폐기물 정보를 입력해 수거를 신청할 수 있다.
- 수거업체의 수락·거절·수거 완료 상태를 확인할 수 있다.
- 이전 이용 이력을 기반으로 관련 폐기물 수거 서비스를 추천받을 수 있다.

#### 2.2 의료폐기물 수집·운반 업체

- 폐기물 유형과 비용을 포함한 수거 서비스를 등록할 수 있다.
- 사이드바의 `업체 확인 대기`를 통해 신규 신청을 빠르게 확인할 수 있다.
- 신청 상태와 업무 조건을 확인하고 수락·거절할 수 있다.
- 수락한 신청은 수거 완료 상태로 변경하여 진행 이력을 관리할 수 있다.

### 3. 주요 고객 적용 사례

#### 사례 1. 병원의 일반의료폐기물 수거 신청

병원 담당자가 일반의료폐기물 수거 서비스를 선택한다. 희망 수거일과 시간, 폐기물 종류·수량, 사업장 출입 요청사항을 입력한 후 수거 비용을 결제한다. 결제 완료 이벤트가 처리되면 신청이 `CONFIRMED`로 변경되고 수거업체의 확인 대기 목록에 표시된다.

#### 사례 2. 수거업체의 신청 수락 및 완료 처리

수거업체 담당자가 업체 확인 대기 메뉴에서 신규 신청을 확인한다. 일정과 폐기물 정보를 검토한 후 수락하면 신청이 `ACCEPTED`로 변경된다. 수거 작업이 끝난 후 수거 완료 버튼을 누르면 `COMPLETED`로 변경된다.

#### 사례 3. 병원의 처리 결과 확인

병원 담당자는 `신청 상태 알림`에서 수거업체가 수락·거절한 결과와 수거 완료 상태를 확인할 수 있다. 알림을 확인하면 읽음 처리되며, 같은 신청의 상태가 다시 변경되면 새로운 알림으로 표시된다.

### 4. 현재 시스템의 한계

- 결제 기능은 외부 PG사가 아닌 교육용 결제 처리 구조이다.
- 알림 읽음 정보는 별도 알림 DB가 아닌 브라우저 `sessionStorage`에 저장된다.
- 병원과 수거업체의 위치·거리 계산 및 지도 표시 기능은 아직 구현되지 않았다.
- 추천 서비스는 실제 머신러닝 모델이 아닌 이용 이력 기반 규칙 추천이다.
- 실제 행정 신고·인계서·증빙 관리 시스템을 대체하지 않는다.

### 5. 구조 개선 제안

1. **Notification Service 도입**  
   상태 변경 이벤트를 Kafka로 받아 알림을 DB에 저장하고 `readAt`으로 읽음 여부를 관리한다.

2. **위치 기반 수거업체 추천**  
   병원과 수거업체의 주소·위도·경도를 저장하고 수거 가능 반경과 거리를 기준으로 서비스를 정렬한다.

3. **업체 자격 정보 검증**  
   사업자등록번호, 수집·운반 허가 정보, 허가 유효기간을 등록·검증하는 기능을 추가한다.

4. **수거 증빙 관리**  
   수거 완료 시 인계 정보, 사진, 문서를 등록할 수 있도록 파일 저장소와 연계한다.

---

## Chapter 2. 시스템 기술 구조 및 설계 이유

### 1. MSA를 적용한 이유

단일 서버에 모든 기능을 구현하면 특정 기능의 변경과 장애가 전체 시스템에 영향을 줄 수 있다. MediWaste Hub는 회원, 수거 서비스, 신청, 결제, 추천을 독립적인 서비스로 분리했다. 이를 통해 서비스별로 책임과 데이터 경계를 명확하게 나누고, 개별 기능을 독립적으로 수정·확장할 수 있도록 했다.

### 2. 서비스별 책임

| 서비스 | 기술 | 주요 책임 |
|---|---|---|
| Vue Frontend | Vue.js, Pinia, Axios | 사용자 화면, 상태 표시, API 호출 |
| API Gateway | Spring Cloud Gateway | 단일 API 진입점, 인증 정보 전달 |
| Auth Server | Spring Authorization Server | OAuth2 로그인과 토큰 발급 |
| User Service | Spring Boot | 회원가입, 역할, 담당자·기관명 관리 |
| Course Service | Spring Boot | 수거 서비스 등록·조회 |
| Enrollment Service | Spring Boot | 수거 신청과 상태 전이 관리 |
| Payment Service | Spring Boot | 수거 비용 결제, 결제 완료 이벤트 발행 |
| Recommend Service | FastAPI | 수거 이력 기반 규칙 추천 |
| Eureka Server | Spring Cloud Netflix Eureka | 서비스 등록과 탐색 |
| Kafka | Apache Kafka | 결제 완료 이벤트 전달 |
| MariaDB | MariaDB | 회원·수거 서비스·신청·결제 데이터 저장 |
| Auth UI | Nginx | 인증 서버 앞단의 커스텀 로그인 화면 |

### 3. 시스템 요청 처리 흐름

```text
병원·의원 회원가입 및 로그인
        ↓
수거 서비스 조회
        ↓
희망 수거일·시간·폐기물 정보 입력
        ↓
Enrollment Service의 PENDING 신청 생성
        ↓
Payment Service의 결제 처리
        ↓
Kafka payment-completed 이벤트 발행
        ↓
Enrollment Service가 이벤트를 수신하여 CONFIRMED로 변경
        ↓
수거업체가 수락(ACCEPTED) 또는 거절(REJECTED)
        ↓
수락된 신청의 수거 완료(COMPLETED) 처리
```

### 4. 핵심 코드 이해

#### 4.1 회원 기관명 저장

```java
User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .name(request.getName())
        .organizationName(request.getOrganizationName())
        .role(role)
        .build();
```

- **코드의 역할:** 회원가입 요청에서 담당자명과 병원·수거업체명을 분리해 저장한다.
- **입력값:** 이메일, 암호화할 비밀번호, 담당자명, 기관명, 역할.
- **실행 결과:** 로그인 후 사용자 역할을 구분하고 프로필과 수거 서비스 화면에 실제 기관명을 표시할 수 있다.

#### 4.2 수거 신청 상태 전이

```java
public void accept() {
    if (this.status != Status.CONFIRMED) {
        throw new IllegalArgumentException(
                "업체 확인 대기 상태의 신청만 수락할 수 있습니다");
    }
    this.status = Status.ACCEPTED;
}
```

- **코드의 역할:** 결제가 완료되어 업체 확인 대기 상태인 신청만 수락하도록 제한한다.
- **입력 조건:** 신청 상태가 `CONFIRMED`여야 한다.
- **실행 결과:** 정상적인 수락 시 `ACCEPTED`로 변경되며, 잘못된 상태에서 호출하면 예외가 발생한다.

#### 4.3 결제 완료 Kafka 이벤트 처리

```java
@KafkaListener(topics = "payment-completed")
public void handlePaymentCompleted(Map<String, Object> event) {
    Object requestIdValue = event.get("collectionRequestId");
    Long requestId = Long.valueOf(requestIdValue.toString());
    enrollmentService.confirmEnrollment(requestId);
}
```

- **코드의 역할:** Payment Service가 발행한 결제 완료 이벤트를 Enrollment Service에서 수신한다.
- **입력값:** Kafka 이벤트의 `collectionRequestId`.
- **실행 결과:** 해당 수거 신청을 결제 대기에서 접수 확정 상태로 변경한다.

#### 4.4 Vue 신청 필수항목 검증

```javascript
const fieldChecks = computed(() => ({
  date: Boolean(reservation.preferredCollectionDate),
  time: Boolean(
    reservation.preferredStartTime &&
    reservation.preferredEndTime &&
    reservation.preferredStartTime !== reservation.preferredEndTime
  ),
  waste: Boolean(reservation.wasteInformation.trim())
}))

const reservationComplete = computed(() =>
  Object.values(fieldChecks.value).every(Boolean)
)
```

- **코드의 역할:** 희망 수거일, 시작·종료 시간, 폐기물 정보의 입력 완료 여부를 확인한다.
- **입력 조건:** 필수항목이 모두 작성되고 시작·종료 시간이 서로 달라야 한다.
- **실행 결과:** 모든 검증이 완료되어야 수거 신청 버튼이 활성화된다.

### 5. FastAPI Recommend Service를 사용한 이유

FastAPI는 본 프로젝트에서 AI 모델을 학습하는 역할이 아니라, 수거 이력 기반 규칙 추천 결과를 REST API로 제공하는 역할을 한다. Python의 비동기 처리와 Pydantic 스키마 검증을 이용할 수 있으며, 추후 머신러닝 모델을 적용할 경우 기존 API 구조를 유지하면서 추천 로직만 교체할 수 있다.

현재 추천 규칙은 다음과 같다.

1. 병원 계정의 이전 수거 신청 이력을 조회한다.
2. 자주 이용한 폐기물 유형을 찾는다.
3. 같은 폐기물 유형 중 이용하지 않은 수거 서비스를 추천한다.
4. 이력이 없는 사용자에게는 접수 건수가 많은 서비스를 추천한다.

---

## Chapter 3. 트러블슈팅 사례 정리

### 1. 이전 계정으로 자동 로그인되는 문제

#### 문제 상황

로그아웃한 후 다른 계정으로 로그인하려고 해도 이전에 사용한 계정으로 즉시 로그인되었다.

#### 원인

Vue에 저장된 Access Token과 사용자 정보는 삭제되었지만, Auth Server에서 사용한 `JSESSIONID` 세션 쿠키가 브라우저에 남아 있었다. 인증 서버는 이 세션을 통해 기존 사용자가 아직 로그인된 것으로 판단했다.

#### 해결 방안

```javascript
accessToken.value = null
user.value = null
sessionStorage.removeItem('access_token')
sessionStorage.removeItem('user')

await fetch(`${AUTH_SERVER_URL}/logout`, {
  method: 'GET',
  credentials: 'include',
  mode: 'no-cors',
  cache: 'no-store'
})
```

OAuth2 인증을 시작하기 전에 Vue 토큰과 사용자 정보를 삭제하고 Auth Server의 로그아웃 API를 호출하여 서버 세션까지 종료했다. 이후 OAuth 요청에 `prompt=login`을 추가해 로그인 화면이 다시 표시되도록 했다.

#### 학습한 점

클라이언트의 토큰을 삭제하는 것과 인증 서버의 세션을 종료하는 것은 다른 작업임을 이해했다. OAuth2 인증 환경에서는 클라이언트와 인증 서버의 상태를 모두 고려해야 한다.

### 2. 회원가입 후 병원·수거업체명이 표시되지 않는 문제

#### 문제 상황

회원가입 화면에 병원명과 수거업체명 입력창을 추가했지만 회원가입 후 마이페이지에 해당 값이 표시되지 않았다.

#### 원인

Vue 회원가입 폼에만 `organizationName`을 추가했을 뿐, User Entity, 회원가입 DTO, 저장 로직과 `users` 테이블에는 해당 필드가 없었다. 프런트엔드와 백엔드의 데이터 구조가 서로 일치하지 않았다.

#### 해결 방안

```java
@NotBlank(message = "병원·의원명 또는 수거업체명은 필수입니다")
private String organizationName;
```

`organizationName`을 User Entity, RegisterRequest, UserResponse, UserService, DB 스키마에 모두 추가했다. Vue에서는 역할에 따라 `병원·의원명` 또는 `수거업체명`으로 라벨과 placeholder가 변경되도록 했다.

#### 학습한 점

화면의 입력 필드를 추가하는 작업은 Vue 템플릿만 변경하는 것이 아니다. API 요청 DTO, Entity, DB 컬럼, 저장 로직, API 응답 및 표시 로직이 모두 일치해야 정상 동작한다.

### 3. 자정을 넘는 수거 시간을 입력할 수 없는 문제

#### 문제 상황

희망 수거 시간을 `23:01 → 00:03`으로 선택하면 종료 시간이 시작 시간보다 이른 것으로 판단되어 신청 버튼이 활성화되지 않았다.

#### 원인

시작·종료 시간을 같은 날짜의 문자열로 비교했기 때문에 `00:03`이 `23:01`보다 이른 시간으로 판단되었다.

#### 해결 방안

```javascript
const crossesMidnight = computed(() =>
  fieldChecks.value.time &&
  reservation.preferredEndTime < reservation.preferredStartTime
)
```

시작과 종료 시간이 서로 같지 않으면 유효한 기간으로 판단하고, 종료 시간이 시작 시간보다 이르면 다음 날 종료로 해석했다. 화면에는 `23:01 → 다음 날 00:03`으로 표시해 사용자가 해석 결과를 확인할 수 있도록 했다.

#### 학습한 점

시간 값을 비교할 때는 시간만 보는 것이 아니라 날짜 변경 가능성도 고려해야 한다. 또한 시스템이 해석한 종료일을 사용자에게 명확히 보여줘야 잘못된 신청을 줄일 수 있다.

### 4. 신청 수락·거절 시 상태 변경 실패

#### 문제 상황

수거업체가 수락 또는 거절 버튼을 눌렀을 때 `수거 신청 상태를 변경하지 못했습니다.`라는 오류가 발생했다.

#### 원인

프런트엔드가 호출하는 API 주소와 Enrollment Controller의 엔드포인트가 일치하지 않거나, `CONFIRMED`가 아닌 신청을 수락·거절하려고 할 때 오류가 발생했다.

#### 해결 방안

```text
POST /api/enrollments/{id}/accept
POST /api/enrollments/{id}/reject
POST /api/enrollments/{id}/complete
```

Vue API 호출 주소와 Enrollment Controller의 엔드포인트를 일치시켰다. 또한 Entity 내부에 상태 전이 검증을 추가해 `CONFIRMED`에서만 수락·거절할 수 있고, `ACCEPTED`에서만 수거 완료 처리할 수 있도록 했다.

#### 학습한 점

프런트엔드와 백엔드 간 API 규약은 URL, HTTP Method, Header, 요청 본문, 응답 구조가 모두 일치해야 한다. 또한 상태 전이 규칙을 화면에만 두지 않고 Entity에서도 검증해야 잘못된 API 호출을 막을 수 있다.

### 5. Spring 기본 로그인 화면을 수정할 수 없는 문제

#### 문제 상황

OAuth2 로그인 시 Spring Security의 `Please sign in` 기본 화면이 표시되어 MediWaste Hub 화면 디자인과 일치하지 않았다.

#### 원인

Auth Server가 수정 가능한 소스 코드가 아닌 완성된 Docker 이미지로만 제공되었다. JAR 내부에도 수정할 수 있는 HTML 템플릿이 없었고 Spring Security가 실행 중 기본 로그인 화면을 생성하고 있었다.

#### 해결 방안

API Gateway 앞에 Nginx 기반 `auth-ui`를 추가했다. `GET /login`은 MediWaste Hub 전용 로그인 HTML을 보여주고, 실제 인증을 처리하는 `POST /login`은 기존 API Gateway와 Auth Server로 전달했다.

```nginx
location = /login {
    error_page 418 = @mediwaste_login;
    if ($request_method = GET) { return 418; }
    proxy_pass http://api-gateway:8080;
}
```

#### 학습한 점

외부에서 제공된 이미지를 직접 수정하기 어려울 때는 인증 로직 자체를 재구현하기보다 프록시 계층을 활용해 필요한 표현 계층만 교체할 수 있다. 인증 POST는 기존 서버에 위임했기 때문에 스켈레톤의 인증 규칙을 유지할 수 있었다.

---

## Chapter 4. 개인 수행 내용 및 느낀 점

### 1. 개인적으로 분석한 부분

- Enrollment Service의 수거 신청 생성과 상태 전이 구조
- Payment Service의 결제 완료 Kafka 이벤트 흐름
- Recommend Service의 신청 이력 기반 규칙 추천 로직
- OAuth2 로그인 과정과 Vue·Gateway·Auth Server 간 역할

### 2. 개인적으로 개선한 부분

- 담당자명과 병원·수거업체명을 분리한 회원가입 데이터 구조
- 수거 신청 필수항목 체크와 신청 버튼 활성화 조건
- 자정을 넘는 수거 시간의 다음 날 종료 처리
- 수거업체의 업체 확인 대기 신청 필터
- 병원의 수락·거절·수거 완료 상태 알림
- 알림 클릭 시 읽음 처리 및 상태 변경 시 신규 알림 표시
- 인증 서버 세션으로 인한 이전 계정 자동 로그인 문제 해결
- MediWaste Hub 전용 로그인 화면과 Vue 대시보드 디자인 개선

### 3. 프로젝트를 통해 느낀 점

번역 수준의 용어 변경만으로는 다른 도메인의 서비스를 완성할 수 없다. 온라인 교육의 수강신청과 의료폐기물 수거 신청은 `등록 → 신청 → 결제 → 확정`이라는 전체 흐름은 유사하지만, 필요한 입력 데이터와 상태 전이 규칙은 다르다. 따라서 기존 구조를 재사용하되 도메인의 핵심 규칙을 Entity·DTO·API·화면에 일관되게 반영해야 한다는 점을 학습했다.

또한 MSA에서는 하나의 사용자 기능이 여러 서비스에 걸쳐 실행된다. 수거 신청 확정은 Enrollment Service 하나의 기능이 아니라 Payment Service의 결제, Kafka의 이벤트 전달, Enrollment Service의 상태 변경이 이어진 결과이다. 오류가 발생했을 때 화면만 확인하는 것이 아니라 각 서비스의 API 응답, Kafka 이벤트, DB 상태를 함께 확인해야 한다.

이번 프로젝트를 통해 스켈레톤 코드를 모두 새로 작성하지 않고도 기존 서비스 경계와 이벤트 흐름을 이해하면 다른 비즈니스 주제로 확장할 수 있음을 확인했다. 추후에는 알림 서비스와 위치 기반 추천을 분리해 더 명확한 MSA 구조로 확장해 보고 싶다.

---

## 참고: 주요 API 명세

| 서비스 | Method | API | 설명 |
|---|---|---|---|
| User | POST | `/api/users/register` | 병원·수거업체 회원가입 |
| User | GET | `/api/users/me` | 현재 사용자 정보 조회 |
| Course | POST | `/api/courses` | 수거 서비스 등록 |
| Course | GET | `/api/courses` | 수거 서비스 목록 조회 |
| Enrollment | POST | `/api/enrollments` | 수거 신청 |
| Enrollment | GET | `/api/enrollments/my` | 병원의 신청 목록 조회 |
| Enrollment | GET | `/api/enrollments/carrier/my` | 수거업체의 접수 목록 조회 |
| Enrollment | POST | `/api/enrollments/{id}/accept` | 수거 신청 수락 |
| Enrollment | POST | `/api/enrollments/{id}/reject` | 수거 신청 거절 |
| Enrollment | POST | `/api/enrollments/{id}/complete` | 수거 완료 처리 |
| Recommend | GET | `/api/recommend/{userId}` | 이력 기반 수거 서비스 추천 |

